package com.mecanica.oficina_inventory_service.adapters.persistence;

import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class InsumosJpaGatewayTest {

    @Autowired
    private InsumosJpaGateway gateway;

    private Insumos novoInsumo(String nome) {
        return Insumos.criar(nome, new BigDecimal("45.90"), 20, 5, "L");
    }

    @Test
    void deveCriarEBuscarInsumoPorId() {
        Insumos salvo = gateway.criar(novoInsumo("Óleo 5W30"));

        assertThat(salvo.getId()).isNotBlank();
        assertThat(salvo.getAtivo()).isTrue();

        Optional<Insumos> encontrado = gateway.buscar(salvo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("Óleo 5W30");
        assertThat(encontrado.get().getEstoqueAtual()).isEqualTo(20);
        assertThat(encontrado.get().getUnidade()).isEqualTo("L");
    }

    @Test
    void deveRetornarVazio_quandoIdNaoExiste() {
        assertThat(gateway.buscar("inexistente")).isEmpty();
        assertThat(gateway.existePorId("inexistente")).isFalse();
        assertThat(gateway.existePorNome("Inexistente")).isFalse();
    }

    @Test
    void deveVerificarExistenciaPorNomeEPorId() {
        Insumos salvo = gateway.criar(novoInsumo("Filtro de Óleo"));

        assertThat(gateway.existePorNome("Filtro de Óleo")).isTrue();
        assertThat(gateway.existePorId(salvo.getId())).isTrue();
    }

    @Test
    void deveListarApenasInsumosAtivos() {
        Insumos inativado = gateway.criar(novoInsumo("Inativo"));
        gateway.inativar(inativado.getId());
        Insumos ativo = gateway.criar(novoInsumo("Ativo"));

        List<Insumos> ativos = gateway.listar();

        // o banco de teste é compartilhado: outras classes podem deixar insumos gravados
        assertThat(ativos).allMatch(Insumos::getAtivo);
        assertThat(ativos).extracting(Insumos::getId)
                .contains(ativo.getId())
                .doesNotContain(inativado.getId());
    }

    @Test
    void deveAlterarDadosDoInsumoAtivo() {
        Insumos salvo = gateway.criar(novoInsumo("Óleo Antigo"));

        Insumos novosDados = Insumos.reconstituir(
                salvo.getId(), "Óleo Novo", new BigDecimal("60.00"), 50, 10, "GL");

        Insumos alterado = gateway.alterar(salvo.getId(), novosDados);

        assertThat(alterado.getNome()).isEqualTo("Óleo Novo");
        assertThat(alterado.getPrecoUnitario()).isEqualByComparingTo(new BigDecimal("60.00"));
        assertThat(alterado.getEstoqueAtual()).isEqualTo(50);
        assertThat(alterado.getEstoqueMinimo()).isEqualTo(10);
        assertThat(alterado.getUnidade()).isEqualTo("GL");
    }

    @Test
    void deveInativarEReativarInsumo() {
        Insumos salvo = gateway.criar(novoInsumo("Pastilha"));

        gateway.inativar(salvo.getId());
        assertThat(gateway.buscar(salvo.getId())).isEmpty();
        assertThat(gateway.existePorId(salvo.getId())).isFalse();

        Insumos reativado = gateway.ativar(salvo.getId());
        assertThat(reativado.getAtivo()).isTrue();
        assertThat(gateway.buscar(salvo.getId())).isPresent();
    }

    @Test
    void deveObterEstoqueAtualDoInsumoAtivo() {
        Insumos salvo = gateway.criar(novoInsumo("Correia"));

        Integer estoque = gateway.obterEstoqueAtual(salvo.getId());

        assertThat(estoque).isEqualTo(20);
    }
}

