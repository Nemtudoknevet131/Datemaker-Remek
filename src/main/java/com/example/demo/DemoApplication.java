package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemoApplication {

    public static void main(String[] args) {
        // Ez a metódus indítja el a teljes Spring Boot alkalmazást.
        SpringApplication.run(DemoApplication.class, args);
    }
}