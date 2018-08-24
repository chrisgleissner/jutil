package uk.gleissner.jutil.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}