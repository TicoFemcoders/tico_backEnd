package com.femcoders.tico.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.dto.response.TicketMessageResponse;

public interface TicketMessageService {

    public Page<TicketMessageResponse> getMessagesByTicketId(Long ticketId, Pageable pageable);

    public TicketMessageResponse createMessage(Long ticketId, TicketMessageRequest dto);

}
