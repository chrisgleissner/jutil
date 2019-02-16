package com.github.chrisgleissner.jutil.sqllog.example;

import com.github.chrisgleissner.jutil.sqllog.SqlLog;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SqlLogExampleApplicationDisabledTest {

    @Autowired
    private ApplicationContext ctx;

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void sqlLogBeanDoesNotExist() {
        ctx.getBean(SqlLog.class);
    }
}