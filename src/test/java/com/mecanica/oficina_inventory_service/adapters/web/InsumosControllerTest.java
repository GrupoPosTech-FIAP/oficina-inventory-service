package com.mecanica.oficina_inventory_service.adapters.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mecanica.oficina_inventory_service.adapters.common.FileUtils;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.AlterarInsumosRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.CadastrarInsumosRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.ComprarInsumoSimuladoRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.ItemEstoqueRequest;
import com.mecanica.oficina_inventory_service.adapters.web.dto.request.MovimentarEstoqueLoteRequest;
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
import com.mecanica.oficina_inventory_service.domain.insumo.Insumos;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InsumosControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FileUtils fileUtils = new FileUtils();

    @Mock
    private CadastrarInsumoUseCase cadastrarInsumoUseCase;
    @Mock
    private ConsultarInsumoUseCase consultarInsumoUseCase;
    @Mock
    private ListarInsumosUseCase listarInsumosUseCase;
    @Mock
    private AlterarInsumoUseCase alterarInsumoUseCase;
    @Mock
    private AtivarInsumoUseCase ativarInsumoUseCase;
    @Mock
    private InativarInsumoUseCase inativarInsumoUseCase;
    @Mock
    private RegistrarCompraUseCase registrarCompraUseCase;
    @Mock
    private BaixarEstoqueUseCase baixarEstoqueUseCase;
    @Mock
    private EstornarEstoqueUseCase estornarEstoqueUseCase;

    private static final String ID = "insumo-1";

    private Insumos insumo;

    @BeforeEach
    void setUp() {
        InsumosController controller = new InsumosController(
                cadastrarInsumoUseCase, consultarInsumoUseCase, listarInsumosUseCase,
                alterarInsumoUseCase, ativarInsumoUseCase, inativarInsumoUseCase,
                registrarCompraUseCase, baixarEstoqueUseCase, estornarEstoqueUseCase,
                new InsumosPresenter());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        insumo = Insumos.reconstituir(
                ID, "Óleo 5W30", new BigDecimal("45.90"), 20, 5, "L");
        insumo.ativar();
    }

    @Test
    void deveListarInsumosERetornar200() throws Exception {
        when(listarInsumosUseCase.executar()).thenReturn(List.of(insumo));

        mockMvc.perform(get("/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(ID))
                .andExpect(jsonPath("$[0].nome").value("Óleo 5W30"))
                .andExpect(jsonPath("$[0].ativo").value(true));
    }

    @Test
    void deveBuscarInsumoPorIdERetornar200() throws Exception {
        when(consultarInsumoUseCase.executar(ID)).thenReturn(insumo);

        mockMvc.perform(get("/insumos/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.nome").value("Óleo 5W30"))
                .andExpect(jsonPath("$.precoUnitario").value(45.90))
                .andExpect(jsonPath("$.estoqueAtual").value(20))
                .andExpect(jsonPath("$.estoqueMinimo").value(5))
                .andExpect(jsonPath("$.unidade").value("L"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveRetornar404AoBuscar_quandoInsumoNaoEncontrado() throws Exception {
        when(consultarInsumoUseCase.executar("inexistente"))
                .thenThrow(new IllegalArgumentException("Insumo não encontrado"));

        mockMvc.perform(get("/insumos/inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveCadastrarInsumoERetornar201() throws Exception {
        when(cadastrarInsumoUseCase.executar("Óleo 5W30", new BigDecimal("45.90"), 20, 5, "L"))
                .thenReturn(insumo);

        mockMvc.perform(post("/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastrarRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.nome").value("Óleo 5W30"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void deveRetornar404AoCadastrar_quandoUseCaseLancaIllegalArgument() throws Exception {
        when(cadastrarInsumoUseCase.executar(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Insumo já cadastrado"));

        mockMvc.perform(post("/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastrarRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAlterarInsumoERetornar204() throws Exception {
        mockMvc.perform(put("/insumos/" + ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alterarRequest())))
                .andExpect(status().isNoContent());

        verify(alterarInsumoUseCase).executar(
                ID, "Óleo 10W40", new BigDecimal("50.00"), 30, 10, "L");
    }

    @Test
    void deveRetornar404AoAlterar_quandoUseCaseLancaIllegalArgument() throws Exception {
        doThrow(new IllegalArgumentException("Insumo não encontrado"))
                .when(alterarInsumoUseCase).executar(eq("inexistente"), any(), any(), any(), any(), any());

        mockMvc.perform(put("/insumos/inexistente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alterarRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRegistrarCompraSimuladaERetornar200() throws Exception {
        when(registrarCompraUseCase.executar(ID, 15)).thenReturn(insumo);

        mockMvc.perform(post("/insumos/" + ID + "/compra-simulada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComprarInsumoSimuladoRequest(15))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID));

        verify(registrarCompraUseCase).executar(ID, 15);
    }

    @Test
    void deveRetornar404AoRegistrarCompra_quandoUseCaseLancaIllegalArgument() throws Exception {
        when(registrarCompraUseCase.executar(eq("inexistente"), any()))
                .thenThrow(new IllegalArgumentException("Insumo não encontrado"));

        mockMvc.perform(post("/insumos/inexistente/compra-simulada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComprarInsumoSimuladoRequest(15))))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBaixarEstoqueEmLoteERetornar204() throws Exception {
        var request = new MovimentarEstoqueLoteRequest(List.of(new ItemEstoqueRequest(ID, 3)));

        mockMvc.perform(post("/insumos/baixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(baixarEstoqueUseCase).executar(List.of(new ItemEstoqueInput(ID, 3)));
    }

    @Test
    void deveRetornar409AoBaixarEstoque_quandoEstoqueInsuficiente() throws Exception {
        doThrow(new IllegalStateException("Estoque insuficiente"))
                .when(baixarEstoqueUseCase).executar(any());

        var request = new MovimentarEstoqueLoteRequest(List.of(new ItemEstoqueRequest(ID, 99)));

        mockMvc.perform(post("/insumos/baixa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveEstornarEstoqueEmLoteERetornar204() throws Exception {
        var request = new MovimentarEstoqueLoteRequest(List.of(new ItemEstoqueRequest(ID, 3)));

        mockMvc.perform(post("/insumos/estorno")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(estornarEstoqueUseCase).executar(List.of(new ItemEstoqueInput(ID, 3)));
    }

    @Test
    void deveAtivarInsumoERetornar200() throws Exception {
        when(ativarInsumoUseCase.executar(ID)).thenReturn(insumo);

        mockMvc.perform(patch("/insumos/" + ID + "/ativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(ativarInsumoUseCase).executar(ID);
    }

    @Test
    void deveRetornar404AoAtivar_quandoUseCaseLancaIllegalArgument() throws Exception {
        when(ativarInsumoUseCase.executar("inexistente"))
                .thenThrow(new IllegalArgumentException("Insumo não encontrado"));

        mockMvc.perform(patch("/insumos/inexistente/ativar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveDesativarInsumoERetornar204() throws Exception {
        mockMvc.perform(delete("/insumos/" + ID))
                .andExpect(status().isNoContent());

        verify(inativarInsumoUseCase).executar(ID);
    }

    @Test
    void deveRetornar404AoDesativar_quandoUseCaseLancaIllegalArgument() throws Exception {
        doThrow(new IllegalArgumentException("Insumo não encontrado"))
                .when(inativarInsumoUseCase).executar("inexistente");

        mockMvc.perform(delete("/insumos/inexistente"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("dadosCamposInvalidos")
    void deveRetornar400_quandoCamposEstaoInvalidosAoCriar(String filename, List<String> errors) throws Exception {
        var request = fileUtils.readResourceFile("/insumos/%s".formatted(filename));

        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/insumos")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseError = objectMapper.readValue(mvcResult, ValidationErrorResponse.class);

        Assertions.assertThat(responseError.erros())
                .extracting(ValidationErrorResponse.FieldError::mensagem)
                .containsExactlyInAnyOrderElementsOf(errors);
    }

    @ParameterizedTest
    @MethodSource("dadosCamposInvalidos")
    void deveRetornar400_quandoCamposEstaoInvalidosAoAlterar(String filename, List<String> errors) throws Exception {
        var request = fileUtils.readResourceFile("/insumos/%s".formatted(filename));

        var insumoId = UUID.randomUUID();

        var mvcResult = mockMvc.perform(MockMvcRequestBuilders.put("/insumos/" + insumoId)
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseError = objectMapper.readValue(mvcResult, ValidationErrorResponse.class);

        Assertions.assertThat(responseError.erros())
                .extracting(ValidationErrorResponse.FieldError::mensagem)
                .containsExactlyInAnyOrderElementsOf(errors);
    }

    private CadastrarInsumosRequest cadastrarRequest() {
        return new CadastrarInsumosRequest("Óleo 5W30", new BigDecimal("45.90"), 20, 5, "L", true);
    }

    private AlterarInsumosRequest alterarRequest() {
        return new AlterarInsumosRequest("Óleo 10W40", new BigDecimal("50.00"), 30, 10, "L", true);
    }

    private static Stream<Arguments> dadosCamposInvalidos() {
        var errosComuns = errosComuns();

        var erroPrecoNegativo = "O preço unitário deve ser maior ou igual a zero";
        var erroEstoqueAtualNegativo = "O estoque atual deve ser maior ou igual a zero";
        var erroEstoqueMinimoNegativo = "O estoque mínimo deve ser maior ou igual a zero";

        return Stream.of(
                Arguments.of("cadastrar-insumos-400-campos-vazios.json", errosComuns),
                Arguments.of("cadastrar-insumos-400-campos-nulos.json", errosComuns),
                Arguments.of("alterar-insumo-400-preco-negativo.json", List.of(erroPrecoNegativo)),
                Arguments.of("alterar-insumo-400-estoque-atual-negativo.json", List.of(erroEstoqueAtualNegativo)),
                Arguments.of("alterar-insumo-400-estoque-minimo-negativo.json", List.of(erroEstoqueMinimoNegativo))
        );
    }

    private static List<String> errosComuns() {
        var erroCampoNome = "O nome é obrigatório";
        var erroPrecoNulo = "O preço unitário é obrigatório";
        var erroEstoqueAtualNulo = "O estoque atual é obrigatório";
        var erroEstoqueMinimoNulo = "O estoque mínimo é obrigatório";
        var erroUnidade = "A unidade é obrigatória";

        return new ArrayList<>(List.of(erroCampoNome, erroPrecoNulo, erroEstoqueAtualNulo, erroEstoqueMinimoNulo, erroUnidade));
    }
}

