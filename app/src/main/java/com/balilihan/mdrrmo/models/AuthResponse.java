package com.balilihan.mdrrmo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private long   userId;
    private String username;
    private String email;
    private int    roleId;
    private String role;
    private String accountStatus;
}