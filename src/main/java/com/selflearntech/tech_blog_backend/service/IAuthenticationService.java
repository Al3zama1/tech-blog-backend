package com.selflearntech.tech_blog_backend.service;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.model.User;

public interface IAuthenticationService {
    void registerUser(RegistrationDTO registrationDTO);

    User authenticateUser(String email, String password);
}
