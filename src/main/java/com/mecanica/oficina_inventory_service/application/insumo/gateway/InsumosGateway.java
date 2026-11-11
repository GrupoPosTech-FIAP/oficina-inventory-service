package com.mecanica.oficina_inventory_service.application.insumo.gateway;

import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

import java.util.List;
import java.util.Optional;

public interface InsumosGateway {
    Optional<Insumos> buscar(String id);

    /**
     * Busca o insumo ativo para alterar o saldo, travando-o até o fim da transação corrente: outra
     * baixa, estorno ou compra do mesmo insumo espera, em vez de sobrescrever o saldo lido aqui.
     * Exige transação aberta. Quem trava vários insumos deve travá-los em ordem crescente de id,
     * para duas transações nunca ficarem esperando uma pela outra (deadlock).
     */
    Optional<Insumos> buscarParaAtualizar(String id);
    Boolean existePorNome(String nome);
    Boolean existePorId(String id);
    Insumos criar(Insumos insumo);
    List<Insumos> listar();
    Insumos alterar(String id, Insumos insumo);
    Insumos ativar(String id);
    void inativar(String id);
    Integer obterEstoqueAtual(String id);
}

