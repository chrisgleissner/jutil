package uk.gleissner.jutil.spring.batch.rest;

import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
public class JobController {

    private static final Logger logger = getLogger(JobController.class);

    @Autowired
    private JobService jobService;

    @RequestMapping("/jobExecutions")
    @ResponseBody
    public List<JobExecution> jobExecutions(
            @RequestParam(value="jobName", required=false) String jobNameRegexp,
            @RequestParam(value="exitStatus", required=false) ExitStatus jobExecutionStatus,
            @RequestParam(value="maxNumberOfJobInstances", required=false) Integer maxNumberOfJobInstances,
            @RequestParam(value="maxNumberOfJobExecutionsPerInstance", required=false) Integer maxNumberOfJobExecutionsPerInstance) {
        List<JobExecution> jobExecutions = jobService.jobExecutions(
                Optional.ofNullable(jobNameRegexp),
                Optional.ofNullable(jobExecutionStatus),
                Optional.ofNullable(maxNumberOfJobInstances),
                Optional.ofNullable(maxNumberOfJobExecutionsPerInstance));
        logger.info("Returning Job executions: {}", jobExecutions);
        return jobExecutions;
    }
}
