package com.github.chrisgleissner.jutil.table.provider;

public interface TableProvider {
    Iterable<String> getHeaders();

    Iterable<? extends Iterable<String>> getRows();
}
