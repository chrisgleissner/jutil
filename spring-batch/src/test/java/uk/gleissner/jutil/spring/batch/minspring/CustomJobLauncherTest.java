package uk.gleissner.jutil.spring.batch.minspring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CustomJobLauncherConfig.class)
public class CustomJobLauncherTest {

    @Autowired
    private CustomJobLauncher customJobLauncher;

    @Test
    public void canWrite() throws InterruptedException {
        customJobLauncher.launch("sample-data.csv");
        customJobLauncher.launch("sample-data2.csv");

        customJobLauncher.getListener().awaitCompletionOfJobs(2, 5_000);

        assertThat(customJobLauncher.getWriter().getItems().size(), is(12));
        assertThat(customJobLauncher.getWriter().getItems().iterator().next().getFirstName(), is("JILL"));
    }
}
