package com.servicelink.dto;

public class AuthResponse {
    private String token;
    private UserDtos.Response user;

    public AuthResponse() {}
    public AuthResponse(String token, UserDtos.Response user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserDtos.Response getUser() { return user; }
    public void setUser(UserDtos.Response user) { this.user = user; }
}