package com.mecanica.oficina_inventory_service.adapters.messaging;

import com.mecanica.oficina_inventory_service.adapters.persistence.InsumosJpaGateway;
import com.mecanica.oficina_inventory_service.adapters.persistence.OutboxEventoJpaEntity;
import com.mecanica.oficina_inventory_service.adapters.persistence.repository.BaixaOrdemServicoSpringDataRepository;
import com.mecanica.oficina_inventory_service.adapters.persistence.repository.OutboxEventoSpringDataRepository;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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

    @Test
    void orcamentoSemInsumosSegueASaga() {
        String os = UUID.randomUUID().toString();

        receber("OrcamentoAprovado", os, "{\"ordemServicoId\":\"" + os + "\",\"insumos\":[]}");

        assertThat(eventosPublicados(os)).containsExactly("EstoqueBaixado");
    }
}
