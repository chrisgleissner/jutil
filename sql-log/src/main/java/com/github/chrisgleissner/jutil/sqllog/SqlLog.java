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
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * Records SQL messages either on heap or by writing them to an OutputStream.
 * Use {@link #startRecording(String)} (on heap) or {@link #startRecording(String, OutputStream, Charset)} (to stream) to start
 * recording which returns a {@link SqlRecording}. To stop recording, call {@link SqlRecording#close()}.</p>
 */
@Slf4j
@ToString
public class SqlLog extends NoOpQueryExecutionListener implements BeanPostProcessor {
    private final static InheritableThreadLocal<SqlRecording> currentRecording = new InheritableThreadLocal<>();

    @ToString.Exclude
    private final QueryLogEntryCreator logEntryCreator = new DefaultJsonQueryLogEntryCreator() {
        protected void writeTimeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        }
    };

    @ToString.Exclude
    private final ConcurrentHashMap<String, SqlRecording> recordingsById = new ConcurrentHashMap<>();

    @Getter
    private final boolean logQueries;

    @Getter
    private final boolean traceMethods;

    @Getter
    private boolean sqlLogEnabled;

    SqlLog(boolean sqlLogEnabled, boolean logQueries, boolean traceMethods) {
        this.sqlLogEnabled = sqlLogEnabled;
        this.logQueries = logQueries;
        this.traceMethods = traceMethods;
    }

    public void setSqlLogEnabled(boolean sqlLogEnabled) {
        this.sqlLogEnabled = sqlLogEnabled;
        log.info("SQL log enabled: {}", sqlLogEnabled);
    }

    /**
     * Starts a heap recording session for the specified ID. If a session with this ID is currently in progress,
     * it is stopped first.
     *
     * @param id under which the recordings will be tracked
     * @return recorded heap SQL logs for previous recording of the specified ID, empty if no such recording exists
     */
    public SqlRecording startRecording(String id) {
        return startRecording(id, null, null);
    }

    /**
     * Starts a stream recording session for the specified ID. If a session with this ID is currently in progress,
     * it is stopped first.
     *
     * @param id under which the recordings will be tracked
     * @return recorded heap SQL logs for previous recording of the specified ID, empty if no such recording exists
     */
    public SqlRecording startRecording(String id, OutputStream os, Charset charset) {
        if (currentRecording.get() != null)
            throw new RuntimeException(String.format("Can't start recording with ID %s since you first need to " +
                    "stop the current recording: %s", id, currentRecording.get()));
        SqlRecording recording = new SqlRecording(this, id, os, charset);
        currentRecording.set(recording);
        recordingsById.put(recording.getId(), recording);
        log.info("Started {}", recording);
        return recording;
    }

    /**
     * Stops the recording with the specified ID and returns it, if existent. Alternatively, call {@link SqlRecording#close()}.
     */
    public SqlRecording stopRecording(String id) {
        SqlRecording recording = recordingsById.remove(id);
        if (recording == null)
            throw new RuntimeException(String.format("Can't stop recording with ID %s since it doesn't exist", id));
        recording.stopRecording();
        currentRecording.set(null);
        log.info("Stopped {}", recording);
        return recording;
    }

    /**
     * Returns the recording with the specified ID, unless it has been stopped.
     *
     * @param id of recording
     * @return recording or null if the recording is not known or not in progress
     */
    public SqlRecording getRecording(String id) {
        return recordingsById.get(id);
    }

    /**
     * Returns all recorded heap SQL logs that match the specified regular expression, regardless of recording ID.
     */
    public Collection<String> getMessagesContainingRegex(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return recordingsById.values().stream()
                .filter(v -> v.getMessages().stream().anyMatch(s -> pattern.matcher(s).find()))
                .flatMap(l -> l.getMessages().stream()).collect(toList());
    }

    /**
     * Returns all recorded heap SQL logs that contain an exact case-sensitive match of the specified string, regardless of recording ID.
     */
    public Collection<String> getMessagesContaining(String expectedString) {
        return recordingsById.values().stream()
                .filter(v -> v.getMessages().stream().anyMatch(s -> s.contains(expectedString)))
                .flatMap(l -> l.getMessages().stream()).collect(toList());
    }

    /**
     * Returns all heap SQL logs, across all recording IDs.
     */
    public Collection<String> getAllMessages() {
        return recordingsById.values().stream().flatMap(l -> l.getMessages().stream()).collect(toList());
    }

    /**
     * Stops all ongoing recordings and clears their recorded messages.
     */
    public void clear() {
        recordingsById.values().forEach(SqlRecording::close);
        recordingsById.clear();
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (sqlLogEnabled && bean instanceof DataSource) {
            ProxyDataSourceBuilder builder = ProxyDataSourceBuilder.create((DataSource) bean)
                    .connectionIdManager(new DefaultConnectionIdManager());
            if (this.traceMethods)
                builder.traceMethods();
            if (this.logQueries)
                builder.logQueryBySlf4j(SLF4JLogLevel.DEBUG);
            ProxyDataSource proxy = builder.listener(this).build();
            log.info("Created proxy for DataSource bean with name {} and type {}: {}", beanName, bean.getClass().getName(), proxy);
            return proxy;
        }
        return bean;
    }

    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
        if (sqlLogEnabled) {
            String msg = logEntryCreator.getLogEntry(executionInfo, list, false, false);
            Optional.ofNullable(currentRecording.get()).ifPresent(r -> {
                r.add(msg);
                log.debug("{}: {}", r.getId(), msg);
            });
        }
    }
}
