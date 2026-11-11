package com.mecanica.oficina_inventory_service.adapters.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Insumos baixados por ordem de serviço — base para o estorno da Saga. */
@Data
@Entity
@NoArgsConstructor
@Table(name = "baixas_ordem_servico")
public class BaixaOrdemServicoJpaEntity {

  @Id
  private String ordemServicoId;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private Instant baixadaEm;

  private Instant estornadaEm;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "baixas_ordem_servico_itens", joinColumns = @JoinColumn(name = "ordem_servico_id"))
  private List<Item> itens = new ArrayList<>();

  @Data
  @Embeddable
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Item {
    @Column(nullable = false)
    private String insumoId;

    @Column(nullable = false)
    private int quantidade;
  }
}
