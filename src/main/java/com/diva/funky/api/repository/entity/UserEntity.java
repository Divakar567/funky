package com.diva.funky.api.repository.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")
@Table(name = "`user`")
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "`id`")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "`username`", nullable = false, updatable = false)
    private String username;

    @Column(name = "`password`", updatable = false)
    private String password;

    @Column(name = "`email`", nullable = false)
    private String email;

    @Column(name = "`firstname`", nullable = false)
    private String firstname;

    @Column(name = "`lastname`", nullable = false)
    private String lastname;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "`gender`", nullable = false)
    private Gender gender;

    @Column(name = "`date_of_birth`", nullable = false)
    private Instant dateOfBirth;

    public enum Gender {
        MALE, FEMALE,
    }
}
