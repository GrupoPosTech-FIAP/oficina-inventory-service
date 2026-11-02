package com.mecanica.oficina_inventory_service.adapters.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class AlterarInsumosRequest {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotNull(message = "O preço unitário é obrigatório")
    @PositiveOrZero(message = "O preço unitário deve ser maior ou igual a zero")
    private BigDecimal precoUnitario;

    @NotNull(message = "O estoque atual é obrigatório")
    @PositiveOrZero(message = "O estoque atual deve ser maior ou igual a zero")
    private Integer estoqueAtual;

    @NotNull(message = "O estoque mínimo é obrigatório")
    @PositiveOrZero(message = "O estoque mínimo deve ser maior ou igual a zero")
    private Integer estoqueMinimo;

    @NotBlank(message = "A unidade é obrigatória")
    private String unidade;

    public AlterarInsumosRequest() {
    }

    public AlterarInsumosRequest(String nome, BigDecimal precoUnitario, Integer estoqueAtual, Integer estoqueMinimo, String unidade, Boolean ativo) {
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.estoqueAtual = estoqueAtual;
        this.estoqueMinimo = estoqueMinimo;
        this.unidade = unidade;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public Integer getEstoqueAtual() {
        return estoqueAtual;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public String getUnidade() {
        return unidade;
    }
}

