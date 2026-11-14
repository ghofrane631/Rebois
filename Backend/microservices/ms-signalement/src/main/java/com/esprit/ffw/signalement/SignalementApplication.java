package com.esprit.ffw.signalement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SignalementApplication {
    public static void main(String[] args) {
        SpringApplication.run(SignalementApplication.class, args);
    }
}
