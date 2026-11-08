package com.mecanica.oficina_inventory_service.application.baixa.gateway;

import com.mecanica.oficina_inventory_service.domain.baixa.BaixaOrdemServico;

import java.util.Optional;

public interface BaixaOrdemServicoGateway {
    Optional<BaixaOrdemServico> buscarPorOrdemServico(String ordemServicoId);
    BaixaOrdemServico salvar(BaixaOrdemServico baixa);
}
