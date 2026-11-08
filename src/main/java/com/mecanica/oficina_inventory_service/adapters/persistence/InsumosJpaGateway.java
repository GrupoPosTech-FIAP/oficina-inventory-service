package com.mecanica.oficina_inventory_service.adapters.persistence;

import com.mecanica.oficina_inventory_service.adapters.persistence.repository.InsumosSpringDataRepository;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InsumosJpaGateway implements InsumosGateway {

    private final InsumosSpringDataRepository repo;

    public InsumosJpaGateway(InsumosSpringDataRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<Insumos> buscar(String id) {
        return repo.findByIdAndAtivoTrue(id).map(this::toDomain);
    }

    @Override
    public Optional<Insumos> buscarParaAtualizar(String id) {
        return repo.travarAtivoPorId(id).map(this::toDomain);
    }

    @Override
    public Boolean existePorNome(String nome) {
        return repo.existsByAtivoTrueAndNome(nome);
    }

    @Override
    public Boolean existePorId(String id) {
        return repo.existsByIdAndAtivoTrue(id);
    }

    @Override
    public Insumos criar(Insumos insumo) {
        return toDomain(repo.save(toEntity(insumo)));
    }

    @Override
    public List<Insumos> listar() {
        return repo.findAllByAtivoTrue().stream().map(this::toDomain).toList();
    }

    @Override
    public Insumos alterar(String id, Insumos insumo) {
        return repo.findByIdAndAtivoTrue(id).map(e -> {
            e.setNome(insumo.getNome());
            e.setPrecoUnitario(insumo.getPrecoUnitario());
            e.setEstoqueAtual(insumo.getEstoqueAtual());
            e.setEstoqueMinimo(insumo.getEstoqueMinimo());
            e.setUnidade(insumo.getUnidade());
            return toDomain(repo.save(e));
        }).orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado: " + id));
    }

    @Override
    public Insumos ativar(String id) {
        return repo.findById(id).map(e -> {
            e.setAtivo(true);
            return toDomain(repo.save(e));
        }).orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado: " + id));
    }

    @Override
    public void inativar(String id) {
        repo.findById(id).ifPresent(e -> {
            e.setAtivo(false);
            repo.save(e);
        });
    }

    @Override
    public Integer obterEstoqueAtual(String id) {
        return repo.findByIdAndAtivoTrue(id)
                .map(InsumosJpaEntity::getEstoqueAtual)
                .orElseThrow(() -> new IllegalArgumentException("Insumo não encontrado: " + id));
    }

    private Insumos toDomain(InsumosJpaEntity e) {
        Insumos insumo = Insumos.reconstituir(
                e.getId(),
                e.getNome(),
                e.getPrecoUnitario(),
                e.getEstoqueAtual(),
                e.getEstoqueMinimo(),
                e.getUnidade()
        );
        if (Boolean.TRUE.equals(e.getAtivo())) {
            insumo.ativar();
        } else {
            insumo.inativar();
        }
        return insumo;
    }

    private InsumosJpaEntity toEntity(Insumos insumo) {
        InsumosJpaEntity e = new InsumosJpaEntity();
        e.setId(insumo.getId());
        e.setNome(insumo.getNome());
        e.setPrecoUnitario(insumo.getPrecoUnitario());
        e.setEstoqueAtual(insumo.getEstoqueAtual());
        e.setEstoqueMinimo(insumo.getEstoqueMinimo());
        e.setUnidade(insumo.getUnidade());
        e.setAtivo(insumo.getAtivo());
        return e;
    }
}

