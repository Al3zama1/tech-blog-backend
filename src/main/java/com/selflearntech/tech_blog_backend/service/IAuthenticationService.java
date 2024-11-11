package com.selflearntech.tech_blog_backend.service;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;

public interface IAuthenticationService {
    void registerUser(RegistrationDTO registrationDTO);
}
