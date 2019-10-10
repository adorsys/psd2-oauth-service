package de.adorsys.xs2a.adapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "de.adorsys.xs2a.adapter")
public class OAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OAuthServiceApplication.class, args); //NOSONAR
    }
}