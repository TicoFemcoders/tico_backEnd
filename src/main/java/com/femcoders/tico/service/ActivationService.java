package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.ActivationReqDTO;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;

public interface ActivationService {

  public void activateAccount(ActivationReqDTO dto);

  public void resendCode(String email);

  public String generateCodeAndSaveToken(User user, TokenType type);

}
