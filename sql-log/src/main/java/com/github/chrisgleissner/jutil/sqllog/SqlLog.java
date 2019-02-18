package com.github.chrisgleissner.jutil.sqllog;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.NoOpQueryExecutionListener;
import net.ttddyy.dsproxy.listener.logging.DefaultJsonQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.QueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import net.ttddyy.dsproxy.transform.QueryTransformer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * Records SQL messages either on heap or by writing them to an OutputStream.
 * Use {@link #startRecording(String)} (on heap) or {@link #startRecording(String, File, Charset)} (in file) to start
 * recording. This returns a {@link SqlRecording}. To stop recording, call {@link SqlRecording#close()}.
 */
@Slf4j
public class SqlLog extends NoOpQueryExecutionListener implements BeanPostProcessor {
    public static final String DEFAULT_ID = "default";
    public static final QueryTransformer identityQueryTransformer = transformInfo -> transformInfo.getQuery();
    public static final QueryTransformer noOpQueryTransformer = transformInfo -> "select 1";

    private final SqlRecording defaultRecording = new SqlRecording(this, DEFAULT_ID, null, null);

    private final QueryLogEntryCreator logEntryCreator = new DefaultJsonQueryLogEntryCreator() {
        protected void writeTimeEntry(StringBuilder sb, ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        }
    };

    private final InheritableThreadLocal<SqlRecording> currentRecording = new InheritableThreadLocal<SqlRecording>() {
        @Override protected SqlRecording initialValue() {
            return defaultRecording;
        }
    };

    private final ConcurrentHashMap<String, SqlRecording> recordingsById = new ConcurrentHashMap<>();

    @Getter
    private final boolean logQueries;

    @Getter
    private final boolean traceMethods;

    @Getter
    private boolean enabled;

    @Getter @Setter
    private QueryTransformer queryTransformer = identityQueryTransformer;

    SqlLog(boolean enabled, boolean propagateCallsToDb, boolean logQueries, boolean traceMethods) {
        this.enabled = enabled;
        this.logQueries = logQueries;
        this.traceMethods = traceMethods;
        recordingsById.put(DEFAULT_ID, defaultRecording);
        setPropagateCallsToDbEnabled(propagateCallsToDb);
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
     * Starts a file recording session for the specified ID. If a session with this ID is currently in progress,
     * it is stopped first.
     *
     * @param id under which the recordings will be tracked
     * @return recorded heap SQL logs for previous recording of the specified ID, empty if no such recording exists
     */
    public SqlRecording startRecording(String id, File file, Charset charset) {
        return recordingsById.computeIfAbsent(id, (i) -> {
            SqlRecording recording = new SqlRecording(this, id, file, charset);
            currentRecording.set(recording);
            log.info("Started {}", recording);
            return recording;
        });
    }

    /**
     * Stops the recording with the specified ID and returns it, if existent. Alternatively, call {@link SqlRecording#close()}.
     */
    public SqlRecording stopRecording(String id) {
        if (DEFAULT_ID.equals(id))
            return defaultRecording;

        SqlRecording recording = recordingsById.remove(id);
        if (recording == null)
            throw new RuntimeException(String.format("Can't stop recording with ID %s since it doesn't exist", id));
        recording.stopRecording();
        currentRecording.set(defaultRecording);
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
     * Stops all ongoing recordings (except the default recording) and clears their recorded messages.
     */
    public void clear() {
        recordingsById.values().forEach((r) -> {
            r.clear();
            r.close();
        });
        recordingsById.keySet().stream().filter(k -> !k.equals(DEFAULT_ID)).forEach(k -> recordingsById.remove(k));
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        if (enabled && bean instanceof DataSource) {
            ProxyDataSourceBuilder builder = ProxyDataSourceBuilder.create((DataSource) bean);
            if (this.traceMethods)
                builder.traceMethods();
            if (this.logQueries)
                builder.logQueryBySlf4j(SLF4JLogLevel.DEBUG);
            builder.queryTransformer(transformInfo -> SqlLog.this.queryTransformer.transformQuery(transformInfo));
            ProxyDataSource proxy = builder.listener(this).build();
            log.info("Created proxy for DataSource bean with name {} and type {}: {}", beanName, bean.getClass().getName(), proxy);
            return proxy;
        }
        return bean;
    }

    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
        if (enabled) {
            String msg = logEntryCreator.getLogEntry(executionInfo, list, false, false);
            Optional.ofNullable(currentRecording.get()).ifPresent(r -> {
                r.add(msg);
                log.debug("{}: {}", r.getId(), msg);
            });
        }
    }

    @Override
    public String toString() {
        return "SqlLog{" +
                "currentRecording=" + currentRecording.get() +
                ", recordingsById=" + recordingsById +
                ", logQueries=" + logQueries +
                ", traceMethods=" + traceMethods +
                ", enabled=" + enabled +
                '}';
    }

    public void setEnabled(boolean sqlLogEnabled) {
        this.enabled = sqlLogEnabled;
        log.info("SQL log enabled: {}", sqlLogEnabled);
    }

    public void setPropagateCallsToDbEnabled(boolean propagateCallsToDbEnabled) {
        queryTransformer = propagateCallsToDbEnabled ? identityQueryTransformer : noOpQueryTransformer;
        log.info("Propagate calls to DB enabled: {}", propagateCallsToDbEnabled);
    }

    public SqlRecording getDefaultRecording() {
        return defaultRecording;
    }
}
