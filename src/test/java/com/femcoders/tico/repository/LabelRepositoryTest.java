package com.femcoders.tico.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.femcoders.tico.entity.Label;

@DataJpaTest
class LabelRepositoryTest {

    @Autowired
    private LabelRepository labelRepository;

    private Label mockLabel;

    @BeforeEach
    void setUp() {
        mockLabel = new Label();
        mockLabel.setName("Software");
        mockLabel.setColor("#ff0000");
        mockLabel.setIsActive(true);
        labelRepository.save(mockLabel);
    }

    @Test
    void findByIsActiveTrue_ShouldReturnActiveLabels() {

        List<Label> result = labelRepository.findByIsActiveTrue();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnTrue_WhenExists() {

        boolean exists = labelRepository.existsByNameIgnoreCase("software");

        assertTrue(exists);
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnFalse_WhenNotExists() {

        boolean exists = labelRepository.existsByNameIgnoreCase("Hardware");

        assertFalse(exists);
    }

    @Test
    void findByNameContainingIgnoreCase_ShouldReturnMatchingLabels() {

        List<Label> result = labelRepository.findByNameContainingIgnoreCase("soft");

        assertEquals(1, result.size());
        assertEquals("Software", result.get(0).getName());
    }
}
