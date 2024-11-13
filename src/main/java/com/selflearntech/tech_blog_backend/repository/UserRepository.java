package com.selflearntech.tech_blog_backend.repository;

import com.selflearntech.tech_blog_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

    Optional<User> findByEmail(String email);
    boolean existsUserByEmail(String email);
}
