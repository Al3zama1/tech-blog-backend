package com.selflearntech.tech_blog_backend.service.impl;

import com.selflearntech.tech_blog_backend.dto.RegistrationDTO;
import com.selflearntech.tech_blog_backend.dto.UserWithRefreshAndAccessTokenDTO;
import com.selflearntech.tech_blog_backend.exception.*;
import com.selflearntech.tech_blog_backend.mapper.UserMapper;
import com.selflearntech.tech_blog_backend.model.Role;
import com.selflearntech.tech_blog_backend.model.RoleType;
import com.selflearntech.tech_blog_backend.model.Token;
import com.selflearntech.tech_blog_backend.model.User;
import com.selflearntech.tech_blog_backend.repository.RoleRepository;
import com.selflearntech.tech_blog_backend.repository.UserRepository;
import com.selflearntech.tech_blog_backend.service.IAuthenticationService;
import com.selflearntech.tech_blog_backend.service.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final ITokenService tokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final Clock clock;

    @Override
    public void registerUser(RegistrationDTO registrationDTO) {
        if (!registrationDTO.getPassword().equals(registrationDTO.getVerifyPassword()))
            throw new BadRequestException(ErrorMessages.PASSWORDS_MUST_MATCH);

        if (userRepository.existsUserByEmail(registrationDTO.getEmail()))
            throw new UserExistsException(ErrorMessages.USER_EXISTS);

        Role userRole = roleRepository.findByAuthority(RoleType.USER).orElseThrow(() -> new RoleAssignmentException(ErrorMessages.ROLE_ASSIGNMENT_FAILURE, RoleType.USER.name()));
        String encodedPassword = passwordEncoder.encode(registrationDTO.getPassword());
        User user = User.builder()
                .firstName(registrationDTO.getFirstName())
                .lastName(registrationDTO.getLastName())
                .email(registrationDTO.getEmail())
                .password(encodedPassword)
                .authorities(Set.of(userRole))
                .build();

        userRepository.save(user);
    }

    @Override
    public UserWithRefreshAndAccessTokenDTO authenticateUser(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = (User) authentication.getPrincipal();
        String accessToken = tokenService.createAccessToken(user);
        String refreshToken = tokenService.createRefreshToken(user.getEmail());
        Jwt refreshJwt;

        try {
            refreshJwt = tokenService.validateJWT(refreshToken);
        } catch (JwtException ex) {
            throw new RuntimeException(ErrorMessages.INVALID_REFRESH_TOKEN);
        }

        Token token = Token.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expireTime(refreshJwt.getExpiresAt())
                .isValid(true)
                .build();
        user.setToken(token);
        user = userRepository.save(user);

        return userMapper.toUserWithAccessAndRefreshTokenDTO(user, accessToken);
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        Jwt decodedJwt;

        try {
            decodedJwt = tokenService.validateJWT(refreshToken);
        } catch (JwtException ex) {
            throw new RefreshTokenException(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.FAIL_TOKEN_DECODE);
        }

        String email = decodedJwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RefreshTokenException(ErrorMessages.INVALID_REFRESH_TOKEN));

        Token storedToken = user.getToken();
        Instant now = Instant.now(clock);

        if (!storedToken.isValid()) throw new RefreshTokenException(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.INVALIDATED_REFRESH_TOKEN);
        if (!storedToken.getRefreshToken().equals(refreshToken)) throw new RefreshTokenException(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.COOKIE_REFRESH_TOKEN_AND_DB_TOKEN_UNMATCH);
        if (storedToken.getExpireTime().isBefore(now)) throw new RefreshTokenException(ErrorMessages.INVALID_REFRESH_TOKEN + ": " + ErrorMessages.EXPIRED_REFRESH_TOKEN);

        return tokenService.createAccessToken(user);
    }
}
