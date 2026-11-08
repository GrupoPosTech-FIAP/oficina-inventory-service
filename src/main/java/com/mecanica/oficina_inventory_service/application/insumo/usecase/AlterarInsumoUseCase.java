package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.NotificarEstoqueBaixoGateway;
import com.mecanica.oficina_inventory_service.application.insumo.output.AlertaEstoqueBaixo;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import com.mecanica.oficina_inventory_service.domain.insumo.OrigemNotificacaoEstoque;

import java.math.BigDecimal;

public class AlterarInsumoUseCase {
    private final InsumosGateway insumosGateway;
    private final NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway;

    public AlterarInsumoUseCase(InsumosGateway insumosGateway, NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway) {
        this.insumosGateway = insumosGateway;
        this.notificarEstoqueBaixoGateway = notificarEstoqueBaixoGateway;
    }

    public void executar(String id, String nome, BigDecimal precoUnitario, Integer estoqueAtual, Integer estoqueMinimo, String unidade) {
        // trava o insumo até o commit, para não sobrescrever uma baixa ou compra em andamento
        int estoqueAnterior = insumosGateway.buscarParaAtualizar(id)
                .orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado"))
                .getEstoqueAtual();

        var insumos = Insumos.reconstituir(
                id,
                nome,
                precoUnitario,
                estoqueAtual,
                estoqueMinimo,
                unidade
        );

        insumosGateway.alterar(id, insumos);

        var notificarEstoqueBaixo = Insumos.deveNotificarAlteracaoInsumo(estoqueAtual, estoqueMinimo);

        if (notificarEstoqueBaixo) {
            notificarEstoqueBaixoGateway.notificar(new AlertaEstoqueBaixo(
                    id,
                    nome,
                    estoqueAnterior,
                    estoqueAtual,
                    estoqueMinimo,
                    OrigemNotificacaoEstoque.ALTERACAO_INSUMO));
        }
    }
}

