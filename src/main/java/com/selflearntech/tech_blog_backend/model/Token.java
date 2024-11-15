package com.selflearntech.tech_blog_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Integer tokenId;

    @Column(name = "refresh_token", length = 500, nullable = false)
    private String refreshToken;

    @Column(name = "expire_time", nullable = false)
    private Instant expireTime;

    @Column(name = "is_valid", nullable = false)
    private boolean isValid;

    @OneToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
