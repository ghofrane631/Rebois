package tn.reboistn.restauration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class RestaurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurationApplication.class, args);
    }
}