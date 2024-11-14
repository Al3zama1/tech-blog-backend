package com.selflearntech.tech_blog_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Enumerated(value = EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleType authority;

    public Role() {
        super();
    }

    public String getAuthority() {
        return authority.name();
    }
}
