package com.selflearntech.tech_blog_backend.service.impl;


import com.selflearntech.tech_blog_backend.model.Role;
import com.selflearntech.tech_blog_backend.model.User;
import com.selflearntech.tech_blog_backend.service.ITokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService implements ITokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final Clock clock;

    @Override
    public String createAccessToken(User user) {
        String roles = user.getAuthorities().stream().map(Role::getAuthority).collect(Collectors.joining(","));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now(clock))
                .expiresAt(Instant.now(clock).plus(10, ChronoUnit.SECONDS))
                .subject(user.getEmail())
                .claim("roles", roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public String createRefreshToken(String subject) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now(clock))
                .expiresAt(Instant.now(clock).plus(7, ChronoUnit.DAYS))
                .subject(subject)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Override
    public Jwt validateJWT(String token) {
        return jwtDecoder.decode(token);
    }
}
