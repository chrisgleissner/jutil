package uk.gleissner.jutil.spring.batch;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Collections.synchronizedList;

public class CacheItemWriter<T> implements ItemWriter<T> {

    private List<T> items = synchronizedList(newLinkedList());

    @Override
    public void write(List<? extends T> items) throws Exception {
        this.items.addAll(items);
    }

    public List<T> getItems() {
        return items;
    }
}
