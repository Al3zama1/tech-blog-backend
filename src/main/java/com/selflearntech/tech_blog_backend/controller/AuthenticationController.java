package com.selflearntech.tech_blog_backend.controller;

import com.selflearntech.tech_blog_backend.dto.*;
import com.selflearntech.tech_blog_backend.mapper.UserMapper;
import com.selflearntech.tech_blog_backend.service.impl.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@Valid @RequestBody RegistrationDTO registrationDTO) {
        authenticationService.registerUser(registrationDTO);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UserDTO> authenticateUser(@Valid @RequestBody AuthenticationDTO authenticationDTO) {
        UserWithRefreshAndAccessTokenDTO userWithRefreshAndAccessTokenDTO = authenticationService
                .authenticateUser(authenticationDTO.getEmail(), authenticationDTO.getPassword());
        UserDTO userDTO = userMapper.toUserDTO(userWithRefreshAndAccessTokenDTO);

        ResponseCookie refreshToken = ResponseCookie.from("refresh-token", userWithRefreshAndAccessTokenDTO.getRefreshToken())
                .domain("localhost")
                .path("/auth/refresh")
                .httpOnly(true)
                .maxAge(Duration.ofDays(7))
                .build();

       return  ResponseEntity
               .ok()
               .header(HttpHeaders.SET_COOKIE, refreshToken.toString())
               .body(userDTO);


    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AccessTokenDTO refreshAccessToken(@CookieValue(name = "refresh-token") String refreshToken) {
        String accessToken = authenticationService.refreshAccessToken(refreshToken);
        return new AccessTokenDTO(accessToken);

    }

}
