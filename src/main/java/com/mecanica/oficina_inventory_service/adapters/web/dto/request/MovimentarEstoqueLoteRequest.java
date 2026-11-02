package com.mecanica.oficina_inventory_service.adapters.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentarEstoqueLoteRequest {

    @NotEmpty(message = "A lista de itens não pode ser vazia")
    @Valid
    private List<ItemEstoqueRequest> itens;
}

