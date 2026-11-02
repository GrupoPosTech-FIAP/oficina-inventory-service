package com.mecanica.oficina_inventory_service.application.insumo.input;

public record ItemEstoqueInput(
        String insumoId,
        Integer quantidade
) {
}

