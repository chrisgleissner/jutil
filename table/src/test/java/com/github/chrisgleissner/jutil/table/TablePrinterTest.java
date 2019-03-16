package com.github.chrisgleissner.jutil.table;

import com.github.chrisgleissner.jutil.table.format.Utf8TableFormat;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.github.chrisgleissner.jutil.table.TablePrinter.DefaultTablePrinter;
import static com.github.chrisgleissner.jutil.table.TablePrinterFixtures.assertTable;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@Slf4j
public class TablePrinterTest {

    private static final Iterable<String> HEADERS = asList("id", "name", "age");

    private static final Iterable<Iterable<String>> DATA = asList(
            asList("1", "john", null),
            asList("2", "tom", "20"),
            asList("3", "verylongname", null),
            asList("4", "mary", "30"));

    private static final Iterable<Iterable<String>> SOME_UTF8_DATA = asList(
            asList("1", "euro: €", "€"),
            asList("2", "drachma: ₯", "₯"),
            asList("3", "rupee: ₹", "₹"));

    @Test
    public void nullHeaders() {
        assertEquals("", DefaultTablePrinter.print(null, newArrayList()));
    }

    @Test
    public void asciiNullData() {
        assertTable("asciiNoData", DefaultTablePrinter.print(HEADERS, null));
    }

    @Test
    public void asciiWithNewlineAndCarriageReturn() {
        assertTable("asciiWithNewlineAndCarriageReturn", TablePrinter.builder().maxCellWidth(5).build().print(
                asList("id", "  1\r\n2", "3\n\r4"),
                asList(
                asList("1", "     0123456\r\n789", null),
                asList("2", null, "  0123456\n789"),
                asList("3", null, "  0123456\r789"))));
    }

    @Test
    public void asciiNoData() {
        assertTable("asciiNoData", DefaultTablePrinter.print(HEADERS, newArrayList()));
    }

    @Test
    public void asciiCustomTab() {
        assertTable("asciiCustomTab", TablePrinter.builder()
                .tabReplacementString("  ").build().print(asList("id"), asList(asList("\t1"))));
    }

    @Test
    public void asciiCustomEncoding() {
        assertTable("asciiCustomEncoding", TablePrinter.builder().encoding("ISO-8859-1").build()
                .print(asList("umlaut", "currency"), asList(asList("ß", "₯"), asList("ä", "€"))));
    }

    @Test
    public void utfNoData() {
        assertTable("utfNoData", TablePrinter.builder().tableFormat(new Utf8TableFormat()).build().print(HEADERS, newArrayList()));
    }

    @Test
    public void asciiHorizontalDividers() {
        assertTable("asciiHorizontalDividers", TablePrinter.builder().horizontalDividers(true).build().print(HEADERS, DATA));
    }

    @Test
    public void utfHorizontalDividers() {
        assertTable("utfHorizontalDividers", TablePrinter.builder().horizontalDividers(true).tableFormat(new Utf8TableFormat()).build().print(HEADERS, DATA));
    }

    @Test
    public void asciiLineNumbers() {
        assertTable("asciiLineNumbers", TablePrinter.builder().rowNumbers(true).startRow(2).build().print(HEADERS, DATA));
    }

    @Test
    public void asciiLineNumbersWithDefault() {
        assertTable("asciiLineNumbersWithDefault", TablePrinter.builder().rowNumbers(true).build().print(HEADERS, DATA));
    }

    @Test
    public void utfLineNumbers() {
        assertTable("utfLineNumbers", TablePrinter.builder().tableFormat(new Utf8TableFormat()).rowNumbers(true).build().print(HEADERS, DATA));
    }

    @Test
    public void ascii() {
        assertTable("ascii", TablePrinter.builder().nullValue("n/a").maxCellWidth(8).build().print(HEADERS, DATA));
    }

    @Test
    public void asciiWithSomeUtfContent() {
        assertTable("asciiWithSomeUtfContent", TablePrinter.builder().nullValue("").maxCellWidth(8).build().print(HEADERS, SOME_UTF8_DATA));
    }

    @Test
    public void utfWithSomeUtfContent() {
        assertTable("utfWithSomeUtfContent", TablePrinter.builder().nullValue("").maxCellWidth(8)
                .tableFormat(new Utf8TableFormat()).build().print(HEADERS, SOME_UTF8_DATA));
    }

    @Test
    public void utf() {
        assertTable("utf", TablePrinter.builder().nullValue("").maxCellWidth(10)
                .startRow(1).endRow(3)
                .tableFormat(new Utf8TableFormat()).build().print(HEADERS, DATA));
    }

    @Test
    public void utfWithoutWraparound() {
        assertTable("utfWithoutWraparound", TablePrinter.builder().nullValue("").wraparound(false).maxCellWidth(10)
                .startRow(1).endRow(3)
                .tableFormat(new Utf8TableFormat()).build().print(HEADERS, DATA));
    }

    @Test
    public void nullHeadersWithNonEmptyData() {
        assertTable("nullHeadersWithNonEmptyData", DefaultTablePrinter.print(null, DATA));

    }

    @Test
    public void headerColumnCountTooLarge() {
        Iterable<String> headers = asList("id", "name", "age", "city");
        Iterable<Iterable<String>> data = asList(
                asList("1", "john", null),
                asList("2", "tom", "20"),
                asList("3", "verylongname", null),
                asList("4", "mary", "30"));
        assertTable("headerColumnCountTooLarge", DefaultTablePrinter.print(headers, data));
    }

    @Test
    public void newLinesAndWraparound() {
        Iterable<String> headers = asList("id", "name", "age", "city");
        Iterable<Iterable<String>> data = asList(
                asList("1", "johnWithVeryLong\tName", "15", "london"),
                asList("2", "tom\nWithNewLinesAndVeryLongName", "\n\n\n20", "\n\n\n\n\nmunich"),
                asList("3", "verylongname", null),
                asList("4", "mary", "30"));
        assertTable("newLinesAndWraparound", TablePrinter.builder().maxCellWidth(10).build().print(headers, data));
    }

    @Test
    public void headerColumnCountTooLittle() {
        Iterable<String> headers = asList("id", "name");
        Iterable<Iterable<String>> data = asList(
                asList("1", "john", null),
                asList("2", "tom", "20"),
                asList("3", "verylongname", null),
                asList("4", "mary", "30"));
        assertTable("headerColumnCountTooLittle", DefaultTablePrinter.print(headers, data));

    }

    @Test
    public void rowColumnCountVaries() {
        Iterable<String> headers = asList("id", "name", "age");
        Iterable<Iterable<String>> data = asList(
                asList("1", "john", null),
                asList("2", "tom", "20", "munich"),
                asList("3", "verylongname", null),
                asList("4", "mary", "30", "orlando", "usa"));
        assertTable("rowColumnCountVaries", DefaultTablePrinter.print(headers, data));
    }

    @Test
    public void canWriteToOutputStream() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            TablePrinter.builder().nullValue("").maxCellWidth(10)
                    .startRow(1).endRow(3)
                    .tableFormat(new Utf8TableFormat()).build().print(HEADERS, DATA, baos);
            assertTable("utf", baos.toString("UTF-8"));
        }
    }
}