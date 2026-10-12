package com.divyansh.aitourplanner.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RateLimitServiceTest {

    @Test
    void checkLoginLimit_firstAttempt_returnsTrue() {
        RateLimitService service = new RateLimitService();
        assertThat(service.checkLoginLimit("192.168.1.1")).isTrue();
    }

    @Test
    void checkLoginLimit_upToMaxAttempts_returnsTrue() {
        RateLimitService service = new RateLimitService();
        String ip = "192.168.1.2";
        for (int i = 0; i < 5; i++) {
            assertThat(service.checkLoginLimit(ip)).isTrue();
            service.recordLoginAttempt(ip);
        }
    }

    @Test
    void checkLoginLimit_exceedsMaxAttempts_returnsFalse() {
        RateLimitService service = new RateLimitService();
        String ip = "192.168.1.3";
        for (int i = 0; i < 5; i++) {
            service.recordLoginAttempt(ip);
        }
        assertThat(service.checkLoginLimit(ip)).isFalse();
    }

    @Test
    void recordLoginSuccess_resetsAttempts() {
        RateLimitService service = new RateLimitService();
        String ip = "192.168.1.4";
        for (int i = 0; i < 5; i++) {
            service.recordLoginAttempt(ip);
        }
        assertThat(service.checkLoginLimit(ip)).isFalse();

        service.recordLoginSuccess(ip);
        assertThat(service.checkLoginLimit(ip)).isTrue();
    }

    @Test
    void checkRegisterLimit_sameBehaviorAsLogin() {
        RateLimitService service = new RateLimitService();
        String ip = "192.168.1.5";
        for (int i = 0; i < 5; i++) {
            assertThat(service.checkRegisterLimit(ip)).isTrue();
            service.recordRegisterAttempt(ip);
        }
        assertThat(service.checkRegisterLimit(ip)).isFalse();
    }

    @Test
    void getRemainingLoginAttempts_decrementsCorrectly() {
        RateLimitService service = new RateLimitService();
        String ip = "192.168.1.6";
        assertThat(service.getRemainingLoginAttempts(ip)).isEqualTo(5);

        service.recordLoginAttempt(ip);
        assertThat(service.getRemainingLoginAttempts(ip)).isEqualTo(4);

        service.recordLoginAttempt(ip);
        assertThat(service.getRemainingLoginAttempts(ip)).isEqualTo(3);
    }

    @Test
    void getRemainingRegisterAttempts_decrementsCorrectly() {
        RateLimitService service = new RateLimitService();
        String ip = "192.168.1.7";
        assertThat(service.getRemainingRegisterAttempts(ip)).isEqualTo(5);
        service.recordRegisterAttempt(ip);
        assertThat(service.getRemainingRegisterAttempts(ip)).isEqualTo(4);
    }

    @Test
    void differentIPs_haveSeparateCounters() {
        RateLimitService service = new RateLimitService();
        service.recordLoginAttempt("192.168.1.10");
        service.recordLoginAttempt("192.168.1.10");
        service.recordLoginAttempt("192.168.1.10");
        service.recordLoginAttempt("192.168.1.10");
        service.recordLoginAttempt("192.168.1.10");

        assertThat(service.checkLoginLimit("192.168.1.10")).isFalse();
        assertThat(service.checkLoginLimit("192.168.1.11")).isTrue();
    }
}