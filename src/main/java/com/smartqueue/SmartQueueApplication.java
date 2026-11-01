package com.smartqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartQueueApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartQueueApplication.class, args);
        System.out.println("===============================================================");
        System.out.println("  Smart Digital Queue & Crowd Prediction System Started!");
        System.out.println("  Web Portal:        http://localhost:8080");
        System.out.println("  H2 DB Console:     http://localhost:8080/h2-console");
        System.out.println("  JDBC URL:          jdbc:h2:file:./data/smartqueue");
        System.out.println("===============================================================");
    }
}
