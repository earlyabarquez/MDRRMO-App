// network/AuthInterceptor.java
// OkHttp interceptor — runs on every outgoing HTTP request.
// Reads the JWT token from SessionManager and attaches it
// as a Bearer token in the Authorization header.
// Spring Boot reads this header to verify the user is logged in.

package com.balilihan.mdrrmo.network;

import android.content.Context;

import com.balilihan.mdrrmo.utils.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager session;

    public AuthInterceptor(Context context) {
        this.session = SessionManager.getInstance(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = session.getToken();

        Request original = chain.request();

        // If no token exists (user not logged in), send request as-is
        // This covers login and register endpoints which don't need auth
        if (token == null) {
            return chain.proceed(original);
        }

        // Attach JWT token to the Authorization header
        // Spring Boot reads "Bearer <token>" to authenticate the request
        Request authenticated = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authenticated);
    }
}