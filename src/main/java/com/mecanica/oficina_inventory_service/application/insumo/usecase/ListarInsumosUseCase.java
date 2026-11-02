package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

import java.util.List;

public class ListarInsumosUseCase {
    private final InsumosGateway insumosGateway;

    public ListarInsumosUseCase(InsumosGateway insumosGateway) {
        this.insumosGateway = insumosGateway;
    }

    public List<Insumos> executar() {
        return insumosGateway.listar();
    }
}

