package com.github.chrisgleissner.jutil.table;

import com.github.chrisgleissner.jutil.table.format.Utf8TableFormat;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

@Slf4j
public class TablePrinterTest {

    private static final List<String> HEADERS = newArrayList("id", "name", "age");
    private static final List<List<String>> DATA = newArrayList(
            newArrayList("1", "john", null),
            newArrayList("2", "tom", "20"),
            newArrayList("3", "verylongname", null),
            newArrayList("4", "mary", "30"));


    @Test
    public void nullHeaders() {
        assertEquals("", TablePrinter.builder().build().print(null, newArrayList()));
    }

    @Test
    public void asciiNullData() throws IOException {
        assertTable("asciiNoData", TablePrinter.builder().build().print(HEADERS, null));
    }

    @Test
    public void asciiNoData() throws IOException {
        assertTable("asciiNoData", TablePrinter.builder().build().print(HEADERS, newArrayList()));
    }

    @Test
    public void utfNoData() throws IOException {
        assertTable("utfNoData", TablePrinter.builder().tableFormat(new Utf8TableFormat()).build().print(HEADERS, newArrayList()));
    }

    @Test
    public void asciiHorizontalDividers() throws IOException {
        assertTable("asciiHorizontalDividers", TablePrinter.builder().horizontalDividers(true).build().print(HEADERS, DATA));
    }

    @Test
    public void utfHorizontalDividers() throws IOException {
        assertTable("utfHorizontalDividers", TablePrinter.builder().horizontalDividers(true).tableFormat(new Utf8TableFormat()).build().print(HEADERS, DATA));
    }

    @Test
    public void ascii() throws IOException {
        assertTable("ascii", TablePrinter.builder().nullValue("n/a").maxCellWidth(8).build().print(HEADERS, DATA));
    }

    @Test
    public void utf() throws IOException {
        assertTable("utf", TablePrinter.builder().nullValue("").maxCellWidth(10)
                .startRow(1).endRow(3)
                .tableFormat(new Utf8TableFormat()).build().print(HEADERS, DATA));
    }

    private void assertTable(String expectedFileNameWithoutSuffix, String actualTableString) throws IOException {
        assertEquals(Files.asCharSource(new File(format("src/test/resources/%s.txt", expectedFileNameWithoutSuffix)), Charsets.UTF_8).read(), actualTableString);
    }
}