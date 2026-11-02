package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarInsumoUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @InjectMocks
    private ConsultarInsumoUseCase useCase;

    @Test
    void deveRetornarInsumo_quandoEncontrado() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");
        when(insumosGateway.buscar("ins-1")).thenReturn(Optional.of(insumo));

        Insumos resultado = useCase.executar("ins-1");

        assertThat(resultado.getId()).isEqualTo("ins-1");
        assertThat(resultado.getNome()).isEqualTo("Óleo");
    }

    @Test
    void deveLancarExcecao_quandoInsumoNaoEncontrado() {
        when(insumosGateway.buscar("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo não encontrado");
    }
}

