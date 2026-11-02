package com.mecanica.oficina_inventory_service.application.insumo.gateway;

import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;

import java.util.List;
import java.util.Optional;

public interface InsumosGateway {
    Optional<Insumos> buscar(String id);
    Boolean existePorNome(String nome);
    Boolean existePorId(String id);
    Insumos criar(Insumos insumo);
    List<Insumos> listar();
    Insumos alterar(String id, Insumos insumo);
    Insumos ativar(String id);
    void inativar(String id);
    Integer obterEstoqueAtual(String id);
}

