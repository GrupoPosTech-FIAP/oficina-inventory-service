package com.mecanica.oficina_inventory_service.application.insumo.gateway;

import com.mecanica.oficina_inventory_service.application.insumo.output.AlertaEstoqueBaixo;

public interface NotificarEstoqueBaixoGateway {

    void notificar(AlertaEstoqueBaixo alerta);
}

