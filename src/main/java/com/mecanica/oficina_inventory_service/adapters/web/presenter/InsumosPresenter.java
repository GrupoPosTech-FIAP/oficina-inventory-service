package com.mecanica.oficina_inventory_service.adapters.web.presenter;

import com.mecanica.oficina_inventory_service.adapters.web.dto.response.InsumosResponse;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.springframework.stereotype.Component;

import java.util.List;

/** Converte a entidade de domínio {@link Insumos} em DTOs de resposta HTTP. */
@Component
public class InsumosPresenter {

    public InsumosResponse apresentar(Insumos insumo) {
        return new InsumosResponse(
                insumo.getId(),
                insumo.getNome(),
                insumo.getPrecoUnitario(),
                insumo.getEstoqueAtual(),
                insumo.getEstoqueMinimo(),
                insumo.getUnidade(),
                insumo.getAtivo());
    }

    public List<InsumosResponse> apresentar(List<Insumos> insumos) {
        return insumos.stream().map(this::apresentar).toList();
    }
}

