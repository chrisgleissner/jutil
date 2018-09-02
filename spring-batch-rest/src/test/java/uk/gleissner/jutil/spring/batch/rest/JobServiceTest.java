package uk.gleissner.jutil.spring.batch.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gleissner.jutil.spring.batch.rest.MockSetup.configureMock;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private JobExplorer jobExplorer;

    @Mock
    private JobLocator jobLocator;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private Job job;

    @Mock
    private JobExecution jobExecution;

    private JobService jobService;

    @Before
    public void setUp() throws NoSuchJobException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        configureMock(jobExplorer);

        when(jobLocator.getJob(anyString())).thenAnswer(i -> job);
        when(job.getJobParametersValidator()).thenReturn(new DefaultJobParametersValidator());

        when(jobExecution.getStatus()).thenReturn(BatchStatus.STARTED);
        when(jobExecution.getEndTime()).thenReturn(new Date());
        when(jobExecution.getStartTime()).thenReturn(new Date());
        when(jobExecution.getExitStatus()).thenReturn(ExitStatus.UNKNOWN);
        when(jobExecution.getFailureExceptions()).thenReturn(Collections.EMPTY_LIST);
        when(jobExecution.getJobId()).thenReturn(1L);
        when(jobExecution.getId()).thenReturn(2L);
        when(jobRepository.createJobExecution(any(), any())).thenReturn(jobExecution);

        jobService = new JobService(jobLocator, jobExplorer, jobRepository);
    }

    @Test
    public void jobExecutionsAll() {
        Collection<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(empty(), empty(), empty(), empty());
        assertThat(jes).hasSize(6);
    }

    @Test
    public void jobExecutionsId() {
        uk.gleissner.jutil.spring.batch.rest.domain.JobExecution je = jobService.jobExecution(1L);
        assertThat(je).isNotNull();
    }

    @Test
    public void jobExecutionsJobNameRegexp() {
        Collection<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(Optional.of("j1"), empty(), empty(), empty());
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsStatus() {
        Collection<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(Optional.of("j1"), Optional.of(ExitStatus.COMPLETED), empty(), empty());
        assertThat(jes).hasSize(2);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobInstances() {
        Collection<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(empty(), Optional.of(ExitStatus.FAILED), Optional.of(1), empty());
        assertThat(jes).hasSize(3);
    }

    @Test
    public void jobExecutionsMaxNumberOfJobExecutionsPerInstance() {
        Collection<uk.gleissner.jutil.spring.batch.rest.domain.JobExecution> jes = jobService.jobExecutions(empty(), Optional.of(ExitStatus.COMPLETED), empty(), Optional.of(1));
        assertThat(jes).hasSize(3);
    }

    @Test
    public void launchJob() {
        uk.gleissner.jutil.spring.batch.rest.domain.JobExecution jobExecution = jobService.launch("foo");
        assertThat(jobExecution.getJobName()).matches("foo");
        assertThat(jobExecution.getStatus()).matches(BatchStatus::isRunning);
    }

    @Test
    public void jobs() {
        Collection<uk.gleissner.jutil.spring.batch.rest.domain.Job> jobs = jobService.jobs();
        assertThat(jobs).hasSize(2);

    }
}