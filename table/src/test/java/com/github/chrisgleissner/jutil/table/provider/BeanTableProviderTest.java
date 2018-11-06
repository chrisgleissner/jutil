package com.github.chrisgleissner.jutil.table.provider;

import lombok.Data;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;

public class BeanTableProviderTest {

    private static final Iterable<Person> PEOPLE = Arrays.asList(new Person("john", "doe", 30),
            new Person("mary", "poppins", 40));

    @Test
    public void works() {
        assertTable("beans", DefaultTablePrinter.print(new BeanTableProvider(PEOPLE)));
    }

    @Test
    public void worksForOutputStream() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DefaultTablePrinter.print(new BeanTableProvider(PEOPLE), baos);
            assertTable("beans", baos.toString("UTF-8"));
        }
    }

    @Data
    static class Person {
        final String firstName;
        final String lastName;
        final int age;
    }

}