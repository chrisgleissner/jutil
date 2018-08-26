package uk.gleissner.jutil.spring.batch.rest;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

@Service
public class JobService {

    private final static Logger logger = LoggerFactory.getLogger(JobService.class);

    private JobExplorer jobExplorer;

    @Autowired
    public JobService(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    public List<JobExecution> jobExecutions(Optional<String> jobNameRegexp,
                                            Optional<ExitStatus> exitStatus,
                                            Optional<Integer> maxNumberOfJobInstances,
                                            Optional<Integer> maxNumberOfJobExecutionsPerInstance) {

        List<String> jobNames = jobExplorer.getJobNames();
        if (jobNameRegexp.isPresent()) {
            Pattern p = Pattern.compile(jobNameRegexp.get());
            jobNames = jobNames.stream().filter(jn -> p.matcher(jn).matches()).collect(toList());
        }

        List<JobExecution> allJobExecutions = Lists.newLinkedList();
        for (String jobName : jobNames) {
            try {
                int jobInstanceCount = jobExplorer.getJobInstanceCount(jobName);
                List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName,
                        min(0, jobInstanceCount - maxNumberOfJobInstances.orElse(MAX_VALUE)),
                        maxNumberOfJobInstances.orElse(MAX_VALUE));
                for (JobInstance jobInstance : jobInstances) {
                    List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance).stream()
                            .limit(maxNumberOfJobExecutionsPerInstance.orElse(MAX_VALUE))
                            .map(JobExecution::fromSpring)
                            .collect(toList());
                    allJobExecutions.addAll(jobExecutions);
                }
            } catch (Exception e) {
                logger.warn("Could not get job executions for job {}", jobName, e);
            }
        }
        return allJobExecutions.stream().filter(je -> !exitStatus.isPresent() || exitStatus.get().equals(je.getExitStatus())).collect(toList());
    }
}
