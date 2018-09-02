package uk.gleissner.jutil.spring.batch.rest.domain;

import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;
import uk.gleissner.jutil.spring.batch.rest.JobExecutionController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Getter
public class JobExecutionResource extends ResourceSupport {

    private final JobExecution jobExecution;

    public JobExecutionResource(final JobExecution jobExecution) {
        this.jobExecution = jobExecution;
        final long id = jobExecution.getId();
        add(linkTo(methodOn(JobExecutionController.class).get(id)).withSelfRel());
    }
}
