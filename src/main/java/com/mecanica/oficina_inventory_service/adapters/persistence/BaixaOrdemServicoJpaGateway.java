package com.mecanica.oficina_inventory_service.adapters.persistence;

import com.mecanica.oficina_inventory_service.adapters.persistence.repository.BaixaOrdemServicoSpringDataRepository;
import com.mecanica.oficina_inventory_service.application.baixa.gateway.BaixaOrdemServicoGateway;
import com.mecanica.oficina_inventory_service.domain.baixa.BaixaOrdemServico;
import com.mecanica.oficina_inventory_service.domain.baixa.BaixaStatus;
import com.mecanica.oficina_inventory_service.domain.baixa.ItemBaixa;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public class BaixaOrdemServicoJpaGateway implements BaixaOrdemServicoGateway {

    private final BaixaOrdemServicoSpringDataRepository repo;

    public BaixaOrdemServicoJpaGateway(BaixaOrdemServicoSpringDataRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<BaixaOrdemServico> buscarPorOrdemServico(String ordemServicoId) {
        return repo.findById(ordemServicoId).map(this::toDomain);
    }

    @Override
    public BaixaOrdemServico salvar(BaixaOrdemServico baixa) {
        return toDomain(repo.save(toEntity(baixa)));
    }

    private BaixaOrdemServico toDomain(BaixaOrdemServicoJpaEntity e) {
        return BaixaOrdemServico.reconstituir(
                e.getOrdemServicoId(),
                e.getItens().stream().map(i -> new ItemBaixa(i.getInsumoId(), i.getQuantidade())).toList(),
                BaixaStatus.valueOf(e.getStatus()),
                e.getBaixadaEm(),
                e.getEstornadaEm());
    }

    private BaixaOrdemServicoJpaEntity toEntity(BaixaOrdemServico baixa) {
        BaixaOrdemServicoJpaEntity e = new BaixaOrdemServicoJpaEntity();
        e.setOrdemServicoId(baixa.getOrdemServicoId());
        e.setStatus(baixa.getStatus().name());
        e.setBaixadaEm(baixa.getBaixadaEm());
        e.setEstornadaEm(baixa.getEstornadaEm());
        e.setItens(new ArrayList<>(baixa.getItens().stream()
                .map(i -> new BaixaOrdemServicoJpaEntity.Item(i.insumoId(), i.quantidade()))
                .toList()));
        return e;
    }
}
