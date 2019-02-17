package com.github.chrisgleissner.jutil.sqllog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.NoOpQueryExecutionListener;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.joining;

@Slf4j @RequiredArgsConstructor
class SqlExecutionListener extends NoOpQueryExecutionListener {
    private final ConcurrentHashMap<String, SqlExecutions> logsById = new ConcurrentHashMap<>();
    private final SqlLog sqlLog;

    ConcurrentHashMap<String, SqlExecutions> getLogsById() {
        return logsById;
    }

    @Override
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> list) {
        String msg = sqlLog.getLogEntryCreator().getLogEntry(executionInfo, list, false, false);
        SqlRecording recording = sqlLog.getRecording();
        if (recording != null) {
            logsById.computeIfAbsent(recording.getId(), id -> new SqlExecutions(recording)).add(msg);
            log.debug("{}: {}", recording.getId(), msg);
        } else
            log.debug("{}", msg);
    }

    public String toString() {
        return String.format("SqlExecutionListener(%s)", logsById.entrySet().stream().map(e ->
                String.format("%s=%s", e.getKey(), e.getValue())).collect(joining("\n")));
    }
}
