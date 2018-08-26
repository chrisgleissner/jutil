package uk.gleissner.jutil.spring.batch.rest.domain;

import com.google.common.base.Throwables;
import lombok.Builder;
import lombok.Data;
import org.springframework.batch.core.BatchStatus;

import java.time.LocalDateTime;
import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static uk.gleissner.jutil.spring.batch.rest.util.DateUtil.localDateTime;

@Data
@Builder
public class JobExecution {

    public static JobExecution fromSpring(org.springframework.batch.core.JobExecution je) {
        return JobExecution.builder().jobId(je.getJobId())
                .startTime(localDateTime(je.getStartTime()))
                .endTime(localDateTime(je.getEndTime()))
                .status(je.getStatus())
                .exceptions(je.getFailureExceptions().stream().map(e -> e.getMessage() + ": " + Throwables.getStackTraceAsString(e)).collect(toList()))
                .build();
    }

    public enum Status { STARTED, COMPLETED, FAILED };

    private long jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BatchStatus status;
    private Collection<String> exceptions;

}
