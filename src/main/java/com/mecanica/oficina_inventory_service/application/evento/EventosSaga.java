package com.mecanica.oficina_inventory_service.application.evento;

/** Nomes dos eventos da Saga (catálogo da ADR 005 em oficina-app-api) usados por este serviço. */
public final class EventosSaga {

    // Consumidos
    public static final String ORCAMENTO_APROVADO = "OrcamentoAprovado";
    public static final String PAGAMENTO_RECUSADO = "PagamentoRecusado";
    public static final String PAGAMENTO_EXPIRADO = "PagamentoExpirado";
    public static final String EXECUCAO_FALHOU = "ExecucaoFalhou";

    // Publicados
    public static final String ESTOQUE_BAIXADO = "EstoqueBaixado";
    public static final String ESTOQUE_INSUFICIENTE = "EstoqueInsuficiente";
    public static final String ESTOQUE_ESTORNADO = "EstoqueEstornado";

    private EventosSaga() {
    }
}
