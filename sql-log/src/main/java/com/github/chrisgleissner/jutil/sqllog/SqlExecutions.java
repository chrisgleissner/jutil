package com.github.chrisgleissner.jutil.sqllog;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.stream.Collectors.joining;

@Slf4j
class SqlExecutions {
    private final SqlRecording sqlRecording;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<String> messages = new LinkedList<>();

    SqlExecutions(SqlRecording sqlRecording) {
        this.sqlRecording = sqlRecording;
    }

    void add(String msg) {
        if (sqlRecording != null) {
            if (sqlRecording.isRecordToStreamEnabled())
                sqlRecording.write(msg);
            else {
                lock.writeLock().lock();
                try {
                    messages.add(msg);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        }
    }

    Collection<String> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(messages);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String toString() {
        Collection<String> msgs = getAll();
        return String.format("SqlExecutions(%s, count=%s):%s", sqlRecording, msgs.size(),
                msgs.isEmpty() ? " empty" : "\n" + msgs.stream().collect(joining("\n")));
    }

}
