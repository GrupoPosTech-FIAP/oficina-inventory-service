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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarInsumoUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @InjectMocks
    private CadastrarInsumoUseCase useCase;

    @Test
    void deveCadastrarInsumoComSucesso() {
        Insumos salvo = Insumos.reconstituir("ins-1", "Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");
        when(insumosGateway.existePorNome("Óleo")).thenReturn(false);
        when(insumosGateway.criar(any())).thenReturn(salvo);

        Insumos resultado = useCase.executar("Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");

        assertThat(resultado.getId()).isEqualTo("ins-1");
        assertThat(resultado.getNome()).isEqualTo("Óleo");

        ArgumentCaptor<Insumos> captor = ArgumentCaptor.forClass(Insumos.class);
        verify(insumosGateway).criar(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Óleo");
        assertThat(captor.getValue().getEstoqueAtual()).isEqualTo(10);
        assertThat(captor.getValue().getAtivo()).isTrue();
    }

    @Test
    void deveLancarExcecao_quandoNomeJaCadastrado() {
        when(insumosGateway.existePorNome("Óleo")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar("Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está cadastrado");

        verify(insumosGateway, never()).criar(any());
    }

    @Test
    void deveLancarExcecao_quandoPrecoNegativo() {
        when(insumosGateway.existePorNome("Óleo")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar("Óleo", BigDecimal.valueOf(-1), 10, 2, "LITRO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Preço Unitário");

        verify(insumosGateway, never()).criar(any());
    }
}

