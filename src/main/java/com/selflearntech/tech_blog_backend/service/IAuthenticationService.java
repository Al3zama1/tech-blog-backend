package com.selflearntech.tech_blog_backend.service;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.dto.UserWithRefreshAndAccessTokenDTO;

public interface IAuthenticationService {
    void registerUser(RegistrationDTO registrationDTO);

    UserWithRefreshAndAccessTokenDTO authenticateUser(String email, String password);
}
