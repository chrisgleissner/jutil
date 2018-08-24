package uk.gleissner.jutil.spring.simplebatch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            countDownLatch.countDown();
        }
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }
}