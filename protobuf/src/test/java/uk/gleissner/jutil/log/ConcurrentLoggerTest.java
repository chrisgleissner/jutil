/*
 * Copyright (C) 2018 Christian Gleissner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gleissner.jutil.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private Appender<ILoggingEvent> mockAppender;

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
    public void canLog() {
        new ConcurrentLogger(2, 2);
        verify(mockAppender, times(49)).doAppend(any());
    }
}
