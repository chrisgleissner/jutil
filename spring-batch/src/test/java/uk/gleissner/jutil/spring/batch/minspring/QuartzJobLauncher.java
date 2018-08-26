package uk.gleissner.jutil.spring.batch.minspring;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;

import static org.slf4j.LoggerFactory.getLogger;

public class QuartzJobLauncher extends QuartzJobBean {

    static final String JOB_LOCATOR = "jobLocator";
    static final String JOB_LAUNCHER = "jobLauncher";

    private static final Logger logger = getLogger(QuartzJobLauncher.class);

    private String jobName;

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            Job job = ((JobLocator) context.get(JOB_LOCATOR)).getJob(jobName);
            logger.info("Starting job {}", job.getName());
            JobExecution jobExecution = ((JobLauncher) context.get(JOB_LAUNCHER)).run(job, new JobParameters());
            logger.info("{}_{} was completed successfully", job.getName(), jobExecution.getId());
        } catch (Exception e) {
            logger.error("Encountered job execution exception", e);
        }
    }
}