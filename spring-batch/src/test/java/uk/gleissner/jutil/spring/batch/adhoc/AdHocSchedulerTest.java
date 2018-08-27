package uk.gleissner.jutil.spring.batch.adhoc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gleissner.jutil.spring.batch.CacheItemWriter;
import uk.gleissner.jutil.spring.batch.JobCompletionNotificationListener;
import uk.gleissner.jutil.spring.batch.Person;

import java.util.function.Supplier;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the ad-hoc Quartz scheduling of Spring Batch jobs, allowing for programmatic scheduling after Spring wiring.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AdHocSchedulerConfig.class)
public class AdHocSchedulerTest {

    private static final String CRON_SCHEDULE_TO_TRIGGER_EVERY_SECOND = "0/1 * * * * ?";

    private static final String CSV1_FILENAME = "sample-data.csv";
    private static final int CSV1_ROWS = 5;

    private static final String CSV2_FILENAME = "sample-data2.csv";
    private static final int CSV2_ROWS = 7;

    private static final int NUMBER_OF_JOBS = 2;
    private static final int NUMBER_OF_EXECUTIONS_PER_JOB = 2;

    private static final String JOB_NAME_1 = "importUserFromCsv1";
    private static final String JOB_NAME_2 = "importUserFromCsv2";

    @Autowired
    private AdHocScheduler adHocScheduler;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener listener = new JobCompletionNotificationListener();
    private CacheItemWriter<Person> writer = new CacheItemWriter();

    @Test
    public void scheduleWorks() throws InterruptedException, DuplicateJobException {
        adHocScheduler.schedule(JOB_NAME_1, csvImportJobSupplier(JOB_NAME_1, CSV1_FILENAME), CRON_SCHEDULE_TO_TRIGGER_EVERY_SECOND);
        adHocScheduler.schedule(JOB_NAME_2, csvImportJobSupplier(JOB_NAME_2, CSV2_FILENAME), CRON_SCHEDULE_TO_TRIGGER_EVERY_SECOND);

        listener.awaitCompletionOfJobs(NUMBER_OF_JOBS * NUMBER_OF_EXECUTIONS_PER_JOB, 5_000);

        assertThat(writer.getItems().size(), is(NUMBER_OF_EXECUTIONS_PER_JOB * (CSV1_ROWS + CSV2_ROWS)));
        assertThat(writer.getItems().iterator().next().getFirstName(), is("JILL"));
    }

    private Supplier<Job> csvImportJobSupplier(String jobName, String csvFilename) {
        return () -> jobBuilderFactory.get(jobName)
                .incrementer(new RunIdIncrementer()) // adds unique parameter on each run so that job can be rerun
                .listener(listener)
                .flow(stepBuilderFactory.get("step")
                        .<Person, Person>chunk(10)
                        .reader(new FlatFileItemReaderBuilder<Person>()
                                .name("personItemReader")
                                .resource(new ClassPathResource(csvFilename))
                                .delimited()
                                .names(new String[]{"firstName", "lastName"})
                                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                                    setTargetType(Person.class);
                                }})
                                .build())
                        .processor((ItemProcessor<Person, Person>) (person)
                                -> new Person(person.getFirstName().toUpperCase(), person.getLastName().toUpperCase()))
                        .writer(writer)
                        .allowStartIfComplete(true)
                        .build())
                .end().build();
    }
}
