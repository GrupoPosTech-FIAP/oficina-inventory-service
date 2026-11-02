package com.mecanica.oficina_inventory_service.adapters.web;

import com.mecanica.oficina_inventory_service.adapters.web.dto.request.AlterarInsumosRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.CadastrarInsumosRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.ComprarInsumoSimuladoRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.MovimentarEstoqueLoteRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.response.InsumosResponse;
import com.mecanica.oficina_inventory_service.adapters.web.dto.response.ValidationErrorResponse;
import com.mecanica.oficina_inventory_service.adapters.web.presenter.InsumosPresenter;
import com.mecanica.oficina_inventory_service.application.insumo.input.ItemEstoqueInput;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.AlterarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.AtivarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.BaixarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.CadastrarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.ConsultarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.EstornarEstoqueUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.InativarInsumoUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.ListarInsumosUseCase;
import com.mecanica.oficina_inventory_service.application.insumo.usecase.RegistrarCompraUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("insumos")
@Tag(name = "Insumos", description = "Gerenciamento de insumos e peças do estoque da oficina")
public class InsumosController {

    private final CadastrarInsumoUseCase cadastrarInsumoUseCase;
    private final ConsultarInsumoUseCase consultarInsumoUseCase;
    private final ListarInsumosUseCase listarInsumosUseCase;
    private final AlterarInsumoUseCase alterarInsumoUseCase;
    private final AtivarInsumoUseCase ativarInsumoUseCase;
    private final InativarInsumoUseCase inativarInsumoUseCase;
    private final RegistrarCompraUseCase registrarCompraUseCase;
    private final BaixarEstoqueUseCase baixarEstoqueUseCase;
    private final EstornarEstoqueUseCase estornarEstoqueUseCase;
    private final InsumosPresenter presenter;

    public InsumosController(
            CadastrarInsumoUseCase cadastrarInsumoUseCase,
            ConsultarInsumoUseCase consultarInsumoUseCase,
            ListarInsumosUseCase listarInsumosUseCase,
            AlterarInsumoUseCase alterarInsumoUseCase,
            AtivarInsumoUseCase ativarInsumoUseCase,
            InativarInsumoUseCase inativarInsumoUseCase,
            RegistrarCompraUseCase registrarCompraUseCase,
            BaixarEstoqueUseCase baixarEstoqueUseCase,
            EstornarEstoqueUseCase estornarEstoqueUseCase,
            InsumosPresenter presenter) {
        this.cadastrarInsumoUseCase = cadastrarInsumoUseCase;
        this.consultarInsumoUseCase = consultarInsumoUseCase;
        this.listarInsumosUseCase = listarInsumosUseCase;
        this.alterarInsumoUseCase = alterarInsumoUseCase;
        this.ativarInsumoUseCase = ativarInsumoUseCase;
        this.inativarInsumoUseCase = inativarInsumoUseCase;
        this.registrarCompraUseCase = registrarCompraUseCase;
        this.baixarEstoqueUseCase = baixarEstoqueUseCase;
        this.estornarEstoqueUseCase = estornarEstoqueUseCase;
        this.presenter = presenter;
    }

    @GetMapping
    @Operation(summary = "Listar insumos ativos", description = "Retorna todos os insumos e peças com ativo=true")
    @ApiResponse(responseCode = "200", description = "Lista de insumos ativos")
    public ResponseEntity<List<InsumosResponse>> listarInsumos() {
        return ResponseEntity.ok(presenter.apresentar(listarInsumosUseCase.executar()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar insumo por ID", description = "Retorna os dados de um insumo ativo pelo seu identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Insumo encontrado",
                    content = @Content(schema = @Schema(implementation = InsumosResponse.class))),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado ou inativo",
                    content = @Content(schema = @Schema()))
    })
    public ResponseEntity<InsumosResponse> buscarInsumoPorId(@PathVariable String id) {
        return ResponseEntity.ok(presenter.apresentar(consultarInsumoUseCase.executar(id)));
    }

    @PostMapping
    @Operation(summary = "Cadastrar insumo", description = "Cadastra um novo insumo ou peça no estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Insumo cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = InsumosResponse.class))),
            @ApiResponse(responseCode = "409", description = "Insumo com o mesmo nome já existe",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos campos de entrada",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    public ResponseEntity<InsumosResponse> cadastrarInsumo(@Valid @RequestBody CadastrarInsumosRequest request) {
        var insumo = cadastrarInsumoUseCase.executar(
                request.getNome(),
                request.getPrecoUnitario(),
                request.getEstoqueAtual(),
                request.getEstoqueMinimo(),
                request.getUnidade()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(presenter.apresentar(insumo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Alterar insumo", description = "Atualiza os dados de um insumo ativo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos campos de entrada",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    public ResponseEntity<Void> alterarInsumo(@PathVariable String id, @Valid @RequestBody AlterarInsumosRequest request) {
        alterarInsumoUseCase.executar(
                id,
                request.getNome(),
                request.getPrecoUnitario(),
                request.getEstoqueAtual(),
                request.getEstoqueMinimo(),
                request.getUnidade()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/compra-simulada")
    @Operation(summary = "Simular compra de insumo",
            description = "Registra uma entrega simulada: incrementa o estoque como se a compra tivesse sido concluída (sem fornecedor real)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque atualizado após simulação de compra",
                    content = @Content(schema = @Schema(implementation = InsumosResponse.class))),
            @ApiResponse(responseCode = "404", description = "Insumo ativo não encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos campos de entrada",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    public ResponseEntity<InsumosResponse> compraSimulada(
            @PathVariable String id,
            @Valid @RequestBody ComprarInsumoSimuladoRequest request) {
        return ResponseEntity.ok(presenter.apresentar(registrarCompraUseCase.executar(id, request.getQuantidade())));
    }

    @PostMapping("/baixa")
    @Operation(summary = "Baixar estoque em lote",
            description = "Realiza a baixa de estoque para uma lista de insumos utilizados em uma Ordem de Serviço e emite alerta de estoque crítico quando aplicável")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Baixa de estoque realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "409", description = "Estoque insuficiente para um ou mais insumos",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos campos de entrada",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    public ResponseEntity<Void> baixarEstoque(@Valid @RequestBody MovimentarEstoqueLoteRequest request) {
        List<ItemEstoqueInput> itens = request.getItens().stream()
                .map(i -> new ItemEstoqueInput(i.getInsumoId(), i.getQuantidade()))
                .toList();
        baixarEstoqueUseCase.executar(itens);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/estorno")
    @Operation(summary = "Estornar estoque em lote",
            description = "Devolve ao estoque as quantidades de uma lista de insumos (compensação de SAGA / cancelamento)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estorno de estoque realizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado",
                    content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos campos de entrada",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    public ResponseEntity<Void> estornarEstoque(@Valid @RequestBody MovimentarEstoqueLoteRequest request) {
        List<ItemEstoqueInput> itens = request.getItens().stream()
                .map(i -> new ItemEstoqueInput(i.getInsumoId(), i.getQuantidade()))
                .toList();
        estornarEstoqueUseCase.executar(itens);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Reativar insumo", description = "Reativa um insumo previamente desativado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Insumo reativado",
                    content = @Content(schema = @Schema(implementation = InsumosResponse.class))),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado",
                    content = @Content(schema = @Schema()))
    })
    public ResponseEntity<InsumosResponse> ativarInsumo(@PathVariable String id) {
        return ResponseEntity.ok(presenter.apresentar(ativarInsumoUseCase.executar(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar insumo", description = "Inativa um insumo do estoque (soft delete)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Insumo desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Insumo não encontrado",
                    content = @Content(schema = @Schema()))
    })
    public ResponseEntity<Void> desativarInsumo(@PathVariable String id) {
        inativarInsumoUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}

