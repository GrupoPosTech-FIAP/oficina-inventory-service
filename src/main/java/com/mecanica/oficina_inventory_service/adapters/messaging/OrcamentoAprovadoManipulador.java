package com.mecanica.oficina_inventory_service.adapters.messaging;

import com.mecanica.oficina_inventory_service.application.baixa.usecase.BaixarEstoqueOrdemServicoUseCase;
import com.mecanica.oficina_inventory_service.application.evento.EventosSaga;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** {@code OrcamentoAprovado} → baixa os insumos do orçamento. */
@Component
public class OrcamentoAprovadoManipulador implements ManipuladorEvento {

    private final BaixarEstoqueOrdemServicoUseCase useCase;

    public OrcamentoAprovadoManipulador(BaixarEstoqueOrdemServicoUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of(EventosSaga.ORCAMENTO_APROVADO);
    }

    @Override
    public void tratar(EventoEnvelope evento) {
        List<ItemEstoqueInput> itens = new ArrayList<>();
        JsonNode insumos = evento.payload() == null ? null : evento.payload().get("insumos");
        if (insumos != null) {
            for (JsonNode insumo : insumos) {
                itens.add(new ItemEstoqueInput(
                        insumo.path("insumoId").asString(null),
                        insumo.path("quantidade").asInt()));
            }
        }
        useCase.executar(evento.sagaId(), itens);
    }
}
