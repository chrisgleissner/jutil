package com.github.chrisgleissner.jutil.table;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

public class TablePrinterFixtures {

    public static final File CSV_FILE = new File("src/test/resources/csvSample.csv");

    public static void assertTable(String expectedFileNameWithoutSuffix, String actualTableString) {
        try {
            assertEquals(Files.asCharSource(new File(format("src/test/resources/%s.txt", expectedFileNameWithoutSuffix)), Charsets.UTF_8).read(),
                    actualTableString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
