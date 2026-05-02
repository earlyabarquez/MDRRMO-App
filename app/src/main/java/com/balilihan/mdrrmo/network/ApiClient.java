// network/ApiClient.java
// Singleton — only one Retrofit instance ever exists.
// All API calls go through this.

package com.balilihan.mdrrmo.network;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // ── Change this to your Spring Boot server IP ──────────────
    // During development: your laptop's LAN IP (e.g. 192.168.1.5)
    // Android emulator: use 10.0.2.2 to reach localhost
    // Production: your deployed server URL
    private static final String BASE_URL = "http://192.168.1.5:8080/";

    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context)) // JWT auto-attach
                    .addInterceptor(logging)
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }


    // Returns a ready-to-use ApiService instance
    public static ApiService getApiService(Context context) {
        return getClient(context).create(ApiService.class);
    }

    }