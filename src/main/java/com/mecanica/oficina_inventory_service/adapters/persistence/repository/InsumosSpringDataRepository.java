package com.mecanica.oficina_inventory_service.adapters.persistence.repository;

import com.mecanica.oficina_inventory_service.adapters.persistence.InsumosJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InsumosSpringDataRepository extends JpaRepository<InsumosJpaEntity, String> {
    Optional<InsumosJpaEntity> findByNome(String nome);

    Optional<InsumosJpaEntity> findByIdAndAtivoTrue(String id);

    List<InsumosJpaEntity> findAllByAtivoTrue();

    Boolean existsByAtivoTrueAndNome(String nome);

    Boolean existsByIdAndAtivoTrue(String id);
}

