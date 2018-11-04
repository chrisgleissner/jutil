package com.github.chrisgleissner.jutil.table.adapters;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.junit.Before;
import org.junit.Test;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.CSV_FILE;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;

public class UnivocityTableTest {

    private CsvParser parser;

    @Before
    public void setUp() {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setHeaderExtractionEnabled(true);
        parser = new CsvParser(settings);
    }

    @Test
    public void iteratable() {
        assertTable("csv", DefaultTablePrinter.print(UnivocityTable.of(parser.iterateRecords(CSV_FILE))));
    }

    @Test
    public void list() {
        assertTable("csv", DefaultTablePrinter.print(UnivocityTable.of(parser.parseAllRecords(CSV_FILE))));
    }
}