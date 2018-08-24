/*
 * Copyright (C) 2018 Christian Gleissner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gleissner.jutil.log;

import com.google.common.math.StatsAccumulator;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

/**
 * Concurrently logs via various frameworks which are all configured to propagate via SLF4J to Logback.
 * Useful to verify whether logs appear correctly in the target location.
 */
public class ConcurrentLogger {

    static final int MAX_THREAD_BACKOFF_IN_MS = 10;
    static final String RANDOM_LOG_MESSAGE = RandomStringUtils.randomAlphabetic(100);

    static final Random random = new Random();

    static final Logger jul = Logger.getLogger(ConcurrentLogger.class.getName());
    static final StatsAccumulator julStats = new StatsAccumulator();

    static final org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(ConcurrentLogger.class);
    static final StatsAccumulator log4jStats = new StatsAccumulator();

    static final org.apache.commons.logging.Log jcl = LogFactory.getLog(ConcurrentLogger.class);
    static final StatsAccumulator jclStats = new StatsAccumulator();

    static final org.slf4j.Logger slf4j = LoggerFactory.getLogger(ConcurrentLogger.class);
    static final StatsAccumulator slf4jStats = new StatsAccumulator();

    static ReentrantReadWriteLock statsLock = new ReentrantReadWriteLock(true);


    public ConcurrentLogger(int numberOfThreads, int numberOflogs) {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        Exception e;
        try {
            throw new RuntimeException("test");
        } catch (Exception e2) {
            e = e2;
        }

        ScheduledExecutorService scheduledExecutor = newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                statsLock.readLock().lock();
                System.out.println(format("Mean/Max/StdDev:\t\tJUL:%d/%d/%d\tJCL:%d/%d/%d\tLog4J:%d/%d/%d\tSLF4J:%d/%d/%d",
                        (int) julStats.mean(), (int) julStats.max(), (int) julStats.sampleStandardDeviation(),
                        (int) jclStats.mean(), (int) jclStats.max(), (int) jclStats.sampleStandardDeviation(),
                        (int) log4jStats.mean(), (int) log4jStats.max(), (int) log4jStats.sampleStandardDeviation(),
                        (int) slf4jStats.mean(), (int) slf4jStats.max(), (int) slf4jStats.sampleStandardDeviation()));
            } catch (Exception e4) {
                e4.printStackTrace();
            } finally {
                statsLock.readLock().unlock();
            }
        }, 3, 3, TimeUnit.SECONDS);

        try {
            CountDownLatch latch = new CountDownLatch(numberOfThreads);
            ExecutorService es = newCachedThreadPool();
            for (int t = 0; t < numberOfThreads; t++) {
                es.submit(new LogRunnable(t, numberOflogs, e, latch));
                Thread.sleep(random.nextInt(MAX_THREAD_BACKOFF_IN_MS));
            }
            latch.await();
            scheduledExecutor.shutdown();
            slf4j.info("Log completed");
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

                long startTime = nanoTime();
                jul.info(format("jul-info-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE));
                jul.log(Level.WARNING, format("jul-warn-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE), e);
                jul.severe(format("jul-severe-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE));
                long julMics = (nanoTime() - startTime) / 1000;

                startTime = nanoTime();
                log4j.info(format("log4j-info-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE));
                log4j.warn(format("log4j-warn-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE), e);
                log4j.error(format("log4j-error-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE));
                long log4jMics = (nanoTime() - startTime) / 1000;

                startTime = nanoTime();
                jcl.info(format("jcl-info-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE));
                jcl.warn(format("jc-warn-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE));
                jcl.error(format("jcl-error-%s-%s-%s", t, i, RANDOM_LOG_MESSAGE), e);
                long jclMics = (nanoTime() - startTime) / 1000;

                startTime = nanoTime();
                slf4j.info("slf4j-info-{}-{}-{}", t, i, RANDOM_LOG_MESSAGE);
                slf4j.warn("sfl4j-warn-{}-{}-{}", t, i, RANDOM_LOG_MESSAGE, e);
                slf4j.error("slf4j-error-{}-{}-{}", t, i, RANDOM_LOG_MESSAGE);
                long slf4jMics = (nanoTime() - startTime) / 1000;

                try {
                    statsLock.writeLock().lock();
                    julStats.add(julMics);
                    log4jStats.add(log4jMics);
                    jclStats.add(jclMics);
                    slf4jStats.add(slf4jMics);
                } finally {
                    statsLock.writeLock().unlock();
                }

                try {
                    Thread.sleep(random.nextInt(MAX_THREAD_BACKOFF_IN_MS));
                } catch (Exception e) {
                    slf4j.error("Thread interrupted", e);
                }
            }
            latch.countDown();
        }
    }

    public static void main(String[] args) {
        new ConcurrentLogger(10, 1_000_000);
    }
}
