package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.NotificarEstoqueBaixoGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.application.insumo.output.AlertaEstoqueBaixo;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import com.mecanica.oficina_inventory_service.domain.insumo.OrigemNotificacaoEstoque;

import java.util.List;

public class BaixarEstoqueUseCase {
    private final InsumosGateway insumosGateway;
    private final NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway;

    public BaixarEstoqueUseCase(InsumosGateway insumosGateway, NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway) {
        this.insumosGateway = insumosGateway;
        this.notificarEstoqueBaixoGateway = notificarEstoqueBaixoGateway;
    }

    public void executar(List<ItemEstoqueInput> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("A lista de itens para baixa não pode ser vazia");
        }

        for (ItemEstoqueInput item : itens) {
            if (item == null || item.insumoId() == null || item.insumoId().isBlank()) {
                throw new IllegalArgumentException("O ID do insumo é obrigatório");
            }
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade para baixa deve ser positiva");
            }

            Insumos insumo = insumosGateway.buscar(item.insumoId())
                    .orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado: " + item.insumoId()));

            int estoqueAnterior = insumo.getEstoqueAtual();
            int estoqueMinimoAnterior = insumo.getEstoqueMinimo();

            insumo.darBaixa(item.quantidade());
            insumosGateway.alterar(insumo.getId(), insumo);

            int novoEstoque = insumo.getEstoqueAtual();

            if (Insumos.deveEmitirAlerta(estoqueAnterior, estoqueMinimoAnterior, novoEstoque, insumo.getEstoqueMinimo())) {
                notificarEstoqueBaixoGateway.notificar(new AlertaEstoqueBaixo(
                        insumo.getId(),
                        insumo.getNome(),
                        estoqueAnterior,
                        novoEstoque,
                        insumo.getEstoqueMinimo(),
                        OrigemNotificacaoEstoque.BAIXA_ORDEM_SERVICO));
            }
        }
    }
}

