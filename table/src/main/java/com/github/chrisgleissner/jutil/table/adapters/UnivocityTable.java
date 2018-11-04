package com.github.chrisgleissner.jutil.table.adapters;

import com.github.chrisgleissner.jutil.table.Table;
import com.univocity.parsers.common.IterableResult;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.record.Record;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;

public class UnivocityTable {

    public static Table of(IterableResult<Record, ParsingContext> records) {
        return new IterableTable(records);
    }

    public static Table of(Iterable<Record> records) {
        return new ListTable(records);
    }

    private static class IterableTable implements Table {

        private final IterableResult<Record, ParsingContext> records;

        private IterableTable(IterableResult<Record, ParsingContext> records) {
            this.records = records;
        }

        @Override
        public Iterable<String> getHeaders() {
            return asList(records.iterator().getContext().headers());
        }

        @Override
        public Iterable<? extends Iterable<String>> getRows() {
            return (Iterable<List<String>>) () -> stream(records.spliterator(), false).map(record -> asList(record.getValues())).iterator();
        }
    }

    private static class ListTable implements Table {

        private final Iterable<Record> records;

        private ListTable(Iterable<Record> records) {
            this.records = records;
        }

        @Override
        public Iterable<String> getHeaders() {
            return asList(records.iterator().next().getMetaData().headers());
        }

        @Override
        public Iterable<? extends Iterable<String>> getRows() {
            return (Iterable<List<String>>) () -> stream(records.spliterator(), false).map(record -> asList(record.getValues())).iterator();
        }
    }
}
