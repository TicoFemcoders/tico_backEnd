package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.ActivationRequest;
import com.femcoders.tico.entity.User;
import com.femcoders.tico.enums.TokenType;

public interface ActivationService {

  public void activateAccount(ActivationRequest dto);

  public void resendCode(String email);

  public String generateCodeAndSaveToken(User user, TokenType type);

}
