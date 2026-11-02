package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

import java.math.BigDecimal;

public class CadastrarInsumoUseCase {
    private final InsumosGateway insumosGateway;

    public CadastrarInsumoUseCase(InsumosGateway insumosGateway) {
        this.insumosGateway = insumosGateway;
    }

    public Insumos executar(String nome, BigDecimal precoUnitario, Integer estoqueAtual, Integer estoqueMinimo, String unidade) {
        if (insumosGateway.existePorNome(nome)) {
            throw new IllegalArgumentException("O Insumo " + nome + " já está cadastrado");
        }

        Insumos insumo = Insumos.criar(nome, precoUnitario, estoqueAtual, estoqueMinimo, unidade);

        return insumosGateway.criar(insumo);
    }
}

