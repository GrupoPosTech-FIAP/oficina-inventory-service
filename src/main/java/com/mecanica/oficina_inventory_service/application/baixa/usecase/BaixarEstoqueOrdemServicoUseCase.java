package com.mecanica.oficina_inventory_service.application.baixa.usecase;

import com.mecanica.oficina_inventory_service.application.baixa.gateway.BaixaOrdemServicoGateway;
import com.mecanica.oficina_inventory_service.application.evento.EventosSaga;
import com.mecanica.oficina_inventory_service.application.evento.gateway.PublicadorEventosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.BaixarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.domain.baixa.BaixaOrdemServico;
import com.mecanica.oficina_inventory_service.domain.baixa.ItemBaixa;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Passo da Saga disparado por {@code OrcamentoAprovado}: baixa todos os insumos da OS ou nenhum.
 *
 * <p>O saldo é conferido antes de qualquer alteração; se algum insumo faltar, publica
 * {@code EstoqueInsuficiente} sem mexer no estoque. Caso contrário baixa tudo, guarda o registro
 * da baixa (usado na compensação) e publica {@code EstoqueBaixado} — inclusive com lista vazia,
 * para a Saga seguir quando a OS não usa insumos.
 */
public class BaixarEstoqueOrdemServicoUseCase {

    private final InsumosGateway insumosGateway;
    private final BaixaOrdemServicoGateway baixaGateway;
    private final BaixarEstoqueUseCase baixarEstoqueUseCase;
    private final PublicadorEventosGateway publicador;
    private final Clock clock;

    public BaixarEstoqueOrdemServicoUseCase(InsumosGateway insumosGateway,
                                            BaixaOrdemServicoGateway baixaGateway,
                                            BaixarEstoqueUseCase baixarEstoqueUseCase,
                                            PublicadorEventosGateway publicador,
                                            Clock clock) {
        this.insumosGateway = insumosGateway;
        this.baixaGateway = baixaGateway;
        this.baixarEstoqueUseCase = baixarEstoqueUseCase;
        this.publicador = publicador;
        this.clock = clock;
    }

    public void executar(String ordemServicoId, List<ItemEstoqueInput> itensSolicitados) {
        if (baixaGateway.buscarPorOrdemServico(ordemServicoId).isPresent()) {
            return; // baixa já feita para esta OS: reentrega do mesmo pedido
        }

        List<ItemBaixa> itens = consolidar(itensSolicitados);

        List<Map<String, Object>> faltantes = new ArrayList<>();
        for (ItemBaixa item : itens) {
            int disponivel = insumosGateway.buscar(item.insumoId()).map(Insumos::getEstoqueAtual).orElse(0);
            if (disponivel < item.quantidade()) {
                faltantes.add(Map.of(
                        "insumoId", item.insumoId(),
                        "solicitado", item.quantidade(),
                        "disponivel", disponivel));
            }
        }

        if (!faltantes.isEmpty()) {
            publicador.publicar(EventosSaga.ESTOQUE_INSUFICIENTE, ordemServicoId, Map.of(
                    "ordemServicoId", ordemServicoId,
                    "itens", faltantes));
            return;
        }

        if (!itens.isEmpty()) {
            baixarEstoqueUseCase.executar(itens.stream()
                    .map(i -> new ItemEstoqueInput(i.insumoId(), i.quantidade()))
                    .toList());
        }
        baixaGateway.salvar(BaixaOrdemServico.registrar(ordemServicoId, itens, clock.instant()));

        publicador.publicar(EventosSaga.ESTOQUE_BAIXADO, ordemServicoId, Map.of(
                "ordemServicoId", ordemServicoId,
                "itens", paraPayload(itens)));
    }

    /** Soma as quantidades de um mesmo insumo que apareça mais de uma vez no orçamento. */
    private static List<ItemBaixa> consolidar(List<ItemEstoqueInput> itensSolicitados) {
        Map<String, Integer> quantidades = new LinkedHashMap<>();
        for (ItemEstoqueInput item : Optional.ofNullable(itensSolicitados).orElse(List.of())) {
            if (item == null || item.insumoId() == null || item.insumoId().isBlank()) {
                throw new IllegalArgumentException("O ID do insumo é obrigatório");
            }
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade para baixa deve ser positiva");
            }
            quantidades.merge(item.insumoId(), item.quantidade(), Integer::sum);
        }
        return quantidades.entrySet().stream()
                .map(e -> new ItemBaixa(e.getKey(), e.getValue()))
                .toList();
    }

    static List<Map<String, Object>> paraPayload(List<ItemBaixa> itens) {
        return itens.stream()
                .<Map<String, Object>>map(i -> Map.of("insumoId", i.insumoId(), "quantidade", i.quantidade()))
                .toList();
    }
}
