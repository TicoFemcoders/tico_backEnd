package com.femcoders.tico.service;

import com.femcoders.tico.entity.User;

public interface AuthService {

     public User getAuthenticatedUser();

     public User getOptionalAuthenticatedUser();
}