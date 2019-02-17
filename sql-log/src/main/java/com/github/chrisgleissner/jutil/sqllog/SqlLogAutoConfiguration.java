package com.github.chrisgleissner.jutil.sqllog;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class SqlLogAutoConfiguration {

    @Bean
    SqlLog sqlLog(@Value("${com.github.chrisgleissner.jutil.sqllog:true}") boolean sqlLogEnabled,
                  @Value("${com.github.chrisgleissner.jutil.sqllog.log-queries:false}") boolean logQueries,
                  @Value("${com.github.chrisgleissner.jutil.sqllog.trace-methods:false}") boolean traceMethods) {
        SqlLog sqlLog = new SqlLog(sqlLogEnabled, logQueries, traceMethods);
        log.debug("Created {}", sqlLog);
        return sqlLog;
    }
}
