package com.mecanica.oficina_inventory_service.application.baixa.usecase;

import com.mecanica.oficina_inventory_service.application.baixa.gateway.BaixaOrdemServicoGateway;
import com.mecanica.oficina_inventory_service.application.evento.gateway.PublicadorEventosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.gateway.InsumosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.BaixarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.domain.baixa.BaixaOrdemServico;
import com.mecanica.oficina_inventory_service.domain.baixa.BaixaStatus;
import com.mecanica.oficina_inventory_service.domain.baixa.ItemBaixa;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BaixarEstoqueOrdemServicoUseCaseTest {

    private static final Instant AGORA = Instant.parse("2026-11-08T10:00:00Z");

    private InsumosGateway insumosGateway;
    private BaixaOrdemServicoGateway baixaGateway;
    private BaixarEstoqueUseCase baixarEstoqueUseCase;
    private PublicadorEventosGateway publicador;
    private BaixarEstoqueOrdemServicoUseCase useCase;

    @BeforeEach
    void setUp() {
        insumosGateway = mock(InsumosGateway.class);
        baixaGateway = mock(BaixaOrdemServicoGateway.class);
        baixarEstoqueUseCase = mock(BaixarEstoqueUseCase.class);
        publicador = mock(PublicadorEventosGateway.class);
        useCase = new BaixarEstoqueOrdemServicoUseCase(insumosGateway, baixaGateway, baixarEstoqueUseCase,
                publicador, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    private static Insumos insumo(String id, int estoque) {
        return Insumos.reconstituir(id, "Insumo " + id, BigDecimal.TEN, estoque, 1, "UN");
    }

    @Test
    @SuppressWarnings("unchecked")
    void baixaTudoRegistraEPublicaEstoqueBaixadoConsolidandoInsumosRepetidos() {
        when(baixaGateway.buscarPorOrdemServico("os-1")).thenReturn(Optional.empty());
        when(insumosGateway.buscarParaAtualizar("ins-1")).thenReturn(Optional.of(insumo("ins-1", 10)));
        when(insumosGateway.buscarParaAtualizar("ins-2")).thenReturn(Optional.of(insumo("ins-2", 1)));

        useCase.executar("os-1", List.of(
                new ItemEstoqueInput("ins-1", 2),
                new ItemEstoqueInput("ins-2", 1),
                new ItemEstoqueInput("ins-1", 3)));

        verify(baixarEstoqueUseCase).executar(List.of(
                new ItemEstoqueInput("ins-1", 5),
                new ItemEstoqueInput("ins-2", 1)));

        ArgumentCaptor<BaixaOrdemServico> baixa = ArgumentCaptor.forClass(BaixaOrdemServico.class);
        verify(baixaGateway).salvar(baixa.capture());
        assertThat(baixa.getValue().getOrdemServicoId()).isEqualTo("os-1");
        assertThat(baixa.getValue().getStatus()).isEqualTo(BaixaStatus.BAIXADA);
        assertThat(baixa.getValue().getBaixadaEm()).isEqualTo(AGORA);
        assertThat(baixa.getValue().getItens()).containsExactly(new ItemBaixa("ins-1", 5), new ItemBaixa("ins-2", 1));

        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(publicador).publicar(eq("EstoqueBaixado"), eq("os-1"), payload.capture());
        assertThat(payload.getValue()).containsEntry("ordemServicoId", "os-1");
        assertThat((List<Map<String, Object>>) payload.getValue().get("itens")).containsExactly(
                Map.of("insumoId", "ins-1", "quantidade", 5),
                Map.of("insumoId", "ins-2", "quantidade", 1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void saldoInsuficienteNaoBaixaNadaEPublicaEstoqueInsuficiente() {
        when(baixaGateway.buscarPorOrdemServico("os-1")).thenReturn(Optional.empty());
        when(insumosGateway.buscarParaAtualizar("ins-1")).thenReturn(Optional.of(insumo("ins-1", 10)));
        when(insumosGateway.buscarParaAtualizar("ins-2")).thenReturn(Optional.of(insumo("ins-2", 1)));
        when(insumosGateway.buscarParaAtualizar("ins-x")).thenReturn(Optional.empty());

        useCase.executar("os-1", List.of(
                new ItemEstoqueInput("ins-1", 2),
                new ItemEstoqueInput("ins-2", 4),
                new ItemEstoqueInput("ins-x", 1)));

        verifyNoInteractions(baixarEstoqueUseCase);
        verify(baixaGateway, never()).salvar(any());

        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(publicador).publicar(eq("EstoqueInsuficiente"), eq("os-1"), payload.capture());
        assertThat((List<Map<String, Object>>) payload.getValue().get("itens")).containsExactly(
                Map.of("insumoId", "ins-2", "solicitado", 4, "disponivel", 1),
                Map.of("insumoId", "ins-x", "solicitado", 1, "disponivel", 0));
    }

    @Test
    void semInsumosPublicaEstoqueBaixadoComListaVazia() {
        when(baixaGateway.buscarPorOrdemServico("os-1")).thenReturn(Optional.empty());

        useCase.executar("os-1", null);

        verifyNoInteractions(baixarEstoqueUseCase);
        verify(baixaGateway).salvar(any());
        verify(publicador).publicar("EstoqueBaixado", "os-1", Map.of("ordemServicoId", "os-1", "itens", List.of()));
    }

    @Test
    void ordemServicoJaBaixadaNaoFazNada() {
        when(baixaGateway.buscarPorOrdemServico("os-1"))
                .thenReturn(Optional.of(BaixaOrdemServico.registrar("os-1", List.of(), AGORA)));

        useCase.executar("os-1", List.of(new ItemEstoqueInput("ins-1", 1)));

        verifyNoInteractions(insumosGateway, baixarEstoqueUseCase, publicador);
        verify(baixaGateway, never()).salvar(any());
    }

    @Test
    void itemSemInsumoOuComQuantidadeInvalidaELancaErro() {
        when(baixaGateway.buscarPorOrdemServico(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar("os-1", List.of(new ItemEstoqueInput(" ", 1))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> useCase.executar("os-1", List.of(new ItemEstoqueInput("ins-1", 0))))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(publicador);
    }
}
