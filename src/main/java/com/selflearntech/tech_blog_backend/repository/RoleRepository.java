package com.selflearntech.tech_blog_backend.repository;

import java.util.Optional;

import com.selflearntech.tech_blog_backend.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.selflearntech.tech_blog_backend.model.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByAuthority(RoleType role);

}
