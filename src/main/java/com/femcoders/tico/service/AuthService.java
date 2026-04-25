package com.femcoders.tico.service;

import com.femcoders.tico.dto.request.ResetPasswordConfirmRequest;
import com.femcoders.tico.entity.User;

public interface AuthService {

     public User getAuthenticatedUser();

     public User getOptionalAuthenticatedUser();

     public void requestReset(String email);

     public void confirmReset(ResetPasswordConfirmRequest dto);

}