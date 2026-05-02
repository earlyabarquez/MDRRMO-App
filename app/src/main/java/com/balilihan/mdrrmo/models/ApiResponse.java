package com.balilihan.mdrrmo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApiResponse {
    private boolean success;
    private String  message;
}