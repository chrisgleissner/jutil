package com.github.chrisgleissner.jutil.table.provider;

import com.univocity.parsers.common.IterableResult;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.record.Record;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;

public class UnivocityTableProvider {

    public static TableProvider of(IterableResult<Record, ParsingContext> records) {
        return new IterableTableProvider(records);
    }

    public static TableProvider of(Iterable<Record> records) {
        return new ListTableProvider(records);
    }

    private static class IterableTableProvider implements TableProvider {

        private final IterableResult<Record, ParsingContext> records;

        private IterableTableProvider(IterableResult<Record, ParsingContext> records) {
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

    private static class ListTableProvider implements TableProvider {

        private final Iterable<Record> records;

        private ListTableProvider(Iterable<Record> records) {
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
