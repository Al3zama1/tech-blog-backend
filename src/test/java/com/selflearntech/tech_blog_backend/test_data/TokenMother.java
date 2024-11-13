package com.selflearntech.tech_blog_backend.test_data;

import com.selflearntech.tech_blog_backend.model.Token;

public class TokenMother {
    public static Token.TokenBuilder complete() {
        return Token.builder()
                .tokenId(1)
                .isValid(true)
                .refreshToken("token");
    }
}
