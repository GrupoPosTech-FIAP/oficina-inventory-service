package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarInsumosUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @InjectMocks
    private ListarInsumosUseCase useCase;

    @Test
    void deveRetornarListaDeInsumos() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");
        when(insumosGateway.listar()).thenReturn(List.of(insumo));

        List<Insumos> resultado = useCase.executar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNome()).isEqualTo("Óleo");
    }

    @Test
    void deveRetornarListaVazia_quandoNaoHaInsumos() {
        when(insumosGateway.listar()).thenReturn(List.of());

        List<Insumos> resultado = useCase.executar();

        assertThat(resultado).isEmpty();
    }
}

