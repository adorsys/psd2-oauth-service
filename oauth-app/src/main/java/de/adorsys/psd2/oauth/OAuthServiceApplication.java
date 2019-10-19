package de.adorsys.psd2.oauth;

import de.adorsys.xs2a.adapter.api.remote.Oauth2Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableConfigurationProperties
@EnableFeignClients(basePackageClasses = Oauth2Client.class)
@SpringBootApplication(scanBasePackages = "de.adorsys.psd2.oauth")
public class OAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OAuthServiceApplication.class, args); //NOSONAR
    }
}