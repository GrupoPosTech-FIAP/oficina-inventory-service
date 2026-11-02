package com.mecanica.oficina_inventory_service.application.insumo.output;

import com.mecanica.oficina_inventory_service.domain.insumo.OrigemNotificacaoEstoque;

/**
 * Carga útil do alerta — campos pensados para template de e-mail futuro.
 */
public record AlertaEstoqueBaixo(
        String insumoId,
        String nomeInsumo,
        int estoqueAnterior,
        int estoqueAtual,
        int estoqueMinimo,
        OrigemNotificacaoEstoque origem) {
}

