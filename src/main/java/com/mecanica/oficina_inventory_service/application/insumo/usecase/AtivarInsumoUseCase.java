package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

public class AtivarInsumoUseCase {
    private final InsumosGateway insumosGateway;

    public AtivarInsumoUseCase(InsumosGateway insumosGateway) {
        this.insumosGateway = insumosGateway;
    }

    public Insumos executar(String id) {
        return insumosGateway.ativar(id);
    }
}
