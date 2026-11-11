package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

import java.util.List;

public class EstornarEstoqueUseCase {
    private final InsumosGateway insumosGateway;

    public EstornarEstoqueUseCase(InsumosGateway insumosGateway) {
        this.insumosGateway = insumosGateway;
    }

    /** Deve rodar numa transação: cada insumo fica travado até o commit (ver {@link InsumosGateway#buscarParaAtualizar}). */
    public void executar(List<ItemEstoqueInput> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("A lista de itens para estorno não pode ser vazia");
        }

        for (ItemEstoqueInput item : itens) {
            if (item == null || item.insumoId() == null || item.insumoId().isBlank()) {
                throw new IllegalArgumentException("O ID do insumo é obrigatório");
            }
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser informada e ser um inteiro positivo");
            }
        }

        for (ItemEstoqueInput item : BaixarEstoqueUseCase.emOrdemDeTrava(itens)) {
            Insumos insumo = insumosGateway.buscarParaAtualizar(item.insumoId())
                    .orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado: " + item.insumoId()));

            insumo.adicionarEstoque(item.quantidade());
            insumosGateway.alterar(insumo.getId(), insumo);
        }
    }
}

