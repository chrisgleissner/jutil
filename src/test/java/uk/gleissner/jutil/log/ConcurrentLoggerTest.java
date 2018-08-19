package uk.gleissner.jutil.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static ch.qos.logback.classic.Level.TRACE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;

@RunWith(MockitoJUnitRunner.class)
public class ConcurrentLoggerTest {

    @Mock
    private Appender mockAppender;
    @Captor
    private ArgumentCaptor<LoggingEvent> captorLoggingEvent;

    @Before
    public void setup() {
        final Logger logger = (Logger) getLogger(ConcurrentLogger.class.getName());
        logger.setLevel(TRACE);
        logger.setAdditive(false);
        logger.addAppender(mockAppender);
    }

    @After
    public void teardown() {
        final Logger logger = (Logger) getLogger(ConcurrentLogger.class.getName());
        logger.setAdditive(true);
        logger.detachAppender(mockAppender);
    }


    @Test
    public void canLog() throws InterruptedException {
        new ConcurrentLogger(2, 2);
        verify(mockAppender, times(49)).doAppend(any());
    }
}
