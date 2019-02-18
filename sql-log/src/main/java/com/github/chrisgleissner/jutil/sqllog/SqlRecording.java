package com.github.chrisgleissner.jutil.sqllog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Recording of SQL messages.
 */
@Slf4j
@RequiredArgsConstructor
public class SqlRecording implements Closeable {
    private final SqlLog sqlLog;
    @Getter private final String id;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<String> messages = new LinkedList<>();
    private final AtomicInteger messageCount = new AtomicInteger();
    private final long threadId;
    private final String threadName;
    private final File file;

    private boolean firstWrite = true;
    private OutputStreamWriter osw;

    SqlRecording(SqlLog sqlLog, String id, File file, Charset charset) {
        this.sqlLog = sqlLog;
        this.id = id;
        this.threadId = Thread.currentThread().getId();
        this.threadName = Thread.currentThread().getName();
        this.file = file;
        if (file != null) {
            try {
                this.osw = new OutputStreamWriter(new FileOutputStream(file, true), charset);
            } catch (Exception e) {
                throw new RuntimeException("Could not open output stream for " + file.getAbsolutePath(), e);
            }
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            messages.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Collection<String> getAndClearMessages() {
        return writeLocked(() -> {
            ArrayList<String> msgs = new ArrayList<>(messages);
            messages.clear();
            return msgs;
        });
    }

    public Collection<String> getMessages() {
        return readLocked(() -> new ArrayList<>(messages));
    }

    public int size() {
        return messageCount.get();
    }

    /**
     * Closes (and thus stops) this recording. If writing to an OutputStream, this flushes any pending messages, but
     * closing the OutputStream remains responsibility of the caller.
     */
    @Override
    public void close() {
        sqlLog.stopRecording(id);
    }

    void add(String msg) {
        messageCount.incrementAndGet();
        if (osw != null)
            write(msg);
        else {
            writeLocked(() -> messages.add(msg));
        }
    }

    void write(String msg) {
        if (firstWrite) {
            writeToStream("[");
            firstWrite = false;
        } else {
            writeToStream(",");
            writeToStream("\n");
        }
        writeToStream(msg);
    }

    void stopRecording() {
        if (osw != null) {
            if (!firstWrite) {
                writeToStream("]");
                writeToStream("\n");
            }
            try {
                osw.flush();
                osw.close();
                osw = null;
                log.debug("Closed OutputStream for ID {}", id);
            } catch (Exception e) {
                throw new RuntimeException("Could not close OutputStream for ID " + id, e);
            }
        }
    }

    private <T> T readLocked(Supplier<T> t) {
        lock.readLock().lock();
        try {
            return t.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    private <T> T writeLocked(Supplier<T> t) {
        lock.writeLock().lock();
        try {
            return t.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void writeToStream(String s) {
        try {
            osw.write(s);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to stream: " + s, e);
        }
    }

    @Override
    public String toString() {
        return "SqlRecording{" +
                "id='" + id + '\'' +
                ", threadId=" + threadId +
                ", threadName=" + threadName +
                ", location=" + (file == null ? "heap" : file.getAbsolutePath()) +
                ", messageCount=" + messageCount.get() +
                '}';
    }
}
