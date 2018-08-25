package uk.gleissner.jutil.spring.batch.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import java.util.List;
import java.util.Optional;

@RestController
public class JobController {

    @Autowired
    private JobService jobService;

    @RequestMapping("/jobExecutions")
    public List<JobExecution> jobExecutions(@RequestParam(value="status", required=false) JobExecution.Status status) {
        return jobService.getJobExecutions(Optional.ofNullable(status));
    }
}
