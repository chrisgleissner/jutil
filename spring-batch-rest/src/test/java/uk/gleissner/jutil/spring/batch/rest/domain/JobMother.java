package uk.gleissner.jutil.spring.batch.rest.domain;

public class JobMother {

    public static JobExecution jobExecution(String id) {
        return JobExecution.builder().id(id).build();
    }
}
