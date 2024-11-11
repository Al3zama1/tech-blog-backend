package com.selflearntech.tech_blog_backend.repository;

import com.selflearntech.tech_blog_backend.model.Token;
import com.selflearntech.tech_blog_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);
    Optional<Token> findByUser(User user);
}
