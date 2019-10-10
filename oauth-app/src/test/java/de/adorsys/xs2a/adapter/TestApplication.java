package de.adorsys.xs2a.adapter;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;


@SpringBootApplication(scanBasePackages = "de.adorsys.xs2a.adapter")
@ActiveProfiles("test")
public class TestApplication {
}
