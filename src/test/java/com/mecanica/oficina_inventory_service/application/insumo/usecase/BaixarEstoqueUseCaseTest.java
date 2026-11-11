package com.mecanica.oficina_inventory_service.application.insumo.usecase;

import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.NotificarEstoqueBaixoGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
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
class BaixarEstoqueUseCaseTest {

    @Mock
    private InsumosGateway insumosGateway;

    @Mock
    private NotificarEstoqueBaixoGateway notificarEstoqueBaixoGateway;

    @InjectMocks
    private BaixarEstoqueUseCase useCase;

    @Test
    void deveDarBaixaSemNotificar_quandoEstoquePermaneceAcimaDoMinimo() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo 5W30", BigDecimal.valueOf(50), 10, 2, "L");
        when(insumosGateway.buscarParaAtualizar("ins-1")).thenReturn(Optional.of(insumo));

        useCase.executar(List.of(new ItemEstoqueInput("ins-1", 3)));

        ArgumentCaptor<Insumos> captor = ArgumentCaptor.forClass(Insumos.class);
        verify(insumosGateway).alterar(eq("ins-1"), captor.capture());
        assertThat(captor.getValue().getEstoqueAtual()).isEqualTo(7);
        verify(notificarEstoqueBaixoGateway, never()).notificar(any());
    }

    @Test
    void deveDarBaixaENotificar_quandoEstoqueCruzaParaCritico() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo 5W30", BigDecimal.valueOf(50), 5, 2, "L");
        when(insumosGateway.buscarParaAtualizar("ins-1")).thenReturn(Optional.of(insumo));

        useCase.executar(List.of(new ItemEstoqueInput("ins-1", 4)));

        verify(insumosGateway).alterar(eq("ins-1"), any());

        ArgumentCaptor<AlertaEstoqueBaixo> alertaCaptor = ArgumentCaptor.forClass(AlertaEstoqueBaixo.class);
        verify(notificarEstoqueBaixoGateway).notificar(alertaCaptor.capture());
        AlertaEstoqueBaixo alerta = alertaCaptor.getValue();
        assertThat(alerta.insumoId()).isEqualTo("ins-1");
        assertThat(alerta.estoqueAnterior()).isEqualTo(5);
        assertThat(alerta.estoqueAtual()).isEqualTo(1);
        assertThat(alerta.estoqueMinimo()).isEqualTo(2);
        assertThat(alerta.origem()).isEqualTo(OrigemNotificacaoEstoque.BAIXA_ORDEM_SERVICO);
    }

    @Test
    void deveLancarIllegalStateException_quandoEstoqueInsuficiente() {
        Insumos insumo = Insumos.reconstituir("ins-1", "Óleo 5W30", BigDecimal.valueOf(50), 2, 1, "L");
        when(insumosGateway.buscarParaAtualizar("ins-1")).thenReturn(Optional.of(insumo));

        assertThatThrownBy(() -> useCase.executar(List.of(new ItemEstoqueInput("ins-1", 5))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(insumosGateway, never()).alterar(anyString(), any());
        verify(notificarEstoqueBaixoGateway, never()).notificar(any());
    }

    @Test
    void deveLancarIllegalArgumentException_quandoInsumoNaoEncontrado() {
        when(insumosGateway.buscarParaAtualizar("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(List.of(new ItemEstoqueInput("inexistente", 2))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insumo não encontrado");

        verify(insumosGateway, never()).alterar(anyString(), any());
    }
}

