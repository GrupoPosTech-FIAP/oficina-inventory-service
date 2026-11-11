package com.mecanica.oficina_inventory_service.adapters.persistence.repository;

import com.mecanica.oficina_inventory_service.adapters.persistence.EventoProcessadoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoProcessadoSpringDataRepository extends JpaRepository<EventoProcessadoJpaEntity, String> {
}
