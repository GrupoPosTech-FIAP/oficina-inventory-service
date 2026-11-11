package com.mecanica.oficina_inventory_service.adapters.persistence.repository;

import com.mecanica.oficina_inventory_service.adapters.persistence.InsumosJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InsumosSpringDataRepository extends JpaRepository<InsumosJpaEntity, String> {
    Optional<InsumosJpaEntity> findByNome(String nome);

    Optional<InsumosJpaEntity> findByIdAndAtivoTrue(String id);

    /** {@code SELECT ... FOR UPDATE}: segura a linha até o fim da transação de quem chamou. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InsumosJpaEntity i where i.id = :id and i.ativo = true")
    Optional<InsumosJpaEntity> travarAtivoPorId(@Param("id") String id);

    List<InsumosJpaEntity> findAllByAtivoTrue();

    Boolean existsByAtivoTrueAndNome(String nome);

    Boolean existsByIdAndAtivoTrue(String id);
}

