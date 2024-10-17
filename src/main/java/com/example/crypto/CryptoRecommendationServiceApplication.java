package com.example.crypto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoRecommendationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoRecommendationServiceApplication.class, args);
    }

}
