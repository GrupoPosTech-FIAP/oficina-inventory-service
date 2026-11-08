package com.mecanica.oficina_inventory_service.domain.baixa;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Registro dos insumos baixados para uma ordem de serviço. Guarda o que foi retirado do
 * estoque para que a compensação da Saga devolva exatamente as mesmas quantidades, já que
 * os eventos de falha (pagamento recusado, execução falhou) não trazem a lista de insumos.
 */
public class BaixaOrdemServico {

    private final String ordemServicoId;
    private final List<ItemBaixa> itens;
    private BaixaStatus status;
    private final Instant baixadaEm;
    private Instant estornadaEm;

    private BaixaOrdemServico(String ordemServicoId, List<ItemBaixa> itens, BaixaStatus status,
                              Instant baixadaEm, Instant estornadaEm) {
        this.ordemServicoId = Objects.requireNonNull(ordemServicoId, "O ID da ordem de serviço é obrigatório");
        this.itens = List.copyOf(Objects.requireNonNull(itens, "Itens são obrigatórios"));
        this.status = Objects.requireNonNull(status, "Status é obrigatório");
        this.baixadaEm = Objects.requireNonNull(baixadaEm, "Data da baixa é obrigatória");
        this.estornadaEm = estornadaEm;
    }

    public static BaixaOrdemServico registrar(String ordemServicoId, List<ItemBaixa> itens, Instant agora) {
        return new BaixaOrdemServico(ordemServicoId, itens, BaixaStatus.BAIXADA, agora, null);
    }

    public static BaixaOrdemServico reconstituir(String ordemServicoId, List<ItemBaixa> itens, BaixaStatus status,
                                                 Instant baixadaEm, Instant estornadaEm) {
        return new BaixaOrdemServico(ordemServicoId, itens, status, baixadaEm, estornadaEm);
    }

    public void estornar(Instant agora) {
        if (status == BaixaStatus.ESTORNADA) {
            throw new IllegalStateException("Baixa da ordem de serviço " + ordemServicoId + " já foi estornada");
        }
        this.status = BaixaStatus.ESTORNADA;
        this.estornadaEm = agora;
    }

    public boolean isEstornada() {
        return status == BaixaStatus.ESTORNADA;
    }

    public String getOrdemServicoId() {
        return ordemServicoId;
    }

    public List<ItemBaixa> getItens() {
        return itens;
    }

    public BaixaStatus getStatus() {
        return status;
    }

    public Instant getBaixadaEm() {
        return baixadaEm;
    }

    public Instant getEstornadaEm() {
        return estornadaEm;
    }
}
