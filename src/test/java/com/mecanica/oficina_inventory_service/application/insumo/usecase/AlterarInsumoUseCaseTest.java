package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.NotificarEstoqueBaixoGateway;
import com.mecanica.oficina_inventory_service.application.insumo.output.AlertaEstoqueBaixo;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import com.mecanica.oficina_inventory_service.domain.insumo.OrigemNotificacaoEstoque;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterarInsumoUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @Mock
    private NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway;

    @InjectMocks
    private AlterarInsumoUseCase useCase;

    @Test
    void deveAlterarInsumoSemNotificar_quandoEstoqueAcimaDoMinimo() {
        when(insumosGateway.existePorId("ins-1")).thenReturn(true);
        when(insumosGateway.obterEstoqueAtual("ins-1")).thenReturn(10);

        useCase.executar("ins-1", "Filtro", BigDecimal.valueOf(30), 5, 1, "UN");

        ArgumentCaptor<Insumos> captor = ArgumentCaptor.forClass(Insumos.class);
        verify(insumosGateway).alterar(eq("ins-1"), captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Filtro");
        assertThat(captor.getValue().getEstoqueAtual()).isEqualTo(5);
        verify(notificarEstoqueBaixoGateway, never()).notificar(any());
    }

    @Test
    void deveNotificarEstoqueBaixo_quandoEstoqueFicaAbaixoDoMinimo() {
        when(insumosGateway.existePorId("ins-1")).thenReturn(true);
        when(insumosGateway.obterEstoqueAtual("ins-1")).thenReturn(10);

        useCase.executar("ins-1", "Óleo", BigDecimal.valueOf(30), 3, 5, "L");

        ArgumentCaptor<AlertaEstoqueBaixo> captor = ArgumentCaptor.forClass(AlertaEstoqueBaixo.class);
        verify(notificarEstoqueBaixoGateway).notificar(captor.capture());
        AlertaEstoqueBaixo alerta = captor.getValue();
        assertThat(alerta.insumoId()).isEqualTo("ins-1");
        assertThat(alerta.estoqueAnterior()).isEqualTo(10);
        assertThat(alerta.estoqueAtual()).isEqualTo(3);
        assertThat(alerta.estoqueMinimo()).isEqualTo(5);
        assertThat(alerta.origem()).isEqualTo(OrigemNotificacaoEstoque.ALTERACAO_INSUMO);
    }

    @Test
    void deveNotificarEstoqueBaixo_quandoEstoqueIgualAoMinimo() {
        when(insumosGateway.existePorId("ins-1")).thenReturn(true);
        when(insumosGateway.obterEstoqueAtual("ins-1")).thenReturn(2);

        useCase.executar("ins-1", "Óleo", BigDecimal.valueOf(30), 5, 5, "L");

        verify(notificarEstoqueBaixoGateway, times(1)).notificar(any(AlertaEstoqueBaixo.class));
    }

    @Test
    void deveLancarExcecao_quandoInsumoNaoEncontrado() {
        when(insumosGateway.existePorId("inexistente")).thenReturn(false);

        assertThatThrownBy(() ->
                useCase.executar("inexistente", "Filtro", BigDecimal.valueOf(30), 5, 1, "UN")
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo não encontrado");

        verify(insumosGateway, never()).alterar(anyString(), any());
        verify(notificarEstoqueBaixoGateway, never()).notificar(any());
    }
}

