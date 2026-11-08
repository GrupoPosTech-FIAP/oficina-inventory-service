package com.mecanica.oficina_inventory_service.adapters.persistence.repository;

import com.mecanica.oficina_inventory_service.adapters.persistence.BaixaOrdemServicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaixaOrdemServicoSpringDataRepository extends JpaRepository<BaixaOrdemServicoJpaEntity, String> {
}
