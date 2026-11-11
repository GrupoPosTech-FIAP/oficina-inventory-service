package com.mecanica.oficina_inventory_service.adapters.persistence.repository;

import com.mecanica.oficina_inventory_service.adapters.persistence.OutboxEventoJpaEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventoSpringDataRepository extends JpaRepository<OutboxEventoJpaEntity, String> {

  List<OutboxEventoJpaEntity> findByPublicadoEmIsNullOrderByCriadoEmAsc(Pageable pageable);

}
