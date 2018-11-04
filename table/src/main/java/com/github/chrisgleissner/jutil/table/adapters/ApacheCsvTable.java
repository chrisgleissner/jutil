package com.github.chrisgleissner.jutil.table.adapters;

import com.github.chrisgleissner.jutil.table.Table;
import org.apache.commons.csv.CSVRecord;

import java.util.Collection;
import java.util.Set;

import static java.util.stream.StreamSupport.stream;

public class ApacheCsvTable implements Table {
    private final Iterable<CSVRecord> records;
    private Set<String> headers;

    public ApacheCsvTable(Iterable<CSVRecord> records) {
        this.records = records;
    }

    @Override
    public Iterable<String> getHeaders() {
        return headers;
    }

    @Override
    public Iterable<? extends Iterable<String>> getRows() {
        return (Iterable<Collection<String>>) () -> stream(records.spliterator(), false)
                .map(record -> {
                    if (headers == null)
                        headers = record.toMap().keySet();
                    return record.toMap().values();
                }).iterator();
    }
}
