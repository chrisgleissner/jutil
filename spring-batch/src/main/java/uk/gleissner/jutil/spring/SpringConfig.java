package uk.gleissner.jutil.spring;

import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.stream.IntStream.rangeClosed;
import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class SpringConfig {

    private static final Logger logger = getLogger(SpringConfig.class);
    private ScheduledExecutorService es;

    @Bean
    public ScheduledExecutorService scheduler() {
        es = newSingleThreadScheduledExecutor(r -> new Thread(r, "scheduler"));
        rangeClosed(1, 1000).forEach(i -> {
            logger.info("Iteration {}: {}", i, rangeClosed(1, 100000).map(j -> j + 1).filter(j -> j > 1000 && i < 1000000).sum());
        });
        return es;
    }

    @Bean
    public AtomicInteger scheduledCounter() {
        AtomicInteger i = new AtomicInteger(0);
        scheduler().scheduleAtFixedRate((() -> i.incrementAndGet()), 10, 10, MICROSECONDS);
        return i;
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        if (es != null) {
            es.shutdownNow();
            es.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}
