package uk.gleissner.jutil.spring.batch.rest.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.Throwables;
import lombok.*;
import lombok.experimental.Wither;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;

import java.time.LocalDateTime;
import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static uk.gleissner.jutil.spring.batch.rest.util.DateUtil.localDateTime;

@Value
@Builder
public class JobExecution {

    public static JobExecution fromSpring(org.springframework.batch.core.JobExecution je) {
        return JobExecution.builder()
                .jobId(je.getJobId())
                .id(je.getId())
                .startTime(localDateTime(je.getStartTime()))
                .endTime(localDateTime(je.getEndTime()))
                .exitStatus(je.getExitStatus())
                .status(je.getStatus())
                .exceptions(je.getFailureExceptions().stream().map(e -> e.getMessage() + ": " + Throwables.getStackTraceAsString(e)).collect(toList()))
                .build();
    }

    private long id;
    private long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ExitStatus exitStatus;
    private BatchStatus status;
    private Collection<String> exceptions;

}
