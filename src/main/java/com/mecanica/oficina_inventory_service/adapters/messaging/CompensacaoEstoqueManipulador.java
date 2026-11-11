package com.mecanica.oficina_inventory_service.adapters.messaging;

import com.mecanica.oficina_inventory_service.application.baixa.usecase.EstornarEstoqueOrdemServicoUseCase;
import com.mecanica.oficina_inventory_service.application.evento.EventosSaga;
import java.util.Set;
import org.springframework.stereotype.Component;

/** Falha de pagamento ou de execução → estorna os insumos baixados para a OS. */
@Component
public class CompensacaoEstoqueManipulador implements ManipuladorEvento {

    private final EstornarEstoqueOrdemServicoUseCase useCase;

    public CompensacaoEstoqueManipulador(EstornarEstoqueOrdemServicoUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of(EventosSaga.PAGAMENTO_RECUSADO, EventosSaga.PAGAMENTO_EXPIRADO, EventosSaga.EXECUCAO_FALHOU);
    }

    @Override
    public void tratar(EventoEnvelope evento) {
        useCase.executar(evento.sagaId(), evento.eventType());
    }
}
