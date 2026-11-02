package com.mecanica.oficina_inventory_service.adapters.notification;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.NotificarEstoqueBaixoGateway;
import com.mecanica.oficina_inventory_service.application.insumo.output.AlertaEstoqueBaixo;
import com.mecanica.oficina_inventory_service.domain.insumo.OrigemNotificacaoEstoque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Log em uma única linha com campos fixos (parseável por grep/Loki).
 * O padrão default do Spring/Logback não imprime {@code addKeyValue}; por isso os dados vão na mensagem.
 */
@Component
public class LogNotificadorEstoqueBaixo implements NotificarEstoqueBaixoGateway {

    private static final Logger log = LoggerFactory.getLogger(LogNotificadorEstoqueBaixo.class);

    @Override
    public void notificar(AlertaEstoqueBaixo a) {
        log.warn(
                "[ALERTA_ESTOQUE_MINIMO] tipo_origem={} insumo_id={} nome_insumo={} estoque_anterior={} "
                        + "estoque_atual={} estoque_minimo={} referencia_origem={}",
                tipoOrigemLegivel(a.origem()),
                a.insumoId(),
                a.nomeInsumo(),
                a.estoqueAnterior(),
                a.estoqueAtual(),
                a.estoqueMinimo());
    }

    /** Texto estável para filtro em log (ex.: {@code tipo_origem=baixa_os}). */
    private static String tipoOrigemLegivel(OrigemNotificacaoEstoque origem) {
        return switch (origem) {
            case BAIXA_ORDEM_SERVICO -> "baixa_os";
            case ALTERACAO_INSUMO -> "alteracao_insumo";
        };
    }
}

