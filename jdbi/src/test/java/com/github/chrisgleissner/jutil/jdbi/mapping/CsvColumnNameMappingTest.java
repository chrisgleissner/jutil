package com.github.chrisgleissner.jutil.jdbi.mapping;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvColumnNameMappingTest {

    @Test
    public void pathWorks() {
        assertLabelMappings(new CsvColumnNameMapping(Path.of("src/test/resources/columnNameMappings.csv")));
    }

    private void assertLabelMappings(CsvColumnNameMapping m) {
        assertThat(m.apply("geburtstag")).isEqualTo("birthday");
        assertThat(m.apply("NACHNAME")).isEqualTo("surname");
        assertThat(m.apply("foo")).isEqualTo("foo");
    }

    @Test
    public void inputStream() throws IOException {
        assertLabelMappings(new CsvColumnNameMapping(Files.newInputStream(Path.of("src/test/resources/columnNameMappings.csv"))));
    }
}