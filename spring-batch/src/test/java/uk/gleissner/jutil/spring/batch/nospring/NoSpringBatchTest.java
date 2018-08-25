package uk.gleissner.jutil.spring.batch.nospring;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.test.JobLauncherTestUtils;

import static org.junit.Assert.assertEquals;

@Ignore
public class NoSpringBatchTest {

    private JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();

    @Test
    public void testProgramaticJob() throws Exception {

        JobParameters parameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters()).toJobParameters();

        TaskletStep taskletStep = new TaskletStep();
        taskletStep.setName("step1");
        taskletStep.setJobRepository(jobLauncherTestUtils.getJobRepository());
        //taskletStep.setTransactionManager(transactionManager);
        //Tasklet tasklet = new SplitFilesTasklet();
        //taskletStep.setTasklet(tasklet);

        SimpleJob job = new SimpleJob("test");
        job.addStep(taskletStep);
        job.setJobRepository(jobLauncherTestUtils.getJobRepository());

        jobLauncherTestUtils.setJob(job);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}
