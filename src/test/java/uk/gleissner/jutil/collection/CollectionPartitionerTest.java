package uk.gleissner.jutil.collection;

import org.junit.Test;

import java.util.Collection;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gleissner.jutil.collection.CollectionPartitioner.partition;

public class CollectionPartitionerTest {

    @Test
    public void canPartition() {
        assertThat(partition(list(1, 2, 3, 4, 5), (p, a) -> p.size() < 2),
                is(newArrayList(list(1, 2), list(3, 4), list(5))));
    }

    @Test
    public void canPartitionEmptyList() {
        assertThat(partition(list(), (p, a) -> p.size() < 2),
                is(newArrayList(list())));
    }

    private Collection<Integer> list(int... is) {
        return IntStream.of(is).mapToObj(Integer::new).collect(toList());
    }
}