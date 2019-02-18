package com.github.chrisgleissner.jutil.sqllog;


import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SqlLogTest {

    @Autowired
    private PersonRepo repo;

    @SpyBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SqlLog sqlLog;

    private static File createTempFile(String name) {
        try {
            File file = File.createTempFile(name, ".json");
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() {
        sqlLog.clear();
    }

    @Test
    public void canStartRecordingIfAlreadyRecording() {
        sqlLog.startRecording("foo");
        sqlLog.stopRecording("foo");
        sqlLog.startRecording("baz");
    }

    @Test
    public void attemptToRestartRecordingReturnsAlreadyStartedRecording() {
        SqlRecording recording = sqlLog.startRecording("foo");
        repo.findByLastName("A");
        assertThat(sqlLog.startRecording("foo")).isSameAs(recording);
        assertThat(recording.getMessages()).containsExactly(findByLastNameSql("A"));
    }

    @Test
    public void queryTransformerWorks() {
        try {
            try {
                jdbcTemplate.execute("create table foo (id int)");
                sqlLog.setQueryTransformer(transformInfo -> "insert into foo (id) values (2)");
                jdbcTemplate.execute("insert into foo (id) values (1)");
            } finally {
                sqlLog.setQueryTransformer(SqlLog.identityQueryTransformer);
            }
            assertThat(jdbcTemplate.queryForObject("select count(*) from foo where id = 1", Integer.class)).isEqualTo(0);
            assertThat(jdbcTemplate.queryForObject("select count(*) from foo where id = 2", Integer.class)).isEqualTo(1);
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }

    @Test
    public void noOpQueryTransformerWorks() {
        try {
            sqlLog.setPropagateCallsToDbEnabled(false);
            jdbcTemplate.execute("invalid sql");
        } finally {
            sqlLog.setPropagateCallsToDbEnabled(true);
        }
    }

    @Test
    public void defaultRecording() {
        assertThat(sqlLog.getDefaultRecording()).isSameAs(sqlLog.getRecording(SqlLog.DEFAULT_ID));
    }

    private String findByLastNameSql(String lastName) {
        return String.format("{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, " +
                "\"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ " +
                "from person person0_ where person0_.last_name=?\"], \"params\":[[\"%s\"]]}", lastName);
    }

    @Test
    public void defaultRecordingAlwaysEnabled() {
        assertThat(sqlLog.getDefaultRecording().getMessages()).isEmpty();
        repo.findByLastName("A");

        try (SqlRecording recording = sqlLog.startRecording("test")) {
            repo.findByLastName("B");
            assertThat(recording.getMessages()).containsExactly(findByLastNameSql("B"));
        }

        repo.findByLastName("C");
        assertThat(sqlLog.getDefaultRecording().getMessages()).containsExactly(findByLastNameSql("A"), findByLastNameSql("C"));

        sqlLog.getDefaultRecording().clear();
        assertThat(sqlLog.getDefaultRecording().getMessages()).isEmpty();
    }


    @Test
    public void canStopRecordingThatDoesntExist() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> sqlLog.stopRecording("bar"))
                .withMessageContaining("Can't stop recording with ID bar since it doesn't exist");
    }

    @Test
    public void getMessagesContaining() {
        try (SqlRecording recording = sqlLog.startRecording("test")) {
            repo.save(new Person("Hans", "Müllerʤ"));
            Collection<String> matchingLogs = sqlLog.getMessagesContaining("insert into person");
            assertThat(matchingLogs).containsExactly(
                    "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"call next value for hibernate_sequence\"], \"params\":[[]]}",
                    "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into person (first_name, last_name, id) values (?, ?, ?)\"], \"params\":[[\"Hans\",\"Müllerʤ\",\"1\"]]}");
        }
    }

    @Test
    public void setSqlLogEnabled() {
        try (SqlRecording recording = sqlLog.startRecording("test")) {
            try {
                sqlLog.setEnabled(false);
                repo.findByLastName("A");
                assertThat(sqlLog.getAllMessages()).isEmpty();
            } finally {
                sqlLog.setEnabled(true);
                repo.findByLastName("A");
                assertThat(sqlLog.getAllMessages()).containsExactly(findByLastNameSql("A"));
            }
        }
    }

    @Test
    public void setSqlLogEnabledAlsoAffectsDefaultRecording() {
        try {
            sqlLog.setEnabled(false);
            repo.findByLastName("A");
            assertThat(sqlLog.getAllMessages()).isEmpty();
            assertThat(sqlLog.getDefaultRecording().getMessages()).isEmpty();
        } finally {
            sqlLog.setEnabled(true);
            repo.findByLastName("A");
            assertThat(sqlLog.getAllMessages()).containsExactly(findByLastNameSql("A"));
            assertThat(sqlLog.getDefaultRecording().getMessages()).containsExactly(findByLastNameSql("A"));
        }
    }

    @Test
    public void queryListener() throws InterruptedException {
        CountDownLatch beforeQueryLatch = new CountDownLatch(1);
        CountDownLatch afterQueryLatch = new CountDownLatch(1);
        try {
            sqlLog.getQueryExecutionListeners().add(new QueryExecutionListener() {

                @Override
                public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                    beforeQueryLatch.countDown();
                }

                @Override
                public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
                    afterQueryLatch.countDown();
                }
            });
            repo.findByLastName("A");
            beforeQueryLatch.await(1, SECONDS);
            afterQueryLatch.await(1, SECONDS);
        } finally {
            sqlLog.getQueryExecutionListeners().clear();
        }
    }

    @Test
    public void defaultRecordingKeepsRecordingAfterClose() {
        sqlLog.getDefaultRecording().close();
        repo.findByLastName("A");
        assertThat(sqlLog.getDefaultRecording().getMessages()).containsExactly(findByLastNameSql("A"));
    }

    @Test
    public void regularRecordingStopsRecordingAfterClose() {
        SqlRecording r;
        try (SqlRecording recording = sqlLog.startRecording("test")) {
            r = recording;
            repo.findByLastName("A");
        }
        assertThat(r.getMessages()).containsExactly(findByLastNameSql("A"));
        repo.findByLastName("B");
        assertThat(r.getMessages()).containsExactly(findByLastNameSql("A"));
    }

    @Test
    public void getMessagesContainingRegex() {
        try (SqlRecording recording = sqlLog.startRecording("test")) {
            repo.findAll();
            Collection<String> matchingLogs = sqlLog.getMessagesContainingRegex("select.*?from person");
            assertThat(matchingLogs).containsExactly("{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_\"], \"params\":[[]]}");
        }
    }

    @Test
    public void getMessages() {
        String id = UUID.randomUUID().toString();
        String msg = "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_\"], \"params\":[[]]}";
        String[] msgs = {
                "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_\"], \"params\":[[]]}",
                "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_ where person0_.last_name=?\"], \"params\":[[\"Bauer\"]]}"};

        SqlRecording rec;
        try (SqlRecording recording = sqlLog.startRecording(id)) {
            rec = recording;
            repo.findAll();
            assertThat(recording.getMessages()).containsExactly(msg);
            repo.findByLastName("Bauer");
            assertThat(recording.getMessages()).containsExactly(msgs);
        }
        assertThat(rec.getMessages()).containsExactly(msgs);

        try (SqlRecording recording = sqlLog.startRecording(id)) {
            assertThat(recording.getMessages()).isEmpty();
            repo.findAll();
            assertThat(recording.getMessages()).containsExactly(msg);
        }
    }

    @Test
    public void clearMessages() {
        String id = UUID.randomUUID().toString();
        String msg = "{\"success\":true, \"type\":\"Prepared\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select person0_.id as id1_0_, person0_.first_name as first_na2_0_, person0_.last_name as last_nam3_0_ from person person0_\"], \"params\":[[]]}";
        try (SqlRecording recording = sqlLog.startRecording(id)) {
            assertThat(recording.getMessages()).isEmpty();
            repo.findAll();
            assertThat(recording.getAndClearMessages()).containsExactly(msg);
            assertThat(recording.getMessages()).isEmpty();
        }
    }

    @Test
    public void getMessagesContainingForJdbc() {
        try (SqlRecording recording = sqlLog.startRecording("test")) {
            jdbcTemplate.execute("create table foo (id int)");
            jdbcTemplate.execute("insert into foo (id) values (1)");
            assertThat(jdbcTemplate.queryForObject("select count (*) from foo where id = 1", Integer.class)).isEqualTo(1);
            String[] msgs = {
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"create table foo (id int)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into foo (id) values (1)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select count (*) from foo where id = 1\"], \"params\":[]}"};
            assertThat(sqlLog.getMessagesContaining("table foo")).containsExactlyInAnyOrder(msgs);
            assertThat(sqlLog.getMessagesContaining("foo")).containsExactlyInAnyOrder(msgs);
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }

    @Test
    public void recordToStreamWithQuotesAndUtf8Chars() throws IOException {
        assertThat(sqlLog.getAllMessages()).isEmpty();
        File file = File.createTempFile("test", ".json");
        file.deleteOnExit();
        try (OutputStream os = new FileOutputStream(file);
             SqlRecording recording = sqlLog.startRecording("test", file, Charset.forName("UTF-8"))) {
            jdbcTemplate.execute("create table foo (id int, name varchar)");
            jdbcTemplate.execute("insert into foo (id, name) values (1, 'Hans\tMü\"ller\nʤ')");
            assertThat(sqlLog.getAllMessages()).isEmpty();
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
        assertThat(file).hasSameContentAs(new File("src/test/resources/fileRecording.json"));
    }

    @Test
    public void recordToStreamConcurrently() throws InterruptedException {
        try {
            Map<String, File> filesById = IntStream.range(0, 2).mapToObj((i) -> UUID.randomUUID().toString())
                    .collect(Collectors.toMap(Function.identity(), SqlLogTest::createTempFile));

            try (SqlRecording recording = sqlLog.startRecording("test")) {
                assertThat(sqlLog.getAllMessages()).hasSize(0);
                jdbcTemplate.execute("create table foo (id varchar)");
                assertThat(sqlLog.getAllMessages()).hasSize(1);
            }
            sqlLog.clear();

            CountDownLatch endLatch = new CountDownLatch(filesById.size());
            filesById.entrySet().forEach(entry ->
                    new Thread(() -> {
                        try (SqlRecording recording = sqlLog.startRecording(entry.getKey(), entry.getValue(), Charset.forName("UTF-8"))) {
                            jdbcTemplate.execute(format("insert into foo (id) values ('%s')", entry.getKey()));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        endLatch.countDown();
                    }, entry.getKey()).start());
            endLatch.await(2, SECONDS);
            assertThat(sqlLog.getAllMessages()).isEmpty();

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
    public void getMessagesContainingForTransactionalJdbc() {
        try (SqlRecording recording = sqlLog.startRecording("test")) {
            jdbcTemplate.execute("create table foo (id int)");
            jdbcTemplate.execute("insert into foo (id) values (1)");
            assertThat(jdbcTemplate.queryForObject("select count (*) from foo where id = 1", Integer.class)).isEqualTo(1);

            Collection<String> matchingLogs = sqlLog.getMessagesContaining("table foo");
            String[] msgs = {
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"create table foo (id int)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"insert into foo (id) values (1)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"select count (*) from foo where id = 1\"], \"params\":[]}"};
            assertThat(matchingLogs).containsExactly(msgs);
            matchingLogs = sqlLog.getMessagesContaining("foo");
            assertThat(matchingLogs).containsExactly(msgs);
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }

    @Test
    public void getAllMessagesForConcurrentJdbc() throws InterruptedException {
        try {
            List<String> ids = new ArrayList<>();
            ids.add(UUID.randomUUID().toString());
            ids.add(UUID.randomUUID().toString());

            assertThat(sqlLog.getAllMessages()).hasSize(0);
            try (SqlRecording recording = sqlLog.startRecording("test")) {
                jdbcTemplate.execute("create table foo (id varchar)");
                assertThat(sqlLog.getAllMessages()).hasSize(1);
            }

            CountDownLatch endLatch = new CountDownLatch(ids.size());
            ids.forEach(id ->
                    new Thread(() -> {
                        sqlLog.startRecording(id);
                        jdbcTemplate.execute(format("insert into foo (id) values ('%s')", id));
                        endLatch.countDown();
                    }, id).start());
            endLatch.await(2, SECONDS);

            ids.forEach(id -> assertThat(sqlLog.getRecording(id).getMessages()).containsExactly(format(
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, " +
                            "\"batchSize\":0, \"query\":[\"insert into foo (id) values ('%s')\"], \"params\":[]}", id)));
            assertThat(sqlLog.getAllMessages()).hasSize(ids.size());
        } finally {
            jdbcTemplate.execute("drop table foo");
        }
    }

    @Test
    public void preparedStatement() {
        List<Foo> foos = Arrays.asList(new Foo(1, "a"), new Foo(2, "b"));
        try (SqlRecording recording = sqlLog.startRecording("test")) {
            jdbcTemplate.execute("create table foo (id varchar, name varchar)");
            int[] updateCounts = jdbcTemplate.batchUpdate("insert into foo (id, name) values (?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Foo foo = foos.get(i);
                    ps.setInt(1, foo.id);
                    ps.setString(2, foo.name);
                }

                @Override
                public int getBatchSize() {
                    return foos.size();
                }
            });
            assertThat(updateCounts).containsExactly(1, 1);
            assertThat(recording.getMessages()).containsExactly(
                    "{\"success\":true, \"type\":\"Statement\", \"batch\":false, \"querySize\":1, \"batchSize\":0, \"query\":[\"create table foo (id varchar, name varchar)\"], \"params\":[]}",
                    "{\"success\":true, \"type\":\"Prepared\", \"batch\":true, \"querySize\":1, \"batchSize\":2, \"query\":[\"insert into foo (id, name) values (?, ?)\"], \"params\":[[\"1\",\"a\"],[\"2\",\"b\"]]}");
        } finally {
            jdbcTemplate.execute("drop table foo");

        }
    }

    @Value
    private class Foo {
        int id;
        String name;
    }
}

