package uk.gleissner.jutil.spring.batch.rest.domain;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.hateoas.ResourceSupport;

@Value
@AllArgsConstructor
public class Job extends ResourceSupport {
    private String name;
}
