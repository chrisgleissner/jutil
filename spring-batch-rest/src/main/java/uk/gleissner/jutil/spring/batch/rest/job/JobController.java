package uk.gleissner.jutil.spring.batch.rest.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;
import uk.gleissner.jutil.spring.batch.rest.jobexecution.JobExecutionResource;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/job", produces = "application/hal+json")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping("/{jobName}")
    public JobResource get(@PathVariable String jobName) {
        return new JobResource(jobService.job(jobName));
    }

    @GetMapping
    public Resources<JobResource> all() {
        Collection<JobResource> jobs = jobService.jobs().stream().map(JobResource::new).collect(toList());
        return new Resources<>(jobs, linkTo(methodOn(JobController.class).all()).withSelfRel());

    }

    @PutMapping("/{jobName}")
    public JobExecutionResource put(@PathVariable String jobName) {
        return new JobExecutionResource(jobService.launch(jobName));
    }
}
