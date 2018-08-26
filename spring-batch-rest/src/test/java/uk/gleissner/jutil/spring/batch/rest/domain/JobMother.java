package uk.gleissner.jutil.spring.batch.rest.domain;

public class JobMother {

    public static JobExecution jobExecution(long id) {
        return JobExecution.builder().jobId(id).build();
    }
}
