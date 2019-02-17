package com.github.chrisgleissner.jutil.sqllog.example;

import com.github.chrisgleissner.jutil.sqllog.SqlLog;
import com.github.chrisgleissner.jutil.sqllog.SqlRecording;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = "com.github.chrisgleissner.jutil.sqllog=false")
public class SqlLogExampleApplicationDisabledTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SqlLog sqlLog;

    @Before
    public void setUp() {
        sqlLog.clear();
    }

    @Test
    public void wontRecordIfDisabledByProperty() {
        try (SqlRecording recording = sqlLog.startRecording("example")) {
            jdbcTemplate.execute("create table foo (id int)");
            assertThat(sqlLog.getAllMessages()).isEmpty();
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }
}