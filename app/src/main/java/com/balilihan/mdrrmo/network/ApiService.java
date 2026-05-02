// network/ApiService.java
// Retrofit turns these interface methods into real HTTP calls.
// We'll add more endpoints here as we build each feature.

package com.balilihan.mdrrmo.network;

import com.balilihan.mdrrmo.models.ApiResponse;
import com.balilihan.mdrrmo.models.AuthRequest;
import com.balilihan.mdrrmo.models.AuthResponse;
import com.balilihan.mdrrmo.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Login — returns JWT token + user info
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    // Register — returns success/error message
    // Account is PENDING until admin approves from web app
    @POST("api/auth/register")
    Call<ApiResponse> register(@Body RegisterRequest request);
}