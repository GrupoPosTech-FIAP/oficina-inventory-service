package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtivarInsumoUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @InjectMocks
    private AtivarInsumoUseCase useCase;

    @Test
    void deveAtivarInsumoComSucesso() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");
        when(insumosGateway.ativar("ins-1")).thenReturn(insumo);

        Insumos resultado = useCase.executar("ins-1");

        assertThat(resultado.getId()).isEqualTo("ins-1");
        verify(insumosGateway).ativar("ins-1");
    }

    @Test
    void deveLancarExcecao_quandoInsumoNaoEncontrado() {
        when(insumosGateway.ativar("inexistente"))
                .thenThrow(new IllegalArgumentException("Insumo não encontrado: inexistente"));

        assertThatThrownBy(() -> useCase.executar("inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo não encontrado");
    }
}
