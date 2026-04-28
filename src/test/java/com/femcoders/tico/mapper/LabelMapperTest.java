package com.femcoders.tico.mapper;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.femcoders.tico.dto.request.LabelRequest;
import com.femcoders.tico.dto.response.LabelResponse;
import com.femcoders.tico.entity.Label;
import com.femcoders.tico.utils.LabelMappingUtils;

@ExtendWith(MockitoExtension.class)
class LabelMapperTest {

    @Spy
    private LabelMappingUtils labelMappingUtils;

    @InjectMocks
    private LabelMapperImpl labelMapper;

    @Test
    void toEntity_ShouldMapCorrectly() {

        LabelRequest request = new LabelRequest("Software", "#ff0000");

        Label result = labelMapper.toEntity(request);

        assertEquals("Software", result.getName());
        assertEquals("#ff0000", result.getColor());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
    }

    @Test
    void toResponseDto_ShouldMapCorrectly() {

        Label label = new Label();
        label.setId(1L);
        label.setName("Software");
        label.setColor("#ff0000");
        label.setIsActive(true);
        label.setTickets(new HashSet<>());

        LabelResponse result = labelMapper.toResponseDto(label);

        assertEquals(1L, result.id());
        assertEquals("Software", result.name());
        assertEquals("#ff0000", result.color());
        assertTrue(result.active());
        assertEquals(0L, result.activeTickets());
        assertEquals(0L, result.closedTickets());
    }
}
