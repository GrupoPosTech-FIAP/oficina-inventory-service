package com.mecanica.oficina_inventory_service.domain.insumo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InsumoTest {

    @Test
    void deveCriarInsumoComDadosValidados() {
        Insumos insumo = Insumos.criar("Fluido de arrefecimento", BigDecimal.valueOf(19.9), 10, 2, "Unidade");

        assertThat(insumo.getNome()).isEqualTo("Fluido de arrefecimento");
        assertThat(insumo.getPrecoUnitario()).isEqualTo(BigDecimal.valueOf(19.9));
        assertThat(insumo.getEstoqueAtual()).isEqualTo(10);
        assertThat(insumo.getEstoqueMinimo()).isEqualTo(2);
        assertThat(insumo.getUnidade()).isEqualTo("Unidade");
        assertThat(insumo.getAtivo()).isEqualTo(true);
    }

    @Test
    void deveLancarExcecaoQuandoPrecoForNegativo() {
        assertThatThrownBy(() -> Insumos.criar("Fluido de arrefecimento", BigDecimal.valueOf(-10.23), 10, 2, "Unidade"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preço Unitário deve ser um número positivo");
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueAtualForNegativo() {
        assertThatThrownBy(() -> Insumos.criar("Fluido de arrefecimento", BigDecimal.valueOf(19.9), -5, 2, "Unidade"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque Atual deve ser um número inteiro positivo");
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueMinimoForNegativo() {
        assertThatThrownBy(() -> Insumos.criar("Fluido de arrefecimento", BigDecimal.valueOf(19.9), 10, -10, "Unidade"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque Mínimo deve ser um número inteiro positivo");
    }

    @Test
    void deveLancarExcecaoQuandoNomeForNulo() {
        assertThatThrownBy(() -> Insumos.criar(null, BigDecimal.valueOf(19.9), 10, 2, "Unidade"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Nome é obrigatório");
    }

    @Test
    void deveLancarExcecaoQuandoUnidadeForNula() {
        assertThatThrownBy(() -> Insumos.criar("Fluido de arrefecimento", BigDecimal.valueOf(19.9), 10, 2, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Unidade é obrigatório");
    }

    @Test
    void deveRetornarIdNuloQuandoCriadoSemPersistencia() {
        Insumos insumo = Insumos.criar("Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");

        assertThat(insumo.getId()).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoPrecoForNulo() {
        assertThatThrownBy(() -> Insumos.criar("Óleo", null, 10, 2, "LITRO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Preço Unitário deve ser um número positivo");
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueAtualForNulo() {
        assertThatThrownBy(() -> Insumos.criar("Óleo", BigDecimal.valueOf(50), null, 2, "LITRO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque Atual deve ser um número inteiro positivo");
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueMinimoForNulo() {
        assertThatThrownBy(() -> Insumos.criar("Óleo", BigDecimal.valueOf(50), 10, null, "LITRO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque Mínimo deve ser um número inteiro positivo");
    }

    // --- darBaixa ---

    @Test
    void deveDarBaixaNoEstoqueComSucesso() {
        Insumos insumo = Insumos.criar("Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");

        insumo.darBaixa(3);

        assertThat(insumo.getEstoqueAtual()).isEqualTo(7);
    }

    @Test
    void deveDarBaixaConsumindoEstoqueTotalComSucesso() {
        Insumos insumo = Insumos.criar("Óleo", BigDecimal.valueOf(50), 5, 0, "LITRO");

        insumo.darBaixa(5);

        assertThat(insumo.getEstoqueAtual()).isEqualTo(0);
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeDarBaixaForZero() {
        Insumos insumo = Insumos.criar("Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");

        assertThatThrownBy(() -> insumo.darBaixa(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade para baixa deve ser positiva");
    }

    @Test
    void deveLancarExcecaoQuandoQuantidadeDarBaixaForNegativa() {
        Insumos insumo = Insumos.criar("Óleo", BigDecimal.valueOf(50), 10, 2, "LITRO");

        assertThatThrownBy(() -> insumo.darBaixa(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade para baixa deve ser positiva");
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueInsuficienteParaDarBaixa() {
        Insumos insumo = Insumos.criar("Óleo", BigDecimal.valueOf(50), 3, 0, "LITRO");

        assertThatThrownBy(() -> insumo.darBaixa(5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Estoque insuficiente para o insumo");
    }

    // --- deveEmitirAlerta ---

    @Test
    void deveEmitirQuandoSoAumentaMinimoEstoqueIgualAoMinimoAntigo() {
        assertThat(Insumos.deveEmitirAlerta(5, 5, 5, 10)).isTrue();
    }

    @Test
    void deveEmitirQuandoEstoqueCruzaParaCriticoPorBaixa() {
        assertThat(Insumos.deveEmitirAlerta(10, 2, 1, 2)).isTrue();
    }

    @Test
    void naoDeveEmitirQuandoJaCriticoESoBaixaMaisSemMudarMinimo() {
        assertThat(Insumos.deveEmitirAlerta(2, 2, 1, 2)).isFalse();
    }

    @Test
    void naoDeveEmitirQuandoPermaneceAcimaDoNovoMinimo() {
        assertThat(Insumos.deveEmitirAlerta(30, 5, 30, 10)).isFalse();
    }

    // --- deveNotificarAlteracaoInsumo ---

    @Test
    void deveNotificarAlteracaoInsumoQuandoEstoqueIgualAoMinimo() {
        assertThat(Insumos.deveNotificarAlteracaoInsumo(5, 5)).isTrue();
    }

    @Test
    void deveNotificarAlteracaoInsumoQuandoEstoqueAbaixoDoMinimo() {
        assertThat(Insumos.deveNotificarAlteracaoInsumo(2, 10)).isTrue();
    }

    @Test
    void naoDeveNotificarAlteracaoInsumoQuandoEstoqueAcimaDoMinimo() {
        assertThat(Insumos.deveNotificarAlteracaoInsumo(10, 5)).isFalse();
    }
}

