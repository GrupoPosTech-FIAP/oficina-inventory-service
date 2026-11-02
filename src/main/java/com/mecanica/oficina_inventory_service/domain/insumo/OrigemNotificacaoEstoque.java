package com.mecanica.oficina_inventory_service.domain.insumo;

/**
 * Origem do alerta — útil para roteamento futuro (ex.: e-mail só para baixa em OS).
 */
public enum OrigemNotificacaoEstoque {
    BAIXA_ORDEM_SERVICO,
    ALTERACAO_INSUMO
}

