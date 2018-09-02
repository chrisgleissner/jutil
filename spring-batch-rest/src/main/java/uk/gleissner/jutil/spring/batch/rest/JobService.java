package uk.gleissner.jutil.spring.batch.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;
import uk.gleissner.jutil.spring.batch.rest.domain.Job;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
public class JobService {

    private final static Logger logger = LoggerFactory.getLogger(JobService.class);

    private JobLocator jobLocator;
    private JobLauncher jobLauncher;
    private JobExplorer jobExplorer;


    @Autowired
    public JobService(JobLocator jobLocator,
                      JobExplorer jobExplorer,
                      JobRepository jobRepository) {

        this.jobLocator = jobLocator;
        this.jobExplorer = jobExplorer;

        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        jobLauncher.setTaskExecutor(simpleAsyncTaskExecutor);
        this.jobLauncher = jobLauncher;
    }

    public JobExecution jobExecution(long executionId) {
        return JobExecution.fromSpring(null, jobExplorer.getJobExecution(executionId));

    }

    public Collection<JobExecution> jobExecutions(Optional<String> jobNameRegexp,
                                                  Optional<ExitStatus> exitStatus,
                                                  Optional<Integer> maxNumberOfJobInstances,
                                                  Optional<Integer> maxNumberOfJobExecutionsPerInstance) {
        logger.debug("Getting job excecutions(jobNameRegexp={}, exitStatus={}, maxNumberOfJobInstances{}, maxNumberOfJobExecutionsPerInstance={}",
                jobNameRegexp, exitStatus, maxNumberOfJobInstances, maxNumberOfJobExecutionsPerInstance);

        Set<JobExecution> allJobExecutions = new TreeSet<>();
        List<String> jobNames = jobExplorer.getJobNames();
        if (jobNameRegexp.isPresent()) {
            Pattern p = Pattern.compile(jobNameRegexp.get());
            jobNames = jobNames.stream().filter(jn -> p.matcher(jn).matches()).collect(toList());
        }

        for (String jobName : jobNames) {
            try {
                int jobInstanceCount = jobExplorer.getJobInstanceCount(jobName);
                List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName,
                        Math.max(0, jobInstanceCount - maxNumberOfJobInstances.orElse(MAX_VALUE)),
                        maxNumberOfJobInstances.orElse(MAX_VALUE));
                for (JobInstance jobInstance : jobInstances) {
                    List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance).stream()
                            .limit(maxNumberOfJobExecutionsPerInstance.orElse(MAX_VALUE))
                            .map(je -> JobExecution.fromSpring(jobName, je))
                            .collect(toList());
                    allJobExecutions.addAll(jobExecutions);
                }
            } catch (Exception e) {
                logger.warn("Could not get job executions for job {}", jobName, e);
            }
        }
        return allJobExecutions.stream().filter(je -> !exitStatus.isPresent() || exitStatus.get().equals(je.getExitStatus())).collect(toSet());
    }

    public Collection<Job> jobs() {
        return jobExplorer.getJobNames().stream().map(n -> new Job(n)).collect(toList());
    }

    public JobExecution launch(String jobName) {
        logger.debug("Launching job {}...", jobName);
        try {
            org.springframework.batch.core.Job job = jobLocator.getJob(jobName);
            try {

                org.springframework.batch.core.JobExecution jobExecution = jobLauncher.run(job, new JobParameters());
                logger.info("Successfully launched job {}: ", jobName, jobExecution);
                return JobExecution.fromSpring(jobName, jobExecution);
            } catch (Exception e) {
                throw new RuntimeException(format("Failed to launch job: %s", jobName), e);
            }
        } catch (NoSuchJobException e) {
            throw new RuntimeException(format("Could not find job: %s", jobName), e);
        }
    }

    public Job job(String jobName) {
        return new Job(jobName);
    }
}
