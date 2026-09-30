package com.backend.ms_security.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
	long id;
    @Column(
            nullable = false,
            length = 100
    )
	String name;
    @Column(
            nullable = false,
            unique = true,
            length = 150
    )
	String email;
    @Column(
            nullable = false
    )
	String password;
}
