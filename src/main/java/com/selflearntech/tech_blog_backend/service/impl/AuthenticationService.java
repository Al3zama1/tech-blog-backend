package com.selflearntech.tech_blog_backend.service.impl;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.model.User;
import com.selflearntech.tech_blog_backend.service.IAuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements IAuthenticationService {

    @Override
    public void registerUser(RegistrationDTO registrationDTO) {

    }

    @Override
    public User authenticateUser(String email, String password) {

        return null;
    }
}
