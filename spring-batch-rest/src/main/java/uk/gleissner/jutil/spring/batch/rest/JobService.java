package uk.gleissner.jutil.spring.batch.rest;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    public List<JobExecution> getJobExecutions(Optional<JobExecution.Status> status) {
        // TODO
        return Lists.newArrayList();
    }
}
