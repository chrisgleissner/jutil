package com.github.chrisgleissner.jutil.table;

public interface Table {
    Iterable<String> getHeaders();

    Iterable<? extends Iterable<String>> getRows();
}
