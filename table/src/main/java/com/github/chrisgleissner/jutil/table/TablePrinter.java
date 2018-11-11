package com.github.chrisgleissner.jutil.table;

import com.github.chrisgleissner.jutil.table.format.AsciiTableFormat;
import com.github.chrisgleissner.jutil.table.format.TableFormat;
import com.github.chrisgleissner.jutil.table.provider.SimpleTableProvider;
import com.github.chrisgleissner.jutil.table.provider.TableProvider;
import lombok.Builder;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

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
    private boolean wraparound = true;
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

        private void printRow(List<TableData.Cell> row) {
            print(tableFormat.getVerticalBorderFill(true), tableFormat.getVerticalBorderFill(false),
                    tableFormat.getVerticalBorderFill(true), row);
        }

        private void printHeaders(List<TableData.Cell> headers) {
            print(tableFormat.getVerticalBorderFill(true), tableFormat.getVerticalBorderFill(false),
                    tableFormat.getVerticalBorderFill(true), headers);
        }

        private void print(char left, char divider, char right, List<TableData.Cell> cells) {
            int maxLines = cells.stream().mapToInt(c -> c.getLines().size()).max().orElse(0);
            for (int lineIndex = 0; lineIndex < maxLines; lineIndex++) {
                pw.append(left);
                for (int cellIndex = 0; cellIndex < tableData.numberOfCellWidths(); cellIndex++) {
                    if (cellIndex > 0)
                        pw.append(divider);
                    pw.append(' ');

                    String line = "";
                    if (cellIndex < cells.size())
                        line = cells.get(cellIndex).getLine(lineIndex);

                    pw.append(line, 0, min(maxCellWidth, line.length()));

                    for (int k = 0; k < tableData.getCellWidth(cellIndex) - cellLength(line); k++)
                        pw.append(' ');

                    pw.append(' ');
                }
                pw.append(right);
                pw.append('\n');
            }
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

        @Data
        private class Cell {
            private List<String> lines = new ArrayList<>();

            public Cell(String s) {
                if (s == null)
                    s = nullValue;
                s = s.replace("\t", "        ");
                if (wraparound) {
                    StringBuilder sb = new StringBuilder();
                    for (char c : s.toCharArray()) {
                        if (c == '\n' || sb.length() == maxCellWidth) {
                            lines.add(sb.toString());
                            sb = new StringBuilder();
                        }
                        if (c != '\n')
                            sb.append(c);
                    }
                    if (sb.length() > 0)
                        lines.add(sb.toString());
                }
                else {
                    s = s.replace("\n", "");
                    lines.add(s.substring(0, Math.min(maxCellWidth, s.length())));
                }
            }

            public String getLine(int i) {
                return i < lines.size() ? lines.get(i) : "";
            }
        }

        private final Map<Integer, Integer> cellWidths = new TreeMap<>();
        private final List<Cell> headers;
        private final List<List<Cell>> rows;

        private TableData(TableProvider tableProvider) {
            rows = prepareRows(tableProvider);
            updateCellWidthsForRows();

            headers = prepareHeaders(tableProvider);
            updateCellWidthsForHeaders();
        }

        private List<Cell> prepareHeaders(TableProvider tableProvider) {
            Iterable<String> headerIteratable = tableProvider.getHeaders();
            List<Cell> headers = new ArrayList<>();
            if (rowNumbers)
                headers.add(new Cell("#"));
            if (headerIteratable != null)
                headerIteratable.iterator().forEachRemaining(h -> headers.add(new Cell(h)));
            for (int i = headers.size(); i < cellWidths.size(); i++)
                headers.add(new Cell("" + i));
            return headers;
        }

        private void updateCellWidthsForRows() {
            for (Iterable<Cell> row : rows) {
                int i = 0;
                for (Cell cell : row)
                    setCellWidth(i++, cell.lines.stream().mapToInt(l -> cellLength(l)).max().orElse(0));
            }
        }

        private void updateCellWidthsForHeaders() {
            int i = 0;
            for (Cell cell : headers)
                setCellWidth(i++, cell.lines.stream().mapToInt(l -> cellLength(l)).max().orElse(0));
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

        private List<List<Cell>> prepareRows(TableProvider tableProvider) {
            List<List<Cell>> rows = new ArrayList<>();
            if (tableProvider.getRows() != null) {
                int i = 0;
                for (Iterable<String> row : tableProvider.getRows()) {
                    if (i >= startRow && i <= endRow) {
                        List<Cell> cells = new ArrayList<>();
                        cells.addAll(StreamSupport.stream(row.spliterator(), true).map(Cell::new).collect(toList()));
                        if (rowNumbers)
                            cells.add(0, new Cell(Integer.toString(i)));
                        rows.add(cells);
                    }
                    i++;
                }
            }
            return rows;
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
     * @param data    describes the table's rows. Each element in the outer Iterable is a row and each element in the inner
     *                Iterable represents this row's columns.
     * @return String representation of the headers and data
     */
    public String print(Iterable<String> headers, Iterable<? extends Iterable<String>> data) {
        return print(new SimpleTableProvider(headers, data));
    }

    /**
     * Prints the headers and data exposed by the specified TableProvider to the specified OutputStream.
     *
     * @param tableProvider that describes the table to print
     * @param os            to which the String representation of the table will be written
     */
    public void print(TableProvider tableProvider, OutputStream os) {
        new TableString().write(tableProvider, os);
    }

    /**
     * Prints the specified headers and data to the specified OutputStream.
     *
     * @param headers describes the table's columns. Should have the same number of columns as each of the data rows.
     * @param data    describes the table's rows. Each element in the outer Iterable is a row and each element in the inner
     *                Iterable represents this row's columns.
     * @param os      to which the String representation of the headers and data will be written
     */
    public void print(Iterable<String> headers, Iterable<? extends Iterable<String>> data, OutputStream os) {
        print(new SimpleTableProvider(headers, data), os);
    }
}
