package uk.gleissner.jutil.spring;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SpringConfigTest {

    private static ClassPathXmlApplicationContext ctx;

    @BeforeClass
    public static void setup() {
        ctx = new ClassPathXmlApplicationContext("spring.xml");
    }

    @AfterClass
    public static void tearDown() {
        if (ctx != null) {
            ctx.close();
        }
    }

    @Test
    public void canScheduleFromOutsideContext() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ctx.getBean(ScheduledExecutorService.class).schedule(() -> latch.countDown(), 1, TimeUnit.MILLISECONDS);
        latch.await(10, TimeUnit.MILLISECONDS);
    }

    @Test
    public void canScheduleFromWithinContext() throws InterruptedException {
        Thread.sleep(10);
        assertThat(ctx.getBean("scheduledCounter", AtomicInteger.class).get(), is(greaterThan(1)));
    }
}