package com.mecanica.oficina_inventory_service.domain.baixa;

import java.util.Objects;

public record ItemBaixa(String insumoId, int quantidade) {

    public ItemBaixa {
        Objects.requireNonNull(insumoId, "O ID do insumo é obrigatório");
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade para baixa deve ser positiva");
        }
    }
}
