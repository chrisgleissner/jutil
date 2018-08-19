package uk.gleissner.jutil.log;

import org.junit.Test;

import java.io.File;

import static org.apache.commons.io.FileUtils.cleanDirectory;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

public class ConcurrentLoggerTest {

    static final File LOG_DIR = new File("target/log");

    static {
        try {
            if (LOG_DIR.exists()) {
                cleanDirectory(LOG_DIR);
            }
            assertThat(getNumberOfLogFiles(), is(0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void canLog() throws InterruptedException {
        new ConcurrentLogger(2, 10);
        assertThat(getNumberOfLogFiles(), is(greaterThan(0)));
    }

    private static int getNumberOfLogFiles() {
        return LOG_DIR.exists() ? listFiles(LOG_DIR, new String[]{"log"}, true).size() : 0;
    }
}
