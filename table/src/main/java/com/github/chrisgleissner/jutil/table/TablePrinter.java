package com.github.chrisgleissner.jutil.table;

import com.github.chrisgleissner.jutil.table.format.AsciiTableFormat;
import com.github.chrisgleissner.jutil.table.format.TableFormat;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Pretty prints a table.
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
        private int[] cellWidths;
        private StringBuilder sb;

        public String toString(Table table) {
            List<Iterable<String>> rows = rows(table);
            Iterable<String> headers = table.getHeaders();
            if (rowNumbers)
                headers = concat("#", headers);

            sb = new StringBuilder(rows.size() * 128);
            cellWidths = cellWidths(headers, rows);

            printTopEdge();
            printHeaders(headers);
            printUnderHeaders(rows.isEmpty());

            if (!rows.isEmpty()) {
                for (int i = 0; i < rows.size(); i++) {
                    if (i > 0 && horizontalDividers)
                        printHorizontalDivider();
                    printRow(rows.get(i));
                }
                printBottomEdge();
            }

            return sb.toString();
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
            sb.append(left);
            int i = 0;
            for (String cell : cells) {
                if (i > 0)
                    sb.append(divider);
                sb.append(' ');
                cell = cell == null ? nullValue : cell;
                sb.append(cell, 0, min(maxCellWidth, cell.length()));
                for (int i1 = 0; i1 < cellWidths[i] - cellLength(cell); i1++)
                    sb.append(' ');
                sb.append(' ');
                i++;
            }
            sb.append(right);
            sb.append('\n');
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
            sb.append(left);
            for (int i = 0; i < cellWidths.length; i++) {
                if (i > 0)
                    sb.append(divider);
                for (int i1 = 0; i1 < cellWidths[i] + 2; i1++)
                    sb.append(middle);
            }
            sb.append(right);
            sb.append('\n');
        }

        private List<Iterable<String>> rows(Table table) {
            List<Iterable<String>> rows = new ArrayList<>();
            if (table.getRows() != null) {
                int i = 0;
                for (Iterable<String> row : table.getRows()) {
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

        private int[] cellWidths(Iterable<String> headers, List<Iterable<String>> rows) {
            int[] cellWidths = new int[size(headers)];
            int i = 0;
            for (String header : headers)
                cellWidths[i] = max(cellWidths[i++], cellLength(header));
            for (Iterable<String> row : rows) {
                i = 0;
                for (String cell : row)
                    cellWidths[i] = max(cellWidths[i++], cellLength(cell));
            }
            return cellWidths;
        }
    }

    private int size(Iterable<? extends Object> it) {
        int size = 0;
        for (Object o : it)
            size++;
        return size;
    }

    private int cellLength(String s) {
        return min(maxCellWidth, (s == null ? nullValue : s).length());
    }

    public String print(Table table) {
        return table.getHeaders() == null ? "" : new TableString().toString(table);
    }

    public String print(Iterable<String> headers, Iterable<? extends Iterable<String>> data) {
        return print(new SimpleTable(headers, data));
    }
}
