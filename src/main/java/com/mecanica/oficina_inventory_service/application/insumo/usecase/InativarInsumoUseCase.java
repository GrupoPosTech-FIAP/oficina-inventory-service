package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;

public class InativarInsumoUseCase {
    private final InsumosGateway insumosGateway;

    public InativarInsumoUseCase(InsumosGateway insumosGateway) {
        this.insumosGateway = insumosGateway;
    }

    public void executar(String id) {
        insumosGateway.buscar(id).orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado"));
        insumosGateway.inativar(id);
    }
}

