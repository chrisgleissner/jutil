package com.github.chrisgleissner.jutil.sqllog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "com.github.chrisgleissner.jutil.sqllog")
public class SqlLogAutoConfiguration {

    @Bean
    SqlLog sqlLog(@Value("${com.github.chrisgleissner.jutil.sqllog.log-queries:false}") boolean logQueries,
                  @Value("${com.github.chrisgleissner.jutil.sqllog.trace-methods:false}") boolean traceMethods) {
        return new SqlLog(logQueries, traceMethods);
    }
}
