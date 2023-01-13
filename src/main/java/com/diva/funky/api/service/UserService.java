package com.diva.funky.api.service;

import org.springframework.stereotype.Service;

import com.diva.funky.api.model.UserModel;

@Service
public class UserService {

    public UserModel getUserByUsername(String name) {
        return UserModel.builder()
                .username(name)
                .build();
    }
}
