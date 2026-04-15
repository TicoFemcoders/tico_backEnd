package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.ResetPasswordConfirmDTO;

public interface ResetPasswordService {

  public void requestReset(String email);

  public void confirmReset(ResetPasswordConfirmDTO dto);
}
