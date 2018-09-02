package uk.gleissner.jutil.spring.batch.rest;

import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gleissner.jutil.spring.batch.rest.domain.Job;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.slf4j.LoggerFactory.getLogger;

@RestController
@RequestMapping(value = "/jobExecution", produces = "application/hal+json")
public class JobExecutionController {

    private static final Logger logger = getLogger(JobExecutionController.class);

    @Autowired
    private JobService jobService;

    @GetMapping("/{id}")
    public JobExecution get(@PathVariable long id) {
        return jobService.jobExecution(id);
    }

    @GetMapping
    public Collection<JobExecution> all(
            @RequestParam(value = "jobName", required = false) String jobNameRegexp,
            @RequestParam(value = "exitStatus", required = false) ExitStatus jobExecutionStatus,
            @RequestParam(value = "maxNumberOfJobInstances", required = false) Integer maxNumberOfJobInstances,
            @RequestParam(value = "maxNumberOfJobExecutionsPerInstance", required = false) Integer maxNumberOfJobExecutionsPerInstance) {
        return jobService.jobExecutions(
                Optional.ofNullable(jobNameRegexp),
                Optional.ofNullable(jobExecutionStatus),
                Optional.ofNullable(maxNumberOfJobInstances),
                Optional.ofNullable(maxNumberOfJobExecutionsPerInstance));
    }
}
