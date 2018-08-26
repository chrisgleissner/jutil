package uk.gleissner.jutil.spring.batch.springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gleissner.jutil.spring.batch.CacheItemWriter;
import uk.gleissner.jutil.spring.batch.JobCompletionNotificationListener;
import uk.gleissner.jutil.spring.batch.Person;

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
    public void quartzSchedulerWorks() throws InterruptedException {
        completionNotificationListener.awaitCompletionOfJobs(2, 5_000);
        assertThat(writer.getItems().size(), is(10));
        assertThat(writer.getItems().iterator().next().getFirstName(), is("JILL"));
    }
}
