package uk.gleissner.jutil.spring.batch.rest;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
@EnableBatchProcessing
public class SpringBatchRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchRestApplication.class, args);
    }
}
