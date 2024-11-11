package com.selflearntech.tech_blog_backend.test_data;

import com.selflearntech.tech_blog_backend.dto.AuthenticationDTO;

public class AuthenticationDTOMother {

    public static AuthenticationDTO.AuthenticationDTOBuilder complete() {
        return AuthenticationDTO.builder()
                .email("john.doe@gmail.com")
                .password("C11l08a#0522");
    }
}
