package com.selflearntech.tech_blog_backend.service;

import com.selflearntech.tech_blog_backend.model.User;
import org.springframework.security.oauth2.jwt.Jwt;

public interface ITokenService {

    String createAccessToken(User user);

    String createRefreshToken(String subject);

    Jwt validateJWT(String token);
}
