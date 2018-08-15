package uk.gleissner.log;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import uk.gleissner.log.Log;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class LogTest {

    static final File LOG_DIR = new File("target/log");

    static {
        try {
            FileUtils.cleanDirectory(LOG_DIR);
            assertTrue(FileUtils.listFiles(LOG_DIR, new String[]{".log"}, true).isEmpty());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void canLog() {
        new Log(2, 10);
    }
}