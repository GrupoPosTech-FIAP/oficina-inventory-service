package com.mecanica.oficina_inventory_service.application.baixa.usecase;

import com.mecanica.oficina_inventory_service.application.baixa.gateway.BaixaOrdemServicoGateway;
import com.mecanica.oficina_inventory_service.application.evento.EventosSaga;
import com.mecanica.oficina_inventory_service.application.evento.gateway.PublicadorEventosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.EstornarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.domain.baixa.BaixaOrdemServico;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;

/**
 * Compensação da Saga: devolve ao estoque os insumos baixados para a OS e publica
 * {@code EstoqueEstornado}.
 *
 * <p>Sem baixa registrada (o estoque nunca foi baixado) ou com baixa já estornada, não faz nada.
 * Isso torna a compensação idempotente mesmo quando dois eventos de falha chegam para a mesma OS.
 */
public class EstornarEstoqueOrdemServicoUseCase {

    private final BaixaOrdemServicoGateway baixaGateway;
    private final EstornarEstoqueUseCase estornarEstoqueUseCase;
    private final PublicadorEventosGateway publicador;
    private final Clock clock;

    public EstornarEstoqueOrdemServicoUseCase(BaixaOrdemServicoGateway baixaGateway,
                                              EstornarEstoqueUseCase estornarEstoqueUseCase,
                                              PublicadorEventosGateway publicador,
                                              Clock clock) {
        this.baixaGateway = baixaGateway;
        this.estornarEstoqueUseCase = estornarEstoqueUseCase;
        this.publicador = publicador;
        this.clock = clock;
    }

    /**
     * @param motivo evento de falha que disparou a compensação (ex.: {@code PagamentoRecusado})
     * @return {@code true} se houve estorno
     */
    public boolean executar(String ordemServicoId, String motivo) {
        Optional<BaixaOrdemServico> registro = baixaGateway.buscarPorOrdemServico(ordemServicoId);
        if (registro.isEmpty() || registro.get().isEstornada()) {
            return false;
        }

        BaixaOrdemServico baixa = registro.get();
        if (!baixa.getItens().isEmpty()) {
            estornarEstoqueUseCase.executar(baixa.getItens().stream()
                    .map(i -> new ItemEstoqueInput(i.insumoId(), i.quantidade()))
                    .toList());
        }
        baixa.estornar(clock.instant());
        baixaGateway.salvar(baixa);

        publicador.publicar(EventosSaga.ESTOQUE_ESTORNADO, ordemServicoId, Map.of(
                "ordemServicoId", ordemServicoId,
                "motivo", motivo,
                "itens", BaixarEstoqueOrdemServicoUseCase.paraPayload(baixa.getItens())));
        return true;
    }
}
