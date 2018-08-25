package uk.gleissner.jutil.spring.batch.rest;

import com.google.common.collect.Lists;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gleissner.jutil.spring.batch.rest.domain.JobExecution;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JobService {

    @Autowired
    private JobExplorer jobExplorer;

    public List<JobExecution> getJobExecutions(Optional<JobExecution.Status> status) {
        int startRow = 0;
        int count = 1000;
//        jobExplorer.getJobNames().stream()
//                .flatMap(jn -> jobExplorer.getJobInstances(jn, startRow, count))
//                .flatMap(jobExplorer::getJobExecutions).collect(Collectors.toList());
        // TODO
        return Lists.newArrayList();
    }
}
