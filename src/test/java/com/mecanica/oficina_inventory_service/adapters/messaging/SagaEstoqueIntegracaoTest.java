package com.mecanica.oficina_inventory_service.adapters.messaging;

import com.mecanica.oficina_inventory_service.adapters.persistence.InsumosJpaGateway;
import com.mecanica.oficina_inventory_service.adapters.persistence.OutboxEventoJpaEntity;
import com.mecanica.oficina_inventory_service.adapters.persistence.repository.BaixaOrdemServicoSpringDataRepository;
import com.mecanica.oficina_inventory_service.adapters.persistence.repository.OutboxEventoSpringDataRepository;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Passos da Saga no inventory de ponta a ponta (sem broker): evento recebido → estoque → outbox.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SagaEstoqueIntegracaoTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    private ProcessadorEventos processador;
    @Autowired
    private InsumosJpaGateway insumos;
    @Autowired
    private OutboxEventoSpringDataRepository outbox;
    @Autowired
    private BaixaOrdemServicoSpringDataRepository baixas;

    private String oleo;

    @BeforeEach
    void setUp() {
        outbox.deleteAll();
        baixas.deleteAll();
        oleo = insumos.criar(Insumos.criar("Óleo " + UUID.randomUUID(), new BigDecimal("45.90"), 10, 2, "L")).getId();
    }

    private ProcessadorEventos.Resultado receber(String eventType, String sagaId, String payload) {
        return processador.processar(new EventoEnvelope(UUID.randomUUID().toString(), eventType, 1, Instant.now(),
                "oficina-billing-service", sagaId, JSON.readTree(payload)));
    }

    private List<String> eventosPublicados(String sagaId) {
        return outbox.findAll().stream()
                .filter(e -> e.getSagaId().equals(sagaId))
                .sorted((a, b) -> a.getCriadoEm().compareTo(b.getCriadoEm()))
                .map(OutboxEventoJpaEntity::getEventType)
                .toList();
    }

    @Test
    void orcamentoAprovadoBaixaEPagamentoRecusadoEstorna() {
        String os = UUID.randomUUID().toString();

        receber("OrcamentoAprovado", os,
                "{\"ordemServicoId\":\"" + os + "\",\"insumos\":[{\"insumoId\":\"" + oleo + "\",\"quantidade\":4}]}");

        assertThat(insumos.obterEstoqueAtual(oleo)).isEqualTo(6);
        assertThat(eventosPublicados(os)).containsExactly("EstoqueBaixado");

        receber("PagamentoRecusado", os, "{\"ordemServicoId\":\"" + os + "\"}");
        receber("ExecucaoFalhou", os, "{\"ordemServicoId\":\"" + os + "\"}");

        assertThat(insumos.obterEstoqueAtual(oleo)).isEqualTo(10);
        assertThat(eventosPublicados(os)).containsExactly("EstoqueBaixado", "EstoqueEstornado");
    }

    @Test
    void saldoInsuficienteNaoAlteraEstoque() {
        String os = UUID.randomUUID().toString();

        receber("OrcamentoAprovado", os,
                "{\"ordemServicoId\":\"" + os + "\",\"insumos\":[{\"insumoId\":\"" + oleo + "\",\"quantidade\":11}]}");

        assertThat(insumos.obterEstoqueAtual(oleo)).isEqualTo(10);
        assertThat(eventosPublicados(os)).containsExactly("EstoqueInsuficiente");
        assertThat(baixas.findById(os)).isEmpty();
    }

    /**
     * Oito OS aprovadas ao mesmo tempo pedindo 3 L de óleo (há 10) e 1 filtro: só três cabem. Metade
     * lista os insumos na ordem inversa, o que daria deadlock se a trava não seguisse a ordem de id.
     */
    @Test
    void baixasConcorrentesNaoVendemMaisDoQueOEstoque() throws Exception {
        String filtro = insumos.criar(Insumos.criar("Filtro " + UUID.randomUUID(), new BigDecimal("30.00"), 10, 2, "un")).getId();
        int pedidos = 8;
        List<String> ordens = IntStream.range(0, pedidos).mapToObj(i -> UUID.randomUUID().toString()).toList();

        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(pedidos);
        try {
            List<Future<ProcessadorEventos.Resultado>> resultados = new ArrayList<>();
            for (int i = 0; i < pedidos; i++) {
                String os = ordens.get(i);
                String oleoJson = "{\"insumoId\":\"" + oleo + "\",\"quantidade\":3}";
                String filtroJson = "{\"insumoId\":\"" + filtro + "\",\"quantidade\":1}";
                String itens = i % 2 == 0 ? oleoJson + "," + filtroJson : filtroJson + "," + oleoJson;
                resultados.add(executor.submit(() -> {
                    largada.await();
                    return receber("OrcamentoAprovado", os, "{\"ordemServicoId\":\"" + os + "\",\"insumos\":[" + itens + "]}");
                }));
            }
            largada.countDown();
            for (Future<ProcessadorEventos.Resultado> resultado : resultados) {
                assertThat(resultado.get(30, TimeUnit.SECONDS)).isEqualTo(ProcessadorEventos.Resultado.PROCESSADO);
            }
        } finally {
            executor.shutdownNow();
        }

        List<String> desfechos = ordens.stream().flatMap(os -> eventosPublicados(os).stream()).toList();
        assertThat(desfechos).filteredOn("EstoqueBaixado"::equals).hasSize(3);
        assertThat(desfechos).filteredOn("EstoqueInsuficiente"::equals).hasSize(5);
        assertThat(insumos.obterEstoqueAtual(oleo)).isEqualTo(1);
        assertThat(insumos.obterEstoqueAtual(filtro)).isEqualTo(7);
        assertThat(baixas.count()).isEqualTo(3);
    }

    @Test
    void orcamentoSemInsumosSegueASaga() {
        String os = UUID.randomUUID().toString();

        receber("OrcamentoAprovado", os, "{\"ordemServicoId\":\"" + os + "\",\"insumos\":[]}");

        assertThat(eventosPublicados(os)).containsExactly("EstoqueBaixado");
    }
}
