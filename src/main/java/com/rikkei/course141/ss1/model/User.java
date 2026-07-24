package com.rikkei.course141.ss1.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "users") @Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true) private String username;
    private String password;
    private String email;
    private String role;
    private boolean enabled = false;
    private String otpCode;
    private LocalDateTime otpExpiration;
}
