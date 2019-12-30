package com.github.chrisgleissner.jutil.jdbi;

import com.github.chrisgleissner.jutil.jdbi.mapping.ColumnNameMapping;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class RenamingResultSetMetaData implements ResultSetMetaData {
    private final ResultSetMetaData underlying;
    private final ColumnNameMapping columnNameMapping;

    private String mapped(String columnName) {
        return columnNameMapping.apply(columnName);
    }

    @Override
    public int getColumnCount() throws SQLException {
        return underlying.getColumnCount();
    }

    @Override
    public boolean isAutoIncrement(int i) throws SQLException {
        return underlying.isAutoIncrement(i);
    }

    @Override
    public boolean isCaseSensitive(int i) throws SQLException {
        return underlying.isCaseSensitive(i);
    }

    @Override
    public boolean isSearchable(int i) throws SQLException {
        return underlying.isSearchable(i);
    }

    @Override
    public boolean isCurrency(int i) throws SQLException {
        return underlying.isCurrency(i);
    }

    @Override
    public int isNullable(int i) throws SQLException {
        return underlying.isNullable(i);
    }

    @Override
    public boolean isSigned(int i) throws SQLException {
        return underlying.isSigned(i);
    }

    @Override
    public int getColumnDisplaySize(int i) throws SQLException {
        return underlying.getColumnDisplaySize(i);
    }

    @Override
    public String getColumnLabel(int i) throws SQLException {
        return mapped(underlying.getColumnLabel(i));
    }

    @Override
    public String getColumnName(int i) throws SQLException {
        return mapped(underlying.getColumnName(i));
    }

    @Override
    public String getSchemaName(int i) throws SQLException {
        return mapped(underlying.getSchemaName(i));
    }

    @Override
    public int getPrecision(int i) throws SQLException {
        return underlying.getPrecision(i);
    }

    @Override
    public int getScale(int i) throws SQLException {
        return underlying.getScale(i);
    }

    @Override
    public String getTableName(int i) throws SQLException {
        return mapped(underlying.getTableName(i));
    }

    @Override
    public String getCatalogName(int i) throws SQLException {
        return mapped(underlying.getCatalogName(i));
    }

    @Override
    public int getColumnType(int i) throws SQLException {
        return underlying.getColumnType(i);
    }

    @Override
    public String getColumnTypeName(int i) throws SQLException {
        return underlying.getColumnTypeName(i);
    }

    @Override
    public boolean isReadOnly(int i) throws SQLException {
        return underlying.isReadOnly(i);
    }

    @Override
    public boolean isWritable(int i) throws SQLException {
        return underlying.isWritable(i);
    }

    @Override
    public boolean isDefinitelyWritable(int i) throws SQLException {
        return underlying.isDefinitelyWritable(i);
    }

    @Override
    public String getColumnClassName(int i) throws SQLException {
        return underlying.getColumnClassName(i);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) throws SQLException {
        return underlying.unwrap(aClass);
    }

    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return underlying.isWrapperFor(aClass);
    }
}
