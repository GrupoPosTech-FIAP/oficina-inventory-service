package com.mecanica.oficina_inventory_service.adapters.web.presenter;

import com.mecanica.oficina_inventory_service.adapters.web.dto.response.InsumosResponse;
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InsumosPresenterTest {

    private final InsumosPresenter presenter = new InsumosPresenter();

    @Test
    void deveApresentarTodosOsCampos_quandoInsumoAtivo() {
        Insumos insumo = Insumos.reconstituir(
                "insumo-1", "Óleo 5W30", new BigDecimal("45.90"), 20, 5, "L");
        insumo.ativar();

        InsumosResponse response = presenter.apresentar(insumo);

        assertThat(response.getId()).isEqualTo("insumo-1");
        assertThat(response.getNome()).isEqualTo("Óleo 5W30");
        assertThat(response.getPrecoUnitario()).isEqualByComparingTo(new BigDecimal("45.90"));
        assertThat(response.getEstoqueAtual()).isEqualTo(20);
        assertThat(response.getEstoqueMinimo()).isEqualTo(5);
        assertThat(response.getUnidade()).isEqualTo("L");
        assertThat(response.getAtivo()).isTrue();
    }

    @Test
    void deveRefletirAtivoFalse_quandoInsumoInativo() {
        Insumos insumo = Insumos.reconstituir(
                "insumo-2", "Filtro", new BigDecimal("12.00"), 0, 2, "UN");
        insumo.inativar();

        InsumosResponse response = presenter.apresentar(insumo);

        assertThat(response.getAtivo()).isFalse();
        assertThat(response.getEstoqueAtual()).isZero();
    }

    @Test
    void deveApresentarLista_mapeandoCadaInsumo() {
        Insumos a = Insumos.reconstituir("a", "Óleo", new BigDecimal("45.90"), 20, 5, "L");
        a.ativar();
        Insumos b = Insumos.reconstituir("b", "Filtro", new BigDecimal("12.00"), 3, 2, "UN");
        b.inativar();

        List<InsumosResponse> responses = presenter.apresentar(List.of(a, b));

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo("a");
        assertThat(responses.get(0).getAtivo()).isTrue();
        assertThat(responses.get(1).getId()).isEqualTo("b");
        assertThat(responses.get(1).getAtivo()).isFalse();
    }
}

