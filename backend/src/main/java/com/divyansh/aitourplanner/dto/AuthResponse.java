package com.divyansh.aitourplanner.dto;

public class AuthResponse {

    private String token;
    private String email;
    private String username;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String username, String message) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
