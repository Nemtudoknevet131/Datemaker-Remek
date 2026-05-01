package com.example.demo.security;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FireBaseConfig {

    private final SecurityConfig securityConfig;

    FireBaseConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @PostConstruct
    public void init() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("firebase-service-account.json");

                if (serviceAccount == null) {
                    throw new IllegalStateException("firebase-service-account.json not found on classpath");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Fireabase has been initialized");
            } else {
                log.info("FirebaseApp already initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
        }
    }

}