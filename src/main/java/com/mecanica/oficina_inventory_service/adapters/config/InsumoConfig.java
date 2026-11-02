package com.mecanica.oficina_inventory_service.adapters.config;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.NotificarEstoqueBaixoGateway;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.AlterarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.AtivarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.BaixarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.CadastrarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.ConsultarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.EstornarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.InativarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.ListarInsumosUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.RegistrarCompraUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InsumoConfig {

    @Bean
    public CadastrarInsumoUseCase cadastrarInsumoUseCase(InsumosGateway insumosGateway) {
        return new CadastrarInsumoUseCase(insumosGateway);
    }

    @Bean
    public ConsultarInsumoUseCase consultarInsumoUseCase(InsumosGateway insumosGateway) {
        return new ConsultarInsumoUseCase(insumosGateway);
    }

    @Bean
    public ListarInsumosUseCase listarInsumosUseCase(InsumosGateway insumosGateway) {
        return new ListarInsumosUseCase(insumosGateway);
    }

    @Bean
    public AlterarInsumoUseCase alterarInsumoUseCase(InsumosGateway insumosGateway, NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway) {
        return new AlterarInsumoUseCase(insumosGateway, notificarEstoqueBaixoGateway);
    }

    @Bean
    public AtivarInsumoUseCase ativarInsumoUseCase(InsumosGateway insumosGateway) {
        return new AtivarInsumoUseCase(insumosGateway);
    }

    @Bean
    public InativarInsumoUseCase inativarInsumoUseCase(InsumosGateway insumosGateway) {
        return new InativarInsumoUseCase(insumosGateway);
    }

    @Bean
    public RegistrarCompraUseCase registrarCompraUseCase(InsumosGateway insumosGateway) {
        return new RegistrarCompraUseCase(insumosGateway);
    }

    @Bean
    public BaixarEstoqueUseCase baixarEstoqueUseCase(InsumosGateway insumosGateway, NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway) {
        return new BaixarEstoqueUseCase(insumosGateway, notificarEstoqueBaixoGateway);
    }

    @Bean
    public EstornarEstoqueUseCase estornarEstoqueUseCase(InsumosGateway insumosGateway) {
        return new EstornarEstoqueUseCase(insumosGateway);
    }
}

