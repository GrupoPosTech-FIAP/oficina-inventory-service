package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstornarEstoqueUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @InjectMocks
    private EstornarEstoqueUseCase useCase;

    @Test
    void deveEstornarEstoqueEmLoteComSucesso() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo 5W30", BigDecimal.valueOf(50), 7, 2, "L");
        when(insumosGateway.buscar("ins-1")).thenReturn(Optional.of(insumo));

        useCase.executar(List.of(new ItemEstoqueInput("ins-1", 3)));

        ArgumentCaptor<Insumos> captor = ArgumentCaptor.forClass(Insumos.class);
        verify(insumosGateway).alterar(eq("ins-1"), captor.capture());
        assertThat(captor.getValue().getEstoqueAtual()).isEqualTo(10);
    }

    @Test
    void deveLancarIllegalArgumentException_quandoInsumoNaoEncontrado() {
        when(insumosGateway.buscar("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(List.of(new ItemEstoqueInput("inexistente", 2))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo não encontrado");

        verify(insumosGateway, never()).alterar(anyString(), any());
    }
}

