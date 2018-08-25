package uk.gleissner.jutil.spring.batch.minspring;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
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
import org.springframework.stereotype.Component;
import uk.gleissner.jutil.spring.batch.CacheItemWriter;
import uk.gleissner.jutil.spring.batch.JobCompletionNotificationListener;
import uk.gleissner.jutil.spring.batch.Person;

import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static java.lang.System.currentTimeMillis;

@Component
public class CustomJobLauncher {

    @Autowired
    private org.springframework.batch.core.launch.JobLauncher launcher;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    private JobCompletionNotificationListener listener = new JobCompletionNotificationListener();
    private CacheItemWriter<Person> writer = new CacheItemWriter();
    private AtomicInteger id = new AtomicInteger();

    public void launch(String csvFilename) {
        try {
            launcher.run(job("importUserJob" + id.getAndIncrement(), csvFilename),
                    new JobParametersBuilder().addLong("launchTime", currentTimeMillis()).toJobParameters());
        } catch (Exception e) {
            throwIfUnchecked(e);
        }
    }

    private Job job(String jobName, String csvFilename) {
        FlatFileItemReader<Person> reader = new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource(csvFilename))
                .delimited()
                .names(new String[]{"firstName", "lastName"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                    setTargetType(Person.class);
                }})
                .build();

        TaskletStep step1 = stepBuilderFactory.get("step1")
                .<Person, Person>chunk(10)
                .reader(reader)
                .processor((ItemProcessor<Person, Person>) (person) -> new Person(person.getFirstName().toUpperCase(), person.getLastName().toUpperCase()))
                .writer(writer)
                .build();

        return jobBuilderFactory.get(jobName)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();

    }

    public JobCompletionNotificationListener getListener() {
        return listener;
    }

    public CacheItemWriter<Person> getWriter() {
        return writer;
    }
}