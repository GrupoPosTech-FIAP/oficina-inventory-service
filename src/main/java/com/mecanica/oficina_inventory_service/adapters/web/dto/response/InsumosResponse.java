package com.mecanica.oficina_inventory_service.adapters.web.dto.response;

import java.math.BigDecimal;

public class InsumosResponse {

    private String id;
    private String nome;
    private BigDecimal precoUnitario;
    private Integer estoqueAtual;
    private Integer estoqueMinimo;
    private String unidade;
    private Boolean ativo;

    public InsumosResponse() {
    }

    public InsumosResponse(String id, String nome, BigDecimal precoUnitario, Integer estoqueAtual, Integer estoqueMinimo, String unidade, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.precoUnitario = precoUnitario;
        this.estoqueAtual = estoqueAtual;
        this.estoqueMinimo = estoqueMinimo;
        this.unidade = unidade;
        this.ativo = ativo;
    }

    public String getId() {
        return id;
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

    public Boolean getAtivo() {
        return ativo;
    }
}

