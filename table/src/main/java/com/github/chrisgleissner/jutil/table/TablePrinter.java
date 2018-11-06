package com.github.chrisgleissner.jutil.table;

import com.github.chrisgleissner.jutil.table.provider.SimpleTableProvider;
import com.github.chrisgleissner.jutil.table.provider.TableProvider;
import com.github.chrisgleissner.jutil.table.format.AsciiTableFormat;
import com.github.chrisgleissner.jutil.table.format.TableFormat;
import lombok.Builder;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Pretty prints a table. A table consists of an optional header, followed by a random number of rows. Ideally, the number
 * of columns in the header and in each row should be identical, but the printer handles diverging number of columns
 * by padding missing data.
 */
@Builder
public class TablePrinter {

    public static TablePrinter DefaultTablePrinter = TablePrinter.builder().build();

    @Builder.Default
    private String nullValue = "";
    @Builder.Default
    private TableFormat tableFormat = new AsciiTableFormat();
    @Builder.Default
    private int maxCellWidth = 100;
    @Builder.Default
    private int startRow = 0;
    @Builder.Default
    private int endRow = MAX_VALUE;
    @Builder.Default
    private boolean horizontalDividers = false;
    @Builder.Default
    private boolean rowNumbers = false;

    private class TableString {
        private TableData tableData;
        private PrintWriter pw;

        public void write(TableProvider tableProvider, OutputStream os) {
            this.pw = new PrintWriter(os);
            tableData = new TableData(tableProvider);
            if (tableData.exists()) {
                printTopEdge();
                printHeaders(tableData.getHeaders());
                printUnderHeaders(tableData.getRows().isEmpty());

                if (!tableData.getRows().isEmpty()) {
                    for (int i = 0; i < tableData.getRows().size(); i++) {
                        if (i > 0 && horizontalDividers)
                            printHorizontalDivider();
                        printRow(tableData.getRows().get(i));
                    }
                    printBottomEdge();
                }
            }
            pw.flush();
        }

        private void printRow(Iterable<String> row) {
            print(tableFormat.getVerticalBorderFill(true), tableFormat.getVerticalBorderFill(false),
                    tableFormat.getVerticalBorderFill(true), row);
        }

        private void printHeaders(Iterable<String> headers) {
            print(tableFormat.getVerticalBorderFill(true), tableFormat.getVerticalBorderFill(false),
                    tableFormat.getVerticalBorderFill(true), headers);
        }

        private void print(char left, char divider, char right, Iterable<String> cells) {
            pw.append(left);
            Iterator<String> cellIterator = cells.iterator();
            for (int i = 0; i < tableData.numberOfCellWidths(); i++) {
                if (i > 0)
                    pw.append(divider);
                pw.append(' ');

                String cell = null;
                if (cellIterator.hasNext())
                    cell = cellIterator.next();
                cell = cell == null ? nullValue : cell;
                pw.append(cell, 0, min(maxCellWidth, cell.length()));

                for (int j = 0; j < tableData.getCellWidth(i) - cellLength(cell); j++)
                    pw.append(' ');

                pw.append(' ');
            }
            pw.append(right);
            pw.append('\n');
        }

        private void printHorizontalDivider() {
            printHorizontalLine(tableFormat.getLeftEdgeBorderDivider(false), tableFormat.getCross(false, false),
                    tableFormat.getHorizontalBorderFill(false, false), tableFormat.getRightEdgeBorderDivider(false));
        }

        private void printUnderHeaders(boolean emptyData) {
            if (emptyData)
                printHorizontalLine(tableFormat.getBottomLeftCorner(), tableFormat.getCross(true, emptyData),
                        tableFormat.getHorizontalBorderFill(false, true), tableFormat.getBottomRightCorner());
            else
                printHorizontalLine(tableFormat.getLeftEdgeBorderDivider(true), tableFormat.getCross(true, emptyData),
                        tableFormat.getHorizontalBorderFill(false, true), tableFormat.getRightEdgeBorderDivider(true));
        }

        private void printTopEdge() {
            printHorizontalLine(tableFormat.getTopLeftCorner(), tableFormat.getTopEdgeBorderDivider(),
                    tableFormat.getHorizontalBorderFill(true, false), tableFormat.getTopRightCorner());
        }

        private void printBottomEdge() {
            printHorizontalLine(tableFormat.getBottomLeftCorner(), tableFormat.getBottomEdgeBorderDivider(),
                    tableFormat.getHorizontalBorderFill(true, false), tableFormat.getBottomRightCorner());
        }

        private void printHorizontalLine(char left, char divider, char middle, char right) {
            pw.append(left);
            for (int i = 0; i < tableData.numberOfCellWidths(); i++) {
                if (i > 0)
                    pw.append(divider);
                for (int j = 0; j < tableData.getCellWidth(i) + 2; j++)
                    pw.append(middle);
            }
            pw.append(right);
            pw.append('\n');
        }
    }

    private int cellLength(String s) {
        return min(maxCellWidth, (s == null ? nullValue : s).length());
    }

    @Data
    private class TableData {
        private final Map<Integer, Integer> cellWidths = new TreeMap<>();
        private final List<String> headers;
        private final List<Iterable<String>> rows;

        private TableData(TableProvider tableProvider) {
            rows = prepareRows(tableProvider);
            updateCellWidthsForRows();

            headers = prepareHeaders(tableProvider);
            updateCellWidthsForHeaders();
        }

        private List<String> prepareHeaders(TableProvider tableProvider) {
            Iterable<String> headerIteratable = tableProvider.getHeaders();
            List<String> headers = new ArrayList<>();
            if (rowNumbers)
                headers.add("#");
            if (headerIteratable != null)
                headerIteratable.iterator().forEachRemaining(headers::add);
            for (int i = headers.size(); i < cellWidths.size(); i++)
                headers.add("" + i);
            return headers;
        }

        private void updateCellWidthsForRows() {
            for (Iterable<String> row : rows) {
                int i = 0;
                for (String cell : row)
                    setCellWidth(i++, cellLength(cell));
            }
        }

        private void updateCellWidthsForHeaders() {
            int i = 0;
            for (String header : headers)
                setCellWidth(i++, cellLength(header));
        }

        private void setCellWidth(int index, int length) {
            cellWidths.put(index, max(cellWidths.getOrDefault(index, 0), length));
        }

        private int numberOfCellWidths() {
            return cellWidths.size();
        }

        private int getCellWidth(int index) {
            return cellWidths.get(index);
        }

        private List<Iterable<String>> prepareRows(TableProvider tableProvider) {
            List<Iterable<String>> rows = new ArrayList<>();
            if (tableProvider.getRows() != null) {
                int i = 0;
                for (Iterable<String> row : tableProvider.getRows()) {
                    if (i >= startRow && i <= endRow) {
                        if (rowNumbers)
                            rows.add(concat(Integer.toString(i), row));
                        else
                            rows.add(row);
                    }
                    i++;
                }
            }
            return rows;
        }

        private Iterable<String> concat(String s, Iterable<String> iterable) {
            return () -> new Iterator<String>() {
                private Iterator<String> iterator = iterable.iterator();
                private boolean firstElement = true;

                @Override
                public boolean hasNext() {
                    return firstElement ? true : iterator.hasNext();
                }

                @Override
                public String next() {
                    if (firstElement) {
                        firstElement = false;
                        return s;
                    }
                    return iterator.next();
                }
            };
        }

        private boolean exists() {
            return !(headers.isEmpty() && rows.isEmpty());
        }
    }

    /**
     * Prints the headers and data exposed by the specified TableProvider to the specified OutputStream.
     *
     * @param tableProvider that describes the table to print
     * @return String representation of the table
     */
    public String print(TableProvider tableProvider) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            print(tableProvider, baos);
            return baos.toString("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Failed to print table", e);
        }
    }

    /**
     * Prints the specified headers and data to a string.
     *
     * @param headers describes the table's columns. Should have the same number of columns as each of the data rows.
     * @param data describes the table's rows. Each element in the outer Iterable is a row and each element in the inner
     *             Iterable represents this row's columns.
     * @return String representation of the headers and data
     */
    public String print(Iterable<String> headers, Iterable<? extends Iterable<String>> data) {
        return print(new SimpleTableProvider(headers, data));
    }

    /**
     * Prints the headers and data exposed by the specified TableProvider to the specified OutputStream.
     *
     * @param tableProvider that describes the table to print
     * @param os to which the String representation of the table will be written
     */
    public void print(TableProvider tableProvider, OutputStream os) {
        new TableString().write(tableProvider, os);
    }

    /**
     * Prints the specified headers and data to the specified OutputStream.
     *
     * @param headers describes the table's columns. Should have the same number of columns as each of the data rows.
     * @param data describes the table's rows. Each element in the outer Iterable is a row and each element in the inner
     *             Iterable represents this row's columns.
     * @param os to which the String representation of the headers and data will be written
     */
    public void print(Iterable<String> headers, Iterable<? extends Iterable<String>> data, OutputStream os) {
        print(new SimpleTableProvider(headers, data), os);
    }
}
