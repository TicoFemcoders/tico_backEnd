package com.femcoders.tico.service;

import java.util.List;

import com.femcoders.tico.dto.request.TicketMessageRequest;
import com.femcoders.tico.dto.response.TicketMessageResponse;

public interface TicketMessageService {

    List<TicketMessageResponse> getMessagesByTicketId(Long ticketId);

    TicketMessageResponse createMessage(Long ticketId, TicketMessageRequest dto);

    void deleteMessage(Long id);

}
