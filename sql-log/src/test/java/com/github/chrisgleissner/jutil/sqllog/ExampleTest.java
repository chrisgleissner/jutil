package com.github.chrisgleissner.jutil.sqllog;

import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExampleTest {

    public static final File FILE = new File("target/example.json");

    @TestConfiguration
    static class ExampleConfig {
        ExampleConfig(JdbcTemplate jdbcTemplate, SqlLog sqlLog) {
            try (SqlRecording recording = sqlLog.startRecording("example", FILE, Charset.forName("UTF-8"))) {
                jdbcTemplate.execute("create table foo (id int)");
                jdbcTemplate.execute("insert into foo (id) values (1)");
            } finally {
                jdbcTemplate.execute("drop table foo");
            }
        }
    }

    @BeforeClass
    public static void setUp() {
        if (FILE.exists())
            FILE.delete();
    }

    @Test
    public void canWriteToFile() {
        assertThat(FILE).hasContent("[{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"create table foo (id int)\"], \"params\":[]},\n" +
                "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into foo (id) values (1)\"], \"params\":[]}]");
    }
}
