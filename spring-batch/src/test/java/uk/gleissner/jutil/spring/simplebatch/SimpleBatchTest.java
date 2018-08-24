package uk.gleissner.jutil.spring.simplebatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SimpleBatchTest {

    @Autowired
    private CacheItemWriter<Person> writer;

    @Autowired
    private JobCompletionNotificationListener completionNotificationListener;

    @Test
    public void canWrite() throws InterruptedException {
        completionNotificationListener.getCountDownLatch().await(5, SECONDS);
        assertThat(writer.getItems().size(), is(5));
        assertThat(writer.getItems().iterator().next().getFirstName(), is("JILL"));
    }
}
