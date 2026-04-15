package com.femcoders.tico.service;

import com.femcoders.tico.entity.User;

public interface AuthService {

     User getAuthenticatedUser();

     User getOptionalAuthenticatedUser();
}