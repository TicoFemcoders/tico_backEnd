package com.femcoders.tico.mapper;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import com.femcoders.tico.dto.request.TicketCreateRequest;
import com.femcoders.tico.dto.response.LabelSummary;
import com.femcoders.tico.dto.response.TicketResponse;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.entity.Ticket;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TicketPriority;
import com.femcoders.tico.enums.TicketStatus;

@ExtendWith(MockitoExtension.class)
class TicketMapperTest {

    private final TicketMapper mapper = new TicketMapperImpl();


    @Test
    void whenToEntity_thenTitleAndDescriptionAreMapped() {
        TicketCreateRequest request = new TicketCreateRequest(
                "Titulo test", "Descripcion test larga", TicketPriority.HIGH, List.of());

        Ticket result = mapper.toEntity(request);

        assertEquals("Titulo test", result.getTitle());
        assertEquals("Descripcion test larga", result.getDescription());
        assertEquals(TicketPriority.HIGH, result.getPriority());
    }

    @Test
    void whenToEntity_thenIgnoredFieldsAreNull() {
        TicketCreateRequest request = new TicketCreateRequest(
                "Titulo test", "Descripcion test larga", TicketPriority.HIGH, List.of());

        Ticket result = mapper.toEntity(request);

        assertNull(result.getId());
        assertNull(result.getCreatedBy());
        assertNull(result.getAssignedTo());
        assertNull(result.getCreatedAt());
        assertNull(result.getClosedAt());
    }


    @Test
    void whenToResponseDTO_thenFieldsAreMappedCorrectly() {
        Ticket ticket = buildTicket();

        TicketResponse result = mapper.toResponseDTO(ticket);

        assertEquals(1L, result.id());
        assertEquals("Titulo test", result.title());
        assertEquals(TicketStatus.OPEN, result.status());
        assertEquals("Employee Test", result.createdByName());
        assertEquals(2L, result.assignedToId());
        assertEquals("Admin Test", result.assignedToName());
    }

    @Test
    void whenToResponseDTO_thenLabelsAreMappedToSummaries() {
        Ticket ticket = buildTicket();

        TicketResponse result = mapper.toResponseDTO(ticket);

        assertEquals(1, result.labels().size());
        LabelSummary label = result.labels().iterator().next();
        assertEquals("Bug", label.name());
        assertEquals("#FF0000", label.color());
        assertTrue(label.isActive());
    }

    @Test
    void whenToResponseDTO_withNoAssignedTo_thenAssignedFieldsAreNull() {
        Ticket ticket = buildTicket();
        ticket.setAssignedTo(null);

        TicketResponse result = mapper.toResponseDTO(ticket);

        assertNull(result.assignedToId());
        assertNull(result.assignedToName());
    }

    @Test
    void whenToResponseDTO_withNoLabels_thenLabelsAreEmpty() {
        Ticket ticket = buildTicket();
        ticket.setLabels(new HashSet<>());

        TicketResponse result = mapper.toResponseDTO(ticket);

        assertTrue(result.labels().isEmpty());
    }


    @Test
    void whenLabelsToSummaries_withNullLabels_thenReturnsEmptySet() {
        Set<LabelSummary> result = mapper.labelsToSummaries(null);

        assertTrue(result.isEmpty());
    }

   

    private Ticket buildTicket() {
        User employee = new User();
        employee.setName("Employee Test");
        employee.setEmail("employee@test.com");
        employee.setPasswordHash("hash");

        User admin = new User();
        admin.setId(2L);
        admin.setName("Admin Test");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("hash");

        Label label = new Label();
        label.setName("Bug");
        label.setColor("#FF0000");
        label.setIsActive(true);

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Titulo test");
        ticket.setDescription("Descripcion test larga");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setCreatedBy(employee);
        ticket.setAssignedTo(admin);
        ticket.setLabels(new HashSet<>(Set.of(label)));

        return ticket;
    }
}