package uk.gleissner.jutil.spring.batch.rest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gleissner.jutil.spring.batch.adhoc.AdHocScheduler;
import uk.gleissner.jutil.spring.batch.adhoc.AdHocSchedulerConfig;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ServerTest {

    @TestConfiguration
    @Import(AdHocSchedulerConfig.class)
    public static class MyConfig {
    }

    private static final Logger logger = getLogger(ServerTest.class);
    private static final String JOB_NAME = "testJob";
    private static final int MAX_ITEMS = 100;

    private AtomicInteger readerSource = new AtomicInteger();
    private Set<String> writerTarget = new ConcurrentSkipListSet<>();
    private Semaphore allItemsWrittenSemaphore = new Semaphore(-MAX_ITEMS + 1);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AdHocScheduler scheduler;

    @Before
    public void setUp() throws DuplicateJobException {
        scheduler.schedule(JOB_NAME, () -> scheduler.jobs().get(JOB_NAME)
                .incrementer(new RunIdIncrementer()) // adds unique parameter on each run so that job can be rerun
                .flow(scheduler.steps().get("step")
                        .<Integer, String>chunk(30)
                        .reader(() -> {
                            int i = readerSource.incrementAndGet();
                            if (i % 10 == 0)
                                logger.info("Read {} item(s) so far", i);
                            return i <= MAX_ITEMS ? i : null;
                        })
                        .processor((ItemProcessor<Integer, String>) (i1) -> i1.toString())
                        .writer(items -> {
                            writerTarget.addAll(items);
                            logger.info("Wrote {} item(s) so far", writerTarget.size());
                            allItemsWrittenSemaphore.release(items.size());
                        })
                        .allowStartIfComplete(true)
                        .build()).end().build(), "0/1 * * * * ?");
        scheduler.start();
    }

    @Test
    public void jobExecutions() throws InterruptedException {
        allItemsWrittenSemaphore.tryAcquire(1, 3, SECONDS);
        assertThat(writerTarget).hasSize(MAX_ITEMS);
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/jobExecutions?exitStatus=COMPLETED", String.class))
                .contains("\"status\":\"COMPLETED\"")
                .contains("\"id\":0,\"jobId\":0");
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/jobExecutions?exitStatus=FAILED*", String.class)).matches("\\[\\]");
    }
}