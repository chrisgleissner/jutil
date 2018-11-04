package com.github.chrisgleissner.jutil.table.adapters;

import com.github.chrisgleissner.jutil.table.Table;
import lombok.Data;

@Data
public class SimpleTable implements Table {
    private final Iterable<String> headers;
    private final Iterable<? extends Iterable<String>> rows;
}
