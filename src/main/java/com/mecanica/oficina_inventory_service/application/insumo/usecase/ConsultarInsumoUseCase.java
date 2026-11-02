package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

public class ConsultarInsumoUseCase {
    private final InsumosGateway insumosGateway;

    public ConsultarInsumoUseCase(InsumosGateway insumosGateway) {
        this.insumosGateway = insumosGateway;
    }

    public Insumos executar(String id) {
        return insumosGateway.buscar(id).orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado"));
    }
}

