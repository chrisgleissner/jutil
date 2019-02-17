package com.github.chrisgleissner.jutil.sqllog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@ToString
@Getter
@Slf4j
@RequiredArgsConstructor
public class SqlRecording implements Closeable {
    @ToString.Include
    private final String id;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<String> messages = new LinkedList<>();
    private boolean firstWrite = true;
    private PrintStream ps;

    public SqlRecording(String id, OutputStream os) {
        this.id = id;
        if (os != null)
            this.ps = new PrintStream(os);
    }

    void add(String msg) {
        if (isRecordToStreamEnabled())
            write(msg);
        else {
            lock.writeLock().lock();
            try {
                messages.add(msg);
            } finally {
                lock.writeLock().unlock();
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

    boolean isRecordToStreamEnabled() {
        return ps != null;
    }

    void write(String msg) {
        if (firstWrite) {
            ps.print("[");
            firstWrite = false;
        } else
            ps.println(",");
        ps.print(msg);
    }

    @Override
    public void close() {
        if (ps != null) {
            if (!firstWrite)
                ps.println("]");
            try {
                ps.flush();
                ps = null;
                log.debug("Closed OutputStream for ID {}", id);
            } catch (Exception e) {
                throw new RuntimeException("Could not close OutputStream for ID " + id, e);
            }
        }
        log.info("Stopped recording of SQL for ID {}", id);
    }
}
