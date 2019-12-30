package com.github.chrisgleissner.jutil.jdbi;

import com.github.chrisgleissner.jutil.jdbi.mapping.ColumnNameMapping;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RenamingRowMapper<T> implements RowMapper<T> {
    private final RowMapper<T> underlying;
    private final UnaryOperator<ResultSet> resultSetTransformer;

    public static <T> RenamingRowMapper<T> mapColNames(RowMapper<T> underlying, ColumnNameMapping columnNameMapping) {
        return new RenamingRowMapper<>(underlying, rs -> new RenamingResultSet(rs, columnNameMapping));
    }

    @Override
    public T map(ResultSet rs, StatementContext ctx) throws SQLException {
        return underlying.map(rs, ctx);
    }

    @Override
    public RowMapper<T> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
        return underlying.specialize(resultSetTransformer.apply(rs), ctx);
    }
}
