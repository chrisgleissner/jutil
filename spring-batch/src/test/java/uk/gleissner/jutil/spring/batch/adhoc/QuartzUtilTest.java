package uk.gleissner.jutil.spring.batch.adhoc;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.repeatMinutelyForever;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class QuartzUtilTest {

    public static final int NUM_JOBS = 3;

    @Test
    public void jobInfos() throws SchedulerException {
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.clear();

        for (int i = 0; i < NUM_JOBS; i++) {
            JobDetail jobDetail = newJob(DummyJobLauncher.class)
                    .withIdentity("n" + i, "g" + i)
                    .usingJobData(QuartzJobLauncher.JOB_NAME, "n" + i)
                    .build();

            TriggerBuilder<Trigger> triggerBuilder = newTrigger()
                    .withIdentity("t" + i, "g" + i)
                    .forJob("n" + i, "g" + i);
            if (i % 2 == 0)
                triggerBuilder.withSchedule(cronSchedule(format("%d * * * * ?", i)));
            else
                triggerBuilder.withSchedule(repeatMinutelyForever());
            scheduler.scheduleJob(jobDetail, triggerBuilder.build());
        }


        List<QuartzUtil.JobInfo> jobInfos = QuartzUtil.jobInfos(scheduler);
        assertThat(jobInfos).hasSize(NUM_JOBS);
        assertThat(jobInfos).extracting(QuartzUtil.JobInfo::getNextFireTime).isNotNull();
        log.info("Job infos: {}", jobInfos);
    }

    public static class DummyJobLauncher implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            log.info("Executing job with context {}", context);
        }
    }
}