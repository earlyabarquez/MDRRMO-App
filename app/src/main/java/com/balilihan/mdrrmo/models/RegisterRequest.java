package com.balilihan.mdrrmo.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
}