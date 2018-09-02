package uk.gleissner.jutil.spring.batch.rest.jobdetail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/jobDetail", produces = "application/hal+json")
public class JobDetailController {

    @Autowired
    private JobDetailService jobDetailService;

    @GetMapping("/{quartzGroupName}/{quartzJobName}")
    public JobDetailResource get(@PathVariable String quartzGroupName, @PathVariable String quartzJobName) {
        return new JobDetailResource(jobDetailService.jobDetail(quartzGroupName, quartzJobName));
    }

    @GetMapping
    public Resources<JobDetailResource> all(@RequestParam(value = "enabled", required = false) Boolean enabled,
                                            @RequestParam(value = "springBatchJobName", required = false) String springBatchJobName) {
        return new Resources<>(jobDetailService.all(Optional.ofNullable(enabled), Optional.ofNullable(springBatchJobName)).stream()
                .map(JobDetailResource::new).collect(toList()),
                linkTo(methodOn(uk.gleissner.jutil.spring.batch.rest.jobdetail.JobDetailController.class).all(enabled, springBatchJobName)).withSelfRel());
    }
}
