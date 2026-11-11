package com.mecanica.oficina_inventory_service.domain.baixa;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaixaOrdemServicoTest {

    private static final Instant AGORA = Instant.parse("2026-11-08T10:00:00Z");

    @Test
    void registraBaixaComoBaixada() {
        BaixaOrdemServico baixa = BaixaOrdemServico.registrar("os-1", List.of(new ItemBaixa("ins-1", 2)), AGORA);

        assertThat(baixa.getStatus()).isEqualTo(BaixaStatus.BAIXADA);
        assertThat(baixa.isEstornada()).isFalse();
        assertThat(baixa.getBaixadaEm()).isEqualTo(AGORA);
        assertThat(baixa.getEstornadaEm()).isNull();
        assertThat(baixa.getItens()).containsExactly(new ItemBaixa("ins-1", 2));
    }

    @Test
    void estornaUmaUnicaVez() {
        BaixaOrdemServico baixa = BaixaOrdemServico.registrar("os-1", List.of(), AGORA);
        Instant depois = AGORA.plusSeconds(60);

        baixa.estornar(depois);

        assertThat(baixa.isEstornada()).isTrue();
        assertThat(baixa.getEstornadaEm()).isEqualTo(depois);
        assertThatThrownBy(() -> baixa.estornar(depois))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já foi estornada");
    }

    @Test
    void exigeOrdemServico() {
        assertThatThrownBy(() -> BaixaOrdemServico.registrar(null, List.of(), AGORA))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void itemExigeInsumoEQuantidadePositiva() {
        assertThatThrownBy(() -> new ItemBaixa(null, 1)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ItemBaixa("ins-1", 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
