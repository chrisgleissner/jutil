package com.github.chrisgleissner.jutil.sqllog;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@TestPropertySource(properties = "com.github.chrisgleissner.jutil.sqllog=true")
@SpringBootTest
public class SqlLogTest {

    @Autowired
    private PersonRepo repo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SqlLog sqlLog;

    @Before
    public void setUp() {
        sqlLog.clearAll();
        sqlLog.startRecording("default");
    }

    @Test
    public void getLogsContaining() {
        repo.save(new Person("Jack", "Bauer"));
        Collection<String> matchingLogs = sqlLog.getLogsContaining("insert into person");
        assertThat(matchingLogs).containsExactly(
                "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"call next value for hibernate_sequence\"], \"params\":[[]]}",
                "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into person (first_name, last_name, id) values (?, ?, ?)\"], \"params\":[[\"Jack\",\"Bauer\",\"1\"]]}");
    }

    @Test
    public void getLogsContainingRegex() {
        repo.findAll();
        Collection<String> matchingLogs = sqlLog.getLogsContainingRegex("select.*?from person");
        assertThat(matchingLogs).containsExactly(
                "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_\"], \"params\":[[]]}");
    }

    @Test
    public void getLogsForThreadId() {
        String id = UUID.randomUUID().toString();
        sqlLog.startRecording(id);
        repo.findAll();
        String msg = "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_\"], \"params\":[[]]}";
        assertThat(sqlLog.getLogsById(id)).containsExactly(msg);

        repo.findByLastName("Bauer");
        String[] msgs = {"{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_\"], \"params\":[[]]}",
                "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_ where person0_.last_name=?\"], \"params\":[[\"Bauer\"]]}"};
        assertThat(sqlLog.getLogsById(id)).containsExactly(msgs);
        assertThat(sqlLog.startRecording(id)).containsExactlyInAnyOrder(msgs);
        assertThat(sqlLog.getLogsById(id)).isEmpty();

        repo.findAll();
        assertThat(sqlLog.getLogsById(id)).containsExactly(msg);
    }

    @Test
    public void getLogsContainingForJdbc() {
        try {
            jdbcTemplate.execute("create table foo (id int)");
            jdbcTemplate.execute("insert into foo (id) values (1)");
            assertThat(jdbcTemplate.queryForObject("select count (*) from foo where id = 1", Integer.class)).isEqualTo(1);

            String[] msgs = {"{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"create table foo (id int)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into foo (id) values (1)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select count (*) from foo where id = 1\"], \"params\":[]}"};
            assertThat(sqlLog.getLogsContaining("table foo")).containsExactlyInAnyOrder(msgs);
            assertThat(sqlLog.getLogsContaining("foo")).containsExactlyInAnyOrder(msgs);
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }

    @Test
    public void streamRecording() throws IOException {
        assertThat(sqlLog.getLogs()).isEmpty();
        File file = File.createTempFile("test", ".json");
        file.deleteOnExit();
        try (OutputStream os = new FileOutputStream(file)) {
            sqlLog.startRecording("test", os, Charset.forName("UTF-8"));
            jdbcTemplate.execute("create table foo (id int)");
            jdbcTemplate.execute("insert into foo (id) values (1)");
            assertThat(sqlLog.getLogs()).isEmpty();
            sqlLog.stopRecording("test");
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
        assertThat(file).hasSameContentAs(new File("src/test/resources/streamRecording.json"));
    }

    private static File createTempFile(String name) {
        try {
            File file = File.createTempFile(name, ".json");
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void concurrentStreamRecording() throws InterruptedException {
        try {
            Map<String, File> filesById = IntStream.range(0, 2).mapToObj((i) -> UUID.randomUUID().toString())
                    .collect(Collectors.toMap(Function.identity(), SqlLogTest::createTempFile));

            assertThat(sqlLog.getLogs()).hasSize(0);
            jdbcTemplate.execute("create table foo (id varchar)");
            assertThat(sqlLog.getLogs()).hasSize(1);
            sqlLog.clearAll();

            CountDownLatch endLatch = new CountDownLatch(filesById.size());
            filesById.entrySet().forEach(entry ->
                    new Thread(() -> {
                        try (OutputStream os = new FileOutputStream(entry.getValue())) {
                            sqlLog.startRecording(entry.getKey(), os, Charset.forName("UTF-8"));
                            jdbcTemplate.execute(format("insert into foo (id) values ('%s')", entry.getKey()));
                            sqlLog.stopRecording(entry.getKey());
                            endLatch.countDown();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, entry.getKey()).start());
            endLatch.await(2, SECONDS);
            assertThat(sqlLog.getLogs()).isEmpty();

            filesById.entrySet().forEach(entry -> {
                assertThat(entry.getValue()).hasContent(String.format("[{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, " +
                                "\"batchSize\":0, \"query\":[\"insert into foo (id) values ('%s')\"], \"params\":[]}]", entry.getKey()));
            });
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }

    @Transactional
    @Test
    public void getLogsContainingForTransactionalJdbc() {
        try {
            jdbcTemplate.execute("create table foo (id int)");
            jdbcTemplate.execute("insert into foo (id) values (1)");
            assertThat(jdbcTemplate.queryForObject("select count (*) from foo where id = 1", Integer.class)).isEqualTo(1);

            Collection<String> matchingLogs = sqlLog.getLogsContaining("table foo");
            String[] msgs = {"{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"create table foo (id int)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into foo (id) values (1)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select count (*) from foo where id = 1\"], \"params\":[]}"};
            assertThat(matchingLogs).containsExactly(msgs);
            matchingLogs = sqlLog.getLogsContaining("foo");
            assertThat(matchingLogs).containsExactly(msgs);
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }

    @Test
    public void getLogsForThreadIdForConcurrentJdbc() throws InterruptedException {
        try {
            List<String> ids = new ArrayList<>();
            ids.add(UUID.randomUUID().toString());
            ids.add(UUID.randomUUID().toString());

            assertThat(sqlLog.getLogs()).hasSize(0);
            jdbcTemplate.execute("create table foo (id varchar)");
            assertThat(sqlLog.getLogs()).hasSize(1);

            CountDownLatch endLatch = new CountDownLatch(ids.size());
            ids.forEach(id ->
                    new Thread(() -> {
                        sqlLog.startRecording(id);
                        jdbcTemplate.execute(format("insert into foo (id) values ('%s')", id));
                        endLatch.countDown();
                    }, id).start());
            endLatch.await(2, SECONDS);

            ids.forEach(id -> assertThat(sqlLog.getLogsById(id)).containsExactly(format(
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, " +
                            "\"batchSize\":0, \"query\":[\"insert into foo (id) values ('%s')\"], \"params\":[]}", id)));
            assertThat(sqlLog.getLogs()).hasSize(ids.size());
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }
}

