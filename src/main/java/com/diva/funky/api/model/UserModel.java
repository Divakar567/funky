package com.diva.funky.api.model;

import java.io.Serializable;
import java.time.Instant;

import com.diva.funky.api.repository.entity.UserEntity.Gender;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserModel implements Serializable {

    private String id;

    private String username;

    private String email;

    private String firstname;

    private String lastname;

    private Gender gender;

    private Instant dateOfBirth;

    private String createdBy;

    private String lastUpdatedBy;

    private Instant createdDate;

    private Instant lastUpdatedDate;
}
