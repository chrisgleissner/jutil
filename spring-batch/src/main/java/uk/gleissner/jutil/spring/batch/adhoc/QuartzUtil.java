package uk.gleissner.jutil.spring.batch.adhoc;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static java.time.ZoneId.systemDefault;

public class QuartzUtil {

    @Data
    @AllArgsConstructor
    public static class JobInfo {
        private String name;
        private String group;
        private String cron;
        private LocalDateTime nextFireTime;
    }

    public static List<JobInfo> jobInfos(Scheduler scheduler) throws SchedulerException {
        List<JobInfo> jobInfos = new LinkedList<>();
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String name = jobKey.getName();
                String group = jobKey.getGroup();
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);
                String cron = null;
                if (trigger instanceof CronTrigger) {
                    cron = ((CronTrigger) trigger).getCronExpression();
                }
                LocalDateTime nextFireTime = trigger.getNextFireTime().toInstant().atZone(systemDefault()).toLocalDateTime();
                jobInfos.add(new JobInfo(name, group, cron, nextFireTime));
            }
        }
        return jobInfos;
    }
}
