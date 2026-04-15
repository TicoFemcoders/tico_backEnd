package com.femcoders.tico.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.femcoders.tico.entity.User;
import com.femcoders.tico.exception.ResourceNotFoundException;
import com.femcoders.tico.repository.UserRepository;
import com.femcoders.tico.security.UserDetail;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

private final UserRepository userRepository;
    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("No autorizado: Debes estar logueado.");
        }
        String email = authentication.getPrincipal().toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email",email));
    }

    @Override
    public User getOptionalAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && 
            authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof UserDetail) {
            
            UserDetail userDetail = (UserDetail) authentication.getPrincipal();
            return userRepository.findById(userDetail.getUser().getId()).orElse(null);
        }
        return null;
    }
}