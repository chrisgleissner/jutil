package com.github.chrisgleissner.jutil.jdbi;

import com.github.chrisgleissner.jutil.jdbi.mapping.ColumnNameMapping;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;

import java.lang.reflect.Type;
import java.util.Optional;

@RequiredArgsConstructor
public class RenamingRowMapperFactory<T> implements RowMapperFactory {
    private final RowMapperFactory underlying;
    private final ColumnNameMapping columnNameMapping;

    public static <T> RenamingRowMapperFactory<T> mapColNames(RowMapperFactory underlying, ColumnNameMapping columnNameMapping) {
        return new RenamingRowMapperFactory<>(underlying, columnNameMapping);
    }

    @Override
    public Optional<RowMapper<?>> build(Type type, ConfigRegistry config) {
        final RowMapper<?> rowMapper = underlying.build(type, config)
                .orElseThrow(() -> new RuntimeException("Could not create col mapping result transformer"));
        return Optional.of(RenamingRowMapper.mapColNames(rowMapper, columnNameMapping));
    }
}
