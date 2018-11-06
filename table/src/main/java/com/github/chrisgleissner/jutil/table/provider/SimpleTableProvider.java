package com.github.chrisgleissner.jutil.table.provider;

import lombok.Data;

@Data
public class SimpleTableProvider implements TableProvider {
    private final Iterable<String> headers;
    private final Iterable<? extends Iterable<String>> rows;
}
