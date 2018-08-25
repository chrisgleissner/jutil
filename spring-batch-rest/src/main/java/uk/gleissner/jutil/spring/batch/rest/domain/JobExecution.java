package uk.gleissner.jutil.spring.batch.rest.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobExecution {

    public enum Status { STARTED, COMPLETED, FAILED };

    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Status status;

}
