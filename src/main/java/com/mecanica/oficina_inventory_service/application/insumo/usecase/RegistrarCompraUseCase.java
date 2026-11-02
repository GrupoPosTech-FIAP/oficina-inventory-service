package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

public class RegistrarCompraUseCase {
    private final InsumosGateway insumosGateway;

    public RegistrarCompraUseCase(InsumosGateway insumosGateway) {
        this.insumosGateway = insumosGateway;
    }

    public Insumos executar(String id, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser informada e ser um inteiro positivo");
        }

        var insumo = insumosGateway.buscar(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado"));

        insumo.adicionarEstoque(quantidade);

        return insumosGateway.alterar(id, insumo);
    }
}

