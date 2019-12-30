package com.github.chrisgleissner.jutil.jdbi;

import com.github.chrisgleissner.jutil.jdbi.mapping.ColumnNameMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class RenamingResultSet implements ResultSet {
    private final ResultSet underlying;
    private final ColumnNameMapping columnNameMapping;

    private String mapped(String columnName) {
        return columnNameMapping.apply(columnName);
    }

    @Override
    public boolean next() throws SQLException {
        return underlying.next();
    }

    @Override
    public void close() throws SQLException {
        underlying.close();
    }

    @Override
    public boolean wasNull() throws SQLException {
        return underlying.wasNull();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        return underlying.getString(columnIndex);
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        return underlying.getBoolean(columnIndex);
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        return underlying.getByte(columnIndex);
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        return underlying.getShort(columnIndex);
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        return underlying.getInt(columnIndex);
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        return underlying.getLong(columnIndex);
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        return underlying.getFloat(columnIndex);
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        return underlying.getDouble(columnIndex);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return underlying.getBigDecimal(columnIndex, scale);
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        return underlying.getBytes(columnIndex);
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        return underlying.getDate(columnIndex);
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        return underlying.getTime(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return underlying.getTimestamp(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return underlying.getAsciiStream(columnIndex);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return underlying.getUnicodeStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return underlying.getBinaryStream(columnIndex);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        return underlying.getString(mapped(columnLabel));
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return underlying.getBoolean(mapped(columnLabel));
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        return underlying.getByte(mapped(columnLabel));
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        return underlying.getShort(mapped(columnLabel));
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        return underlying.getInt(mapped(columnLabel));
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return underlying.getLong(mapped(columnLabel));
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        return underlying.getFloat(mapped(columnLabel));
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        return underlying.getDouble(mapped(columnLabel));
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        return underlying.getBigDecimal(mapped(columnLabel), scale);
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        return underlying.getBytes(mapped(columnLabel));
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        return underlying.getDate(mapped(columnLabel));
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        return underlying.getTime(mapped(columnLabel));
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return underlying.getTimestamp(mapped(columnLabel));
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return underlying.getAsciiStream(mapped(columnLabel));
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return underlying.getUnicodeStream(mapped(columnLabel));
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return underlying.getBinaryStream(mapped(columnLabel));
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return underlying.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        underlying.clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return underlying.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return new RenamingResultSetMetaData(underlying.getMetaData(), columnNameMapping);
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        return underlying.getObject(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return underlying.getObject(mapped(columnLabel));
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return underlying.findColumn(mapped(columnLabel));
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return underlying.getCharacterStream(columnIndex);
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return underlying.getCharacterStream(mapped(columnLabel));
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return underlying.getBigDecimal(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return underlying.getBigDecimal(mapped(columnLabel));
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return underlying.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return underlying.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return underlying.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return underlying.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        underlying.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        underlying.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return underlying.first();
    }

    @Override
    public boolean last() throws SQLException {
        return underlying.last();
    }

    @Override
    public int getRow() throws SQLException {
        return underlying.getRow();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return underlying.absolute(row);
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return underlying.relative(rows);
    }

    @Override
    public boolean previous() throws SQLException {
        return underlying.previous();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        underlying.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return underlying.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        underlying.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return underlying.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return underlying.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return underlying.getConcurrency();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return underlying.rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return underlying.rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return underlying.rowDeleted();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        underlying.updateNull(columnIndex);
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        underlying.updateBoolean(columnIndex, x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        underlying.updateByte(columnIndex, x);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        underlying.updateShort(columnIndex, x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        underlying.updateInt(columnIndex, x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        underlying.updateLong(columnIndex, x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        underlying.updateFloat(columnIndex, x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        underlying.updateDouble(columnIndex, x);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        underlying.updateBigDecimal(columnIndex, x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        underlying.updateString(columnIndex, x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        underlying.updateBytes(columnIndex, x);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        underlying.updateDate(columnIndex, x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        underlying.updateTime(columnIndex, x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        underlying.updateTimestamp(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        underlying.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        underlying.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        underlying.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        underlying.updateObject(columnIndex, x, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        underlying.updateObject(columnIndex, x);
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        underlying.updateNull(mapped(columnLabel));
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        underlying.updateBoolean(mapped(columnLabel), x);
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        underlying.updateByte(mapped(columnLabel), x);
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        underlying.updateShort(mapped(columnLabel), x);
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        underlying.updateInt(mapped(columnLabel), x);
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        underlying.updateLong(mapped(columnLabel), x);
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        underlying.updateFloat(mapped(columnLabel), x);
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        underlying.updateDouble(mapped(columnLabel), x);
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        underlying.updateBigDecimal(mapped(columnLabel), x);
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        underlying.updateString(mapped(columnLabel), x);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        underlying.updateBytes(mapped(columnLabel), x);
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        underlying.updateDate(mapped(columnLabel), x);
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        underlying.updateTime(mapped(columnLabel), x);
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        underlying.updateTimestamp(mapped(columnLabel), x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        underlying.updateAsciiStream(mapped(columnLabel), x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        underlying.updateBinaryStream(mapped(columnLabel), x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        underlying.updateCharacterStream(mapped(columnLabel), reader, length);
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        underlying.updateObject(mapped(columnLabel), x, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        underlying.updateObject(mapped(columnLabel), x);
    }

    @Override
    public void insertRow() throws SQLException {
        underlying.insertRow();
    }

    @Override
    public void updateRow() throws SQLException {
        underlying.updateRow();
    }

    @Override
    public void deleteRow() throws SQLException {
        underlying.deleteRow();
    }

    @Override
    public void refreshRow() throws SQLException {
        underlying.refreshRow();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        underlying.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        underlying.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        underlying.moveToCurrentRow();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return underlying.getStatement();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return underlying.getObject(columnIndex, map);
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        return underlying.getRef(columnIndex);
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        return underlying.getBlob(columnIndex);
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        return underlying.getClob(columnIndex);
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        return underlying.getArray(columnIndex);
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        return underlying.getObject(mapped(columnLabel), map);
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        return underlying.getRef(mapped(columnLabel));
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        return underlying.getBlob(mapped(columnLabel));
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        return underlying.getClob(mapped(columnLabel));
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        return underlying.getArray(mapped(columnLabel));
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return underlying.getDate(columnIndex, cal);
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        return underlying.getDate(mapped(columnLabel), cal);
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return underlying.getTime(columnIndex, cal);
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        return underlying.getTime(mapped(columnLabel), cal);
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return underlying.getTimestamp(columnIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        return underlying.getTimestamp(mapped(columnLabel), cal);
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        return underlying.getURL(columnIndex);
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        return underlying.getURL(mapped(columnLabel));
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        underlying.updateRef(columnIndex, x);
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        underlying.updateRef(mapped(columnLabel), x);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        underlying.updateBlob(columnIndex, x);
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        underlying.updateBlob(mapped(columnLabel), x);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        underlying.updateClob(columnIndex, x);
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        underlying.updateClob(mapped(columnLabel), x);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        underlying.updateArray(columnIndex, x);
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        underlying.updateArray(mapped(columnLabel), x);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return underlying.getRowId(columnIndex);
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return underlying.getRowId(mapped(columnLabel));
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        underlying.updateRowId(columnIndex, x);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        underlying.updateRowId(mapped(columnLabel), x);
    }

    @Override
    public int getHoldability() throws SQLException {
        return underlying.getHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return underlying.isClosed();
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        underlying.updateNString(columnIndex, nString);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        underlying.updateNString(mapped(columnLabel), nString);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        underlying.updateNClob(columnIndex, nClob);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        underlying.updateNClob(mapped(columnLabel), nClob);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return underlying.getNClob(columnIndex);
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return underlying.getNClob(mapped(columnLabel));
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return underlying.getSQLXML(columnIndex);
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return underlying.getSQLXML(mapped(columnLabel));
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        underlying.updateSQLXML(columnIndex, xmlObject);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        underlying.updateSQLXML(mapped(columnLabel), xmlObject);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return underlying.getNString(columnIndex);
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return underlying.getNString(mapped(columnLabel));
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return underlying.getNCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return underlying.getNCharacterStream(mapped(columnLabel));
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        underlying.updateNCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        underlying.updateNCharacterStream(mapped(columnLabel), reader, length);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        underlying.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        underlying.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        underlying.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        underlying.updateAsciiStream(mapped(columnLabel), x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        underlying.updateBinaryStream(mapped(columnLabel), x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        underlying.updateCharacterStream(mapped(columnLabel), reader, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        underlying.updateBlob(columnIndex, inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        underlying.updateBlob(mapped(columnLabel), inputStream, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        underlying.updateClob(columnIndex, reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        underlying.updateClob(mapped(columnLabel), reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        underlying.updateNClob(columnIndex, reader, length);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        underlying.updateNClob(mapped(columnLabel), reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        underlying.updateNCharacterStream(columnIndex, x);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        underlying.updateNCharacterStream(mapped(columnLabel), reader);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        underlying.updateAsciiStream(columnIndex, x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        underlying.updateBinaryStream(columnIndex, x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        underlying.updateCharacterStream(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        underlying.updateAsciiStream(mapped(columnLabel), x);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        underlying.updateBinaryStream(mapped(columnLabel), x);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        underlying.updateCharacterStream(mapped(columnLabel), reader);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        underlying.updateBlob(columnIndex, inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        underlying.updateBlob(mapped(columnLabel), inputStream);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        underlying.updateClob(columnIndex, reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        underlying.updateClob(mapped(columnLabel), reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        underlying.updateNClob(columnIndex, reader);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        underlying.updateNClob(mapped(columnLabel), reader);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return underlying.getObject(columnIndex, type);
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return underlying.getObject(mapped(columnLabel), type);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        underlying.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        underlying.updateObject(mapped(columnLabel), x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        underlying.updateObject(columnIndex, x, targetSqlType);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        underlying.updateObject(mapped(columnLabel), x, targetSqlType);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return underlying.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return underlying.isWrapperFor(iface);
    }
}
