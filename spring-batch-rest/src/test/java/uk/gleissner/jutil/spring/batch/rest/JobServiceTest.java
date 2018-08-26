package uk.gleissner.jutil.spring.batch.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gleissner.jutil.spring.batch.rest.MockSetup.configureMock;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private JobExplorer jobExplorer;

    private JobService jobService;

    @Before
    public void setUp() throws NoSuchJobException {
        configureMock(jobExplorer);
        jobService = new JobService(jobExplorer);
    }

    @Test
    public void jobExecutionsAll() {
        List<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(empty(), empty(), empty(), empty());
        assertThat(jes).hasSize(6);
    }

    @Test
    public void jobExecutionsJobNameRegexp() {
        List<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(Optional.of("j1"), empty(), empty(), empty());
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsStatus() {
        List<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(Optional.of("j1"), Optional.of(ExitStatus.COMPLETED), empty(), empty());
        assertThat(jes).hasSize(2);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstances() {
        List<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(empty(), Optional.of(ExitStatus.FAILED), Optional.of(1), empty());
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobExecutionsPerInstance() {
        List<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(empty(), Optional.of(ExitStatus.COMPLETED), empty(), Optional.of(1));
        assertThat(jes).hasSize(3);
    }
}