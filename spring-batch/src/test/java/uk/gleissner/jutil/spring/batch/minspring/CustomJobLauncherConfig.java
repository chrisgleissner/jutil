package uk.gleissner.jutil.spring.batch.minspring;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ComponentScan
@EnableBatchProcessing
public class CustomJobLauncherConfig extends DefaultBatchConfigurer {

    @Override
    public void setDataSource(DataSource dataSource) {
        // override to do not set datasource even if a datasource exist.
        // initialize will use a Map based JobRepository (instead of database)
    }

}
