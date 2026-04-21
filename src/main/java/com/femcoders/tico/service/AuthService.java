package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.ResetPasswordConfirmDTO;
import com.femcoders.tico.entity.User;

public interface AuthService {

     public User getAuthenticatedUser();

     public User getOptionalAuthenticatedUser();

     public void requestReset(String email);

     public void confirmReset(ResetPasswordConfirmDTO dto);

}