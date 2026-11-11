package com.mecanica.oficina_inventory_service.application.baixa.usecase;

import com.mecanica.oficina_inventory_service.application.baixa.gateway.BaixaOrdemServicoGateway;
import com.mecanica.oficina_inventory_service.application.evento.gateway.PublicadorEventosGateway;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.EstornarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.domain.baixa.BaixaOrdemServico;
import com.mecanica.oficina_inventory_service.domain.baixa.ItemBaixa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EstornarEstoqueOrdemServicoUseCaseTest {

    private static final Instant BAIXADA_EM = Instant.parse("2026-11-08T10:00:00Z");
    private static final Instant AGORA = Instant.parse("2026-11-08T11:00:00Z");

    private BaixaOrdemServicoGateway baixaGateway;
    private EstornarEstoqueUseCase estornarEstoqueUseCase;
    private PublicadorEventosGateway publicador;
    private EstornarEstoqueOrdemServicoUseCase useCase;

    @BeforeEach
    void setUp() {
        baixaGateway = mock(BaixaOrdemServicoGateway.class);
        estornarEstoqueUseCase = mock(EstornarEstoqueUseCase.class);
        publicador = mock(PublicadorEventosGateway.class);
        useCase = new EstornarEstoqueOrdemServicoUseCase(baixaGateway, estornarEstoqueUseCase, publicador,
                Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void devolveOsInsumosMarcaEstornadaEPublicaEstoqueEstornado() {
        BaixaOrdemServico baixa = BaixaOrdemServico.registrar("os-1",
                List.of(new ItemBaixa("ins-1", 5), new ItemBaixa("ins-2", 1)), BAIXADA_EM);
        when(baixaGateway.buscarPorOrdemServico("os-1")).thenReturn(Optional.of(baixa));

        assertThat(useCase.executar("os-1", "PagamentoRecusado")).isTrue();

        verify(estornarEstoqueUseCase).executar(List.of(
                new ItemEstoqueInput("ins-1", 5),
                new ItemEstoqueInput("ins-2", 1)));
        ArgumentCaptor<BaixaOrdemServico> salva = ArgumentCaptor.forClass(BaixaOrdemServico.class);
        verify(baixaGateway).salvar(salva.capture());
        assertThat(salva.getValue().isEstornada()).isTrue();
        assertThat(salva.getValue().getEstornadaEm()).isEqualTo(AGORA);
        verify(publicador).publicar("EstoqueEstornado", "os-1", Map.of(
                "ordemServicoId", "os-1",
                "motivo", "PagamentoRecusado",
                "itens", List.of(
                        Map.of("insumoId", "ins-1", "quantidade", 5),
                        Map.of("insumoId", "ins-2", "quantidade", 1))));
    }

    @Test
    void baixaSemItensPublicaSemMexerNoEstoque() {
        when(baixaGateway.buscarPorOrdemServico("os-1"))
                .thenReturn(Optional.of(BaixaOrdemServico.registrar("os-1", List.of(), BAIXADA_EM)));

        assertThat(useCase.executar("os-1", "ExecucaoFalhou")).isTrue();

        verifyNoInteractions(estornarEstoqueUseCase);
        verify(publicador).publicar("EstoqueEstornado", "os-1",
                Map.of("ordemServicoId", "os-1", "motivo", "ExecucaoFalhou", "itens", List.of()));
    }

    @Test
    void semBaixaRegistradaNaoFazNada() {
        when(baixaGateway.buscarPorOrdemServico("os-1")).thenReturn(Optional.empty());

        assertThat(useCase.executar("os-1", "PagamentoExpirado")).isFalse();

        verifyNoInteractions(estornarEstoqueUseCase, publicador);
        verify(baixaGateway, never()).salvar(any());
    }

    @Test
    void baixaJaEstornadaNaoEstornaDeNovo() {
        BaixaOrdemServico baixa = BaixaOrdemServico.registrar("os-1", List.of(new ItemBaixa("ins-1", 1)), BAIXADA_EM);
        baixa.estornar(BAIXADA_EM.plusSeconds(1));
        when(baixaGateway.buscarPorOrdemServico("os-1")).thenReturn(Optional.of(baixa));

        assertThat(useCase.executar("os-1", "ExecucaoFalhou")).isFalse();

        verifyNoInteractions(estornarEstoqueUseCase, publicador);
    }
}
