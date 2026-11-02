package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class RegistrarCompraUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @InjectMocks
    private RegistrarCompraUseCase useCase;

    @Test
    void deveRegistrarCompraAdicionandoEstoque() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");
        when(insumosGateway.buscar("ins-1")).thenReturn(Optional.of(insumo));
        when(insumosGateway.alterar(eq("ins-1"), any())).thenAnswer(inv -> inv.getArgument(1));

        Insumos resultado = useCase.executar("ins-1", 5);

        assertThat(resultado.getEstoqueAtual()).isEqualTo(15);

        ArgumentCaptor<Insumos> captor = ArgumentCaptor.forClass(Insumos.class);
        verify(insumosGateway).alterar(eq("ins-1"), captor.capture());
        assertThat(captor.getValue().getEstoqueAtual()).isEqualTo(15);
    }

    @Test
    void deveLancarExcecao_quandoQuantidadeNula() {
        assertThatThrownBy(() -> useCase.executar("ins-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantidade");

        verify(insumosGateway, never()).buscar(anyString());
    }

    @Test
    void deveLancarExcecao_quandoQuantidadeZeroOuNegativa() {
        assertThatThrownBy(() -> useCase.executar("ins-1", 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> useCase.executar("ins-1", -3))
                .isInstanceOf(IllegalArgumentException.class);

        verify(insumosGateway, never()).alterar(anyString(), any());
    }

    @Test
    void deveLancarExcecao_quandoInsumoNaoEncontrado() {
        when(insumosGateway.buscar("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("inexistente", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo não encontrado");

        verify(insumosGateway, never()).alterar(anyString(), any());
    }
}

