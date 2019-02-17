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
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SqlLogExampleApplicationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SqlLog sqlLog;

    @Before
    public void setUp() {
        sqlLog.clear();
    }

    @Test
    public void recordingWorks() {
        try (SqlRecording recording = sqlLog.startRecording("example")) {
            jdbcTemplate.execute("create table foo (id int)");
            jdbcTemplate.execute("insert into foo (id) values (1)");
            assertThat(jdbcTemplate.queryForObject("select count (*) from foo where id = 1", Integer.class)).isEqualTo(1);
            String[] msgs = {"{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"create table foo (id int)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into foo (id) values (1)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select count (*) from foo where id = 1\"], \"params\":[]}"};
            assertThat(sqlLog.getMessagesContaining("table foo")).containsExactlyInAnyOrder(msgs);
            assertThat(sqlLog.getMessagesContaining("foo")).containsExactlyInAnyOrder(msgs);
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }
}