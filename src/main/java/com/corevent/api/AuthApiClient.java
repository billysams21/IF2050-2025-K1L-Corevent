package com.corevent.api;

import org.springframework.stereotype.Component;

import com.corevent.dto.auth.LoginRequest;
import com.corevent.dto.auth.LoginResponse;
import com.corevent.dto.auth.RegisterRequest;
import com.corevent.dto.auth.RegisterResponse;

import jakarta.annotation.PostConstruct;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

@Component
public class AuthApiClient extends BaseApiClient {

    private AuthApi authApi;

    @PostConstruct
    public void init() {
        retrofit = createRetrofit();
        authApi = retrofit.create(AuthApi.class);
    }

    public Call<LoginResponse> login(LoginRequest request) {
        return authApi.login(request);
    }

    public Call<RegisterResponse> register(RegisterRequest request) {
        return authApi.register(request);
    }

    public Call<Void> logout() {
        return authApi.logout();
    }

    public Call<LoginResponse> refreshToken(String refreshToken) {
        return authApi.refreshToken(refreshToken);
    }

    public Call<Void> validateToken(String token) {
        return authApi.validateToken(token);
    }

    // API Interface
    public interface AuthApi {
        @POST("/api/auth/login")
        Call<LoginResponse> login(@Body LoginRequest request);

        @POST("/api/auth/register")
        Call<RegisterResponse> register(@Body RegisterRequest request);

        @POST("/api/auth/logout")
        Call<Void> logout();

        @POST("/api/auth/refresh")
        Call<LoginResponse> refreshToken(@Query("token") String refreshToken);

        @GET("/api/auth/validate")
        Call<Void> validateToken(@Header("Authorization") String token);
    }
}