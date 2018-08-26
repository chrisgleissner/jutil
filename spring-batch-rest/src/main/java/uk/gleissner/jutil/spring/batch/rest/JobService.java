package uk.gleissner.jutil.spring.batch.rest;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Service
public class JobService {

    private final static Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobExplorer jobExplorer;

    public List<JobExecution> getJobExecutions(Optional<String> jobNameRegexp,
                                               Optional<JobExecution.Status> jobExecutionStatus,
                                               Optional<Integer> maxNumberOfJobInstances,
                                               Optional<Integer> maxNumberOfJobExecutionsPerInstance) {

        /*
        List<String> jobNames = jobExplorer.getJobNames();
        if (jobNameRegexp.isPresent()) {
            Pattern p = Pattern.compile(jobNameRegexp.get());
            jobNames = jobNames.stream().filter(jn -> p.matcher(jn).matches()).collect(toList());
        }

        jobNames.stream()
                .flatMap(jn -> {
                    try {
                        int jobInstanceCount = jobExplorer.getJobInstanceCount(jn);
                        List<JobInstance> jobInstances = jobExplorer.getJobInstances(jn,
                                min(0, jobInstanceCount - maxNumberOfJobInstances.orElse(MAX_VALUE)), maxNumberOfJobInstances.orElse(MAX_VALUE));

                        List<JobExecution> jobExecutions = Lists.newLinkedList();
                        for (JobInstance jobInstance : jobInstances) {
                            jobExecutions.addAll(jobExplorer.getJobExecutions(jobInstance).stream()
                                    .limit(maxNumberOfJobExecutionsPerInstance.orElse(MAX_VALUE))
                                    .map(JobExecution::fromSpring)
                                    .collect(toList()));
                        }
                        return jobExecutions;
                    } catch (Exception e) {
                        logger.warn("Could not retreive job executions", e);
                        return emptyList();
                    }
                }).collect(toList());

        return allJobExecutions;
        */

        return new ArrayList();
    }
}
