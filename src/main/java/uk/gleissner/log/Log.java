package uk.gleissner.log;

import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

public class Log {

    static final Random RANDOM = new Random();
    static final int MAX_THREAD_BACKOFF_IN_MS = 10;

    static final Logger jul = Logger.getLogger(Log.class.getName());
    static final org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(Log.class);
    static final org.apache.commons.logging.Log jcl = LogFactory.getLog(Log.class);
    static final org.slf4j.Logger slf4j = LoggerFactory.getLogger(Log.class);

    public Log(int numberOfThreads, int numberOflogs) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Exception e;
        try {
            throw new RuntimeException("test");
        } catch (Exception e2) {
            e = e2;
        }

        try {
            CountDownLatch latch = new CountDownLatch(numberOfThreads);
            ExecutorService es = Executors.newCachedThreadPool();
            for (int t = 0; t < numberOfThreads; t++) {
                es.submit(new LogRunnable(t, numberOflogs, e, latch));
                Thread.sleep(RANDOM.nextInt(MAX_THREAD_BACKOFF_IN_MS));
            }
            latch.await();
        } catch (Exception e3) {
            throw new RuntimeException("Logging failed", e3);
        }
    }


    class LogRunnable implements Runnable {

        private final int t;
        private final Throwable e;
        private final CountDownLatch latch;
        private final int numberOflogs;

        public LogRunnable(int threadId, int numberOflogs, Throwable e, CountDownLatch latch) {
            this.t = threadId;
            this.numberOflogs = numberOflogs;
            this.e = e;
            this.latch = latch;
        }


        @Override
        public void run() {
            for (int i = 0; i < numberOflogs; i++) {
                jul.info(format("jul-info-%s-%s", t, i));
                jul.log(Level.WARNING, format("jul-warn-", t, i), e);
                jul.severe(format("jul-severe-" + i));

                log4j.info(format("log4j-info-", t, i));
                log4j.warn(format("log4j-warn-", t, i), e);
                log4j.error(format("log4j-error-", t, i));

                jcl.info(format("jcl-info-", t, i));
                jcl.warn(format("jc-warn-", t, i));
                jcl.error(format("jcl-error-", t, i), e);

                slf4j.info(format("slf4j-info-", t, i));
                slf4j.warn(format("sfl4j-warn-", t, i), e);
                slf4j.error(format("slf4j-error-", t, i));

                if (i % 100 == 0) {
                    System.out.println(format("Thread %s, Iteration %s", t, i));
                }

                try {
                    Thread.sleep(RANDOM.nextInt(MAX_THREAD_BACKOFF_IN_MS));
                } catch (Exception e) {
                    slf4j.error("Thread interrupted", e);
                }
            }
            latch.countDown();
        }
    }

    public static void main(String[] args) {
        new Log(10, 1_000_000);
    }
}
