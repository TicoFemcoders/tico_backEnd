package com.femcoders.tico.service;

import com.femcoders.tico.dto.ResetPasswordConfirm;
import com.femcoders.tico.entity.User;

public interface AuthService {

     public User getAuthenticatedUser();

     public User getOptionalAuthenticatedUser();

     public void requestReset(String email);

     public void confirmReset(ResetPasswordConfirm dto);

}