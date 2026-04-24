package com.femcoders.tico.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.femcoders.tico.enums.TicketStatus;

public record LabelTicketCounts(Map<Long, Long> active, Map<Long, Long> closed) {

    public static LabelTicketCounts from(List<Object[]> rows) {
        Map<Long, Long> active = new HashMap<>();
        Map<Long, Long> closed = new HashMap<>();
        for (Object[] row : rows) {
            Long labelId = (Long) row[0];
            TicketStatus status = (TicketStatus) row[1];
            Long count = (Long) row[2];
            if (status == TicketStatus.CLOSED) {
                closed.merge(labelId, count, Long::sum);
            } else {
                active.merge(labelId, count, Long::sum);
            }
        }
        return new LabelTicketCounts(active, closed);
    }

    public long activeFor(Long labelId) {
        return active.getOrDefault(labelId, 0L);
    }

    public long closedFor(Long labelId) {
        return closed.getOrDefault(labelId, 0L);
    }
}
