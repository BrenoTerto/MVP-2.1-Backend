package com.redepatas.api.parceiro.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redepatas.api.cliente.models.ClientRole;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoDetalheResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AgendamentoListaResponseDTO;
import com.redepatas.api.parceiro.dtos.AgendamentoDtos.AtualizarStatusAgendamentoDTO;
import com.redepatas.api.parceiro.models.Enum.StatusAgendamento;
import com.redepatas.api.parceiro.models.PartnerModel;
import com.redepatas.api.parceiro.models.TipoServico;
import com.redepatas.api.parceiro.services.AgendamentoService;

class AgendamentosControllerStandaloneTest {

    private MockMvc mockMvc;
    private AgendamentoService agendamentoService;
    private ObjectMapper objectMapper;
    private PartnerModel parceiro;

    @BeforeEach
    void setup() {
        agendamentoService = Mockito.mock(AgendamentoService.class);
        AgendamentosController controller = new AgendamentosController();
        ReflectionTestUtils.setField(controller, "agendamentoService", agendamentoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        parceiro = new PartnerModel();
        parceiro.setIdPartner(UUID.randomUUID());
        parceiro.setLogin("parceiro@test");
        parceiro.setPassword("x");
        parceiro.setRole(ClientRole.PARTNER);
        var auth = new UsernamePasswordAuthenticationToken(parceiro, null, parceiro.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void listarAgendamentosDoDia_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var item = new AgendamentoListaResponseDTO(
            id,
            LocalDate.now(),
            "09:00 - 10:00",
            StatusAgendamento.PENDENTE,
            "Fulano",
            "Bolt",
            45.0,
            TipoServico.BANHO
        );
        when(agendamentoService.listarAgendamentosDoDia(eq(parceiro.getIdPartner()), any(LocalDate.class)))
            .thenReturn(List.of(item));

        mockMvc.perform(get("/agendamentos/hoje"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id.toString()))
            .andExpect(jsonPath("$[0].status").value("PENDENTE"))
            .andExpect(jsonPath("$[0].clienteNome").value("Fulano"));
    }

    @Test
    void listarAgendamentosPendentes_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var item = new AgendamentoListaResponseDTO(
            id,
            LocalDate.now(),
            "09:00 - 10:00",
            StatusAgendamento.PENDENTE,
            "Fulano",
            "Bolt",
            45.0,
            TipoServico.BANHO
        );
        when(agendamentoService.listarAgendamentosPendentes(eq(parceiro.getIdPartner())))
            .thenReturn(List.of(item));

        mockMvc.perform(get("/agendamentos/pendentes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(id.toString()))
            .andExpect(jsonPath("$[0].status").value("PENDENTE"));
    }

    @Test
    void detalhesAgendamento_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var detalhe = new AgendamentoDetalheResponseDTO(
            id,
            LocalDate.now(),
            "09:00 - 10:00",
            StatusAgendamento.PENDENTE,
            45.0,
            TipoServico.BANHO,
            "Fulano",
            "Bolt",
            "CACHORRO",
            "Vira-lata",
            "",
            true,
            true,
            "M",
            java.math.BigDecimal.TEN,
            "",
            "PEQUENO",
            "https://...",
            List.of(new AgendamentoDetalheResponseDTO.ItemAdicional("Corte de Unhas", 15.0))
        );
        when(agendamentoService.buscarDetalhesAgendamento(eq(parceiro.getIdPartner()), eq(id)))
            .thenReturn(detalhe);

        mockMvc.perform(get("/agendamentos/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.petNome").value("Bolt"))
            .andExpect(jsonPath("$.itens[0].nome").value("Corte de Unhas"));
    }

    @Test
    void decidirAgendamento_ok() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(agendamentoService).decidirAgendamento(eq(parceiro.getIdPartner()), eq(id), eq(true));
        var body = new AtualizarStatusAgendamentoDTO();
        body.setAceitar(true);

        mockMvc.perform(
            put("/agendamentos/{id}/decisao", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body))
        )
        .andExpect(status().isOk());

        verify(agendamentoService).decidirAgendamento(eq(parceiro.getIdPartner()), eq(id), eq(true));
    }
}

