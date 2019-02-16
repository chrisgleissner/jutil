package com.github.chrisgleissner.jutil.sqllog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class SqlLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlLogApplication.class, args);
    }
}

