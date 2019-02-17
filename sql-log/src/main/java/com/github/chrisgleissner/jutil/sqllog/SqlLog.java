package com.github.chrisgleissner.jutil.sqllog;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.NoOpQueryExecutionListener;
import net.ttddyy.dsproxy.listener.logging.DefaultJsonQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.QueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.proxy.DefaultConnectionIdManager;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Records SQL executions either on heap or by writing them to an OutputStream.
 *
 * <p>To start recording SQL executions, use {@link #startRecording(String)} (on heap)
 * or {@link #startRecording(String, OutputStream)} (to stream). Stop recording (and close the stream if recording to stream)
 * via {@link #stopRecording(String)}.</p>
 */
@Getter
@ToString
@Slf4j
public class SqlLog extends NoOpQueryExecutionListener implements BeanPostProcessor {
    private final QueryLogEntryCreator logEntryCreator = new DefaultJsonQueryLogEntryCreator() {
        protected void writeTimeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        }
    };
    private final ConcurrentHashMap<String, SqlRecording> recordingsById = new ConcurrentHashMap<>();
    private final static InheritableThreadLocal<SqlRecording> recording = new InheritableThreadLocal<>();
    private final boolean traceMethods;
    private boolean logQueries;

    SqlLog(boolean logQueries, boolean traceMethods) {
        this.logQueries = logQueries;
        this.traceMethods = traceMethods;
        log.debug("Created SqlLog: logQueries={}, traceMethods={}", logQueries, traceMethods);
    }

    /**
     * Starts a heap recording session for the specified ID. If a session with this ID is currently in progress,
     * it is stopped first.
     *
     * @param id under which the recordings will be tracked
     * @return recorded heap SQL logs for previous recording of the specified ID, empty if no such recording exists
     */
    public Collection<String> startRecording(String id) {
        return setRecording(new SqlRecording(id, null));
    }

    /**
     * Starts a stream recording session for the specified ID. If a session with this ID is currently in progress,
     *      * it is stopped first.
     *
     * @param id under which the recordings will be tracked
     * @return recorded heap SQL logs for previous recording of the specified ID, empty if no such recording exists
     */
    public Collection<String> startRecording(String id, OutputStream os) {
        return setRecording(new SqlRecording(id, os));
    }

    private Collection<String> setRecording(SqlRecording rec) {
        Collection<String> messages = getMessagesForCurrentRecording();
        recording.set(rec);
        if (rec == null) {
            log.info("Stopped SQL recording");
        } else {
            recordingsById.put(rec.getId(), rec);
            log.info("Started {} recording of SQL for ID {}", rec.isRecordToStreamEnabled() ? "stream" : "heap", rec.getId());
        }
        return messages;
    }

    private Collection<String> getMessagesForCurrentRecording() {
        SqlRecording oldRecording = recording.get();
        Collection<String> messages = emptyList();
        if (oldRecording != null) {
            messages = Optional.ofNullable(recordingsById.remove(oldRecording.getId())).map(r -> r.getAll()).orElse(emptyList());
            oldRecording.close();
        }
        return messages;
    }

    /**
     * Stops a currently active recording session with the specified ID.
     *
     * @param id of a currently active recording
     * @return recorded heap SQL logs for previous recording of the specified ID, empty if no such recording exists
     */
    public Collection<String> stopRecording(String id) {
        Collection<String> messages = setRecording(null);
        log.info("Stopped recording of SQL for ID {}. Found {} message(s) on heap.", id, messages.size());
        return messages;
    }

    /**
     * Returns the recorded heap SQL logs of the specified recording ID.
     */
    public Collection<String> getLogsById(String id) {
        return Optional.ofNullable(recordingsById.get(id)).map(SqlRecording::getAll).orElse(emptyList());
    }

    /**
     * Returns all recorded heap SQL logs that match the specified regular expression, regardless of recording ID.
     */
    public Collection<String> getLogsContainingRegex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return getLogs(v -> v.getAll().stream().anyMatch(s -> pattern.matcher(s).find()));
    }

    /**
     * Returns all recorded heap SQL logs that contain an exact case-sensitive match of the specified string, regardless of recording ID.
     */
    public Collection<String> getLogsContaining(String expectedString) {
        return getLogs(v -> v.getAll().stream().anyMatch(s -> s.contains(expectedString)));
    }

    private Collection<String> getLogs(Predicate<SqlRecording> predicate) {
        return recordingsById.values().stream()
                .filter(predicate)
                .flatMap(l -> l.getAll().stream()).collect(toList());
    }

    /**
     * Returns all heap SQL logs, across all recording IDs.
     */
    public Collection<String> getLogs() {
        return recordingsById.values().stream().flatMap(l -> l.getAll().stream()).collect(toList());
    }

    /**
     * Clears all heap SQL logs across all recording sessions.
     */
    public void clearAll() {
        recordingsById.clear();
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            ProxyDataSourceBuilder builder = ProxyDataSourceBuilder.create((DataSource) bean)
                    .connectionIdManager(new DefaultConnectionIdManager());
            if (this.traceMethods)
                    builder.traceMethods();
            if (this.logQueries)
                builder.logQueryBySlf4j(SLF4JLogLevel.DEBUG);
            return builder.listener(this).build();
        }
        return bean;
    }

    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
        String msg = logEntryCreator.getLogEntry(executionInfo, list, false, false);
        SqlRecording rec = recording.get();
        if (rec != null) {
            rec.add(msg);
            log.debug("{}: {}", rec.getId(), msg);
        } else
            log.debug("{}", msg);
    }
}
