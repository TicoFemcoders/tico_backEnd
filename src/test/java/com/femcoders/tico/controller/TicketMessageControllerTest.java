package com.femcoders.tico.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.dto.response.TicketMessageResponse;
import com.femcoders.tico.service.TicketMessageService;

@ExtendWith(MockitoExtension.class)
class TicketMessageControllerTest {

    @Mock
    private TicketMessageService ticketMessageService;

    @InjectMocks
    private TicketMessageController ticketMessageController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TicketMessageResponse messageResponse;

   @BeforeEach
void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    mockMvc = MockMvcBuilders.standaloneSetup(ticketMessageController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
            .build();

    messageResponse = new TicketMessageResponse(
            1L, 10L, "Ana García",
            "Hola, ¿puedes revisar esto?",
            false, LocalDateTime.now(), null);
}

    // ── GET /api/tickets/{ticketId}/messages ───────────────────────────────

@Test
void getMessages_debeRetornar200_conListaDeMensajes() {
    when(ticketMessageService.getMessagesByTicketId(eq(10L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(messageResponse)));

    ResponseEntity<Page<TicketMessageResponse>> response =
            ticketMessageController.getMessages(10L, Pageable.unpaged());

    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, response.getBody().getTotalElements());
}

@Test
void getMessages_debeRetornar200_conListaVacia() {
    when(ticketMessageService.getMessagesByTicketId(eq(10L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

    ResponseEntity<Page<TicketMessageResponse>> response =
            ticketMessageController.getMessages(10L, Pageable.unpaged());

    assertEquals(200, response.getStatusCode().value());
    assertEquals(0, response.getBody().getTotalElements());
}

    // ── POST /api/tickets/{ticketId}/messages ──────────────────────────────

    @Test
    void createMessage_debeRetornar201_cuandoDatosCorrectos() throws Exception {
        TicketMessageRequest request = new TicketMessageRequest(10L, "Nuevo mensaje", null);
        when(ticketMessageService.createMessage(eq(10L), any(TicketMessageRequest.class)))
                .thenReturn(messageResponse);

        mockMvc.perform(post("/api/tickets/10/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Hola, ¿puedes revisar esto?"));
    }

    @Test
    void createMessage_debeRetornar400_cuandoContentEsBlanco() throws Exception {
        TicketMessageRequest request = new TicketMessageRequest(10L, "", null);

        mockMvc.perform(post("/api/tickets/10/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createMessage_debeRetornar400_cuandoTicketIdEsNull() throws Exception {
        TicketMessageRequest request = new TicketMessageRequest(null, "Mensaje válido", null);

        mockMvc.perform(post("/api/tickets/10/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}