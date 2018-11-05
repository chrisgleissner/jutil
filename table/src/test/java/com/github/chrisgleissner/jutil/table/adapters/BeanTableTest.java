package com.github.chrisgleissner.jutil.table.adapters;

import lombok.Data;
import org.junit.Test;

import java.util.Arrays;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;

public class BeanTableTest {

    @Test
    public void works() {
        Iterable<Person> people = Arrays.asList(new Person("john", "doe", 30),
                new Person("mary", "poppins", 40));
        assertTable("beans", DefaultTablePrinter.print(new BeanTable(people)));
    }

    @Data
    static class Person {
        final String firstName;
        final String lastName;
        final int age;
    }

}