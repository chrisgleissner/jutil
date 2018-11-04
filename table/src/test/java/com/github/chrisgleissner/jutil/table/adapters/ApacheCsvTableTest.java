package com.github.chrisgleissner.jutil.table.adapters;

import org.apache.commons.csv.CSVFormat;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.CSV_FILE;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;

public class ApacheCsvTableTest {

    @Test
    public void iterable() throws IOException {
        assertTable("csv", DefaultTablePrinter.print(
                new ApacheCsvTable(CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new FileReader(CSV_FILE)))));
    }
}