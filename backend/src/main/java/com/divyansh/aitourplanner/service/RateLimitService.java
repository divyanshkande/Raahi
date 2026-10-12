package com.divyansh.aitourplanner.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_REGISTER_ATTEMPTS = 5;
    private static final Duration AUTH_WINDOW = Duration.ofMinutes(15);

    @Value("${app.rate-limit.ai.requests-per-minute:10}")
    private int maxAiRequestsPerMinute;

    @Value("${app.rate-limit.ai.window-minutes:1}")
    private int aiWindowMinutes;

    private final Cache<String, Integer> loginAttempts = Caffeine.newBuilder()
            .expireAfterWrite(AUTH_WINDOW.toMinutes(), TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private final Cache<String, Integer> registerAttempts = Caffeine.newBuilder()
            .expireAfterWrite(AUTH_WINDOW.toMinutes(), TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    private final Cache<String, Integer> aiRequests = Caffeine.newBuilder()
            .expireAfterWrite(aiWindowMinutes, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public boolean checkLoginLimit(String ip) {
        Integer attempts = loginAttempts.getIfPresent(ip);
        return attempts == null || attempts < MAX_LOGIN_ATTEMPTS;
    }

    public void recordLoginAttempt(String ip) {
        Integer attempts = loginAttempts.getIfPresent(ip);
        loginAttempts.put(ip, (attempts == null ? 1 : attempts + 1));
    }

    public void recordLoginSuccess(String ip) {
        loginAttempts.invalidate(ip);
    }

    public boolean checkRegisterLimit(String ip) {
        Integer attempts = registerAttempts.getIfPresent(ip);
        return attempts == null || attempts < MAX_REGISTER_ATTEMPTS;
    }

    public void recordRegisterAttempt(String ip) {
        Integer attempts = registerAttempts.getIfPresent(ip);
        registerAttempts.put(ip, (attempts == null ? 1 : attempts + 1));
    }

    public int getRemainingLoginAttempts(String ip) {
        Integer attempts = loginAttempts.getIfPresent(ip);
        return Math.max(0, MAX_LOGIN_ATTEMPTS - (attempts == null ? 0 : attempts));
    }

    public int getRemainingRegisterAttempts(String ip) {
        Integer attempts = registerAttempts.getIfPresent(ip);
        return Math.max(0, MAX_REGISTER_ATTEMPTS - (attempts == null ? 0 : attempts));
    }

    // AI endpoint rate limiting
    public boolean checkAiLimit(String userEmail) {
        String key = "ai:" + userEmail;
        Integer attempts = aiRequests.getIfPresent(key);
        return attempts == null || attempts < maxAiRequestsPerMinute;
    }

    public void recordAiRequest(String userEmail) {
        String key = "ai:" + userEmail;
        Integer attempts = aiRequests.getIfPresent(key);
        aiRequests.put(key, (attempts == null ? 1 : attempts + 1));
    }

    public int getRemainingAiRequests(String userEmail) {
        String key = "ai:" + userEmail;
        Integer attempts = aiRequests.getIfPresent(key);
        return Math.max(0, maxAiRequestsPerMinute - (attempts == null ? 0 : attempts));
    }
}