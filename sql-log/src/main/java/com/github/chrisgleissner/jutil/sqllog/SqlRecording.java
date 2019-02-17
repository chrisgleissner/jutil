package com.github.chrisgleissner.jutil.sqllog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.OutputStream;
import java.io.PrintStream;

@ToString
@Getter
@Slf4j
@RequiredArgsConstructor
class SqlRecording implements Closeable {
    @ToString.Include
    private final String id;
    boolean firstWrite = true;
    private PrintStream ps;

    public SqlRecording(String id, OutputStream os) {
        this.id = id;
        if (os != null)
            this.ps = new PrintStream(os);
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
                ps.close();
                ps = null;
                log.debug("Closed OutputStream for ID {}", id);
            } catch (Exception e) {
                throw new RuntimeException("Could not close OutputStream for ID " + id, e);
            }
        }
        log.info("Stopped recording of SQL for ID {}", id);
    }
}
