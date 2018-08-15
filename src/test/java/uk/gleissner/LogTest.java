package uk.gleissner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

public class LogTest {

    static final int NUM_LOGS = 1_000_000;
    static final int NUM_THREADS = 10;
    static final Random RANDOM = new Random();
    static final File LOG_DIR = new File("target/log");
    private static final int MAX_THREAD_BACKOFF_IN_MS = 10;

    static {
        try {
            FileUtils.cleanDirectory(LOG_DIR);
            assertTrue(FileUtils.listFiles(LOG_DIR, new String[]{".log"}, true).isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final Logger jul = Logger.getLogger(LogTest.class.getName());
    static final org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(LogTest.class);
    static final Log jcl = LogFactory.getLog(LogTest.class);
    static final org.slf4j.Logger slf4j = LoggerFactory.getLogger(LogTest.class);

    @Test
    public void canLog() throws InterruptedException, IOException {

        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Exception e;
        try {
            throw new RuntimeException("test");
        } catch (Exception e2) {
            e = e2;
        }

        CountDownLatch latch = new CountDownLatch(NUM_THREADS);
        ExecutorService es = Executors.newCachedThreadPool();
        for (int t = 0; t < NUM_THREADS; t++) {
            es.submit(new LogRunnable(t, e, latch));
            Thread.sleep(RANDOM.nextInt(MAX_THREAD_BACKOFF_IN_MS));
        }
        latch.await();
    }


    class LogRunnable implements Runnable {

        private final int t;
        private final Throwable e;
        private final CountDownLatch latch;

        public LogRunnable(int threadId, Throwable e, CountDownLatch latch) {
            this.t = threadId;
            this.e = e;
            this.latch = latch;
        }


        @Override
        public void run() {
            for (int i = 0; i < LogTest.NUM_LOGS; i++) {
                LogTest.jul.info(format("jul-info-%s-%s", t, i));
                LogTest.jul.log(Level.WARNING, format("jul-warn-", t, i), e);
                LogTest.jul.severe(format("jul-severe-" + i));

                LogTest.log4j.info(format("log4j-info-", t, i));
                LogTest.log4j.warn(format("log4j-warn-", t, i), e);
                LogTest.log4j.error(format("log4j-error-", t, i));

                LogTest.jcl.info(format("jcl-info-", t, i));
                LogTest.jcl.warn(format("jc-warn-", t, i));
                LogTest.jcl.error(format("jcl-error-", t, i), e);

                LogTest.slf4j.info(format("slf4j-info-", t, i));
                LogTest.slf4j.warn(format("sfl4j-warn-", t, i), e);
                LogTest.slf4j.error(format("slf4j-error-", t, i));

                if (i % 100 == 0) {
                    System.out.println(format("Thread %s, Iteration %s", t, i));
                }

                try {
                    Thread.sleep(RANDOM.nextInt(MAX_THREAD_BACKOFF_IN_MS));
                } catch (Exception e) {
                    LogTest.slf4j.error("Thread interrupted", e);
                }
            }
            latch.countDown();
        }
    }
}