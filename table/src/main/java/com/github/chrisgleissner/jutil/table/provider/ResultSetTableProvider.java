package com.github.chrisgleissner.jutil.table.provider;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResultSetTableProvider implements TableProvider {
    private final ResultSet rs;
    private final int columnCount;

    public ResultSetTableProvider(ResultSet resultSet) {
        try {
            this.rs = resultSet;
            this.columnCount = rs.getMetaData().getColumnCount();
        } catch (Exception e) {
            throw new RuntimeException("Could not get column count from ResultSet", e);
        }

    }

    @Override
    public Iterable<String> getHeaders() {
        try {
            List<String> headers = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++)
                headers.add(rs.getMetaData().getColumnName(i));
            return headers;
        } catch (Exception e) {
            throw new RuntimeException("Could not get headers from ResultSet", e);
        }
    }

    @Override
    public Iterable<? extends Iterable<String>> getRows() {
        return (Iterable<Iterable<String>>) () -> new Iterator<Iterable<String>>() {
            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (Exception e) {
                    throw new RuntimeException("Can't get next row from ResultSet", e);
                }
            }

            @Override
            public Iterable<String> next() {
                try {
                    List<String> cells = new ArrayList<>(columnCount);
                    for (int i = 1; i <= columnCount; i++)
                        cells.add(rs.getString(i));
                    return cells;
                } catch (Exception e) {
                    throw new RuntimeException("Can't get next row from ResultSet", e);
                }
            }
        };
    }
}
