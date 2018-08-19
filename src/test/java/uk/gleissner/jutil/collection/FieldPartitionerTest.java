package uk.gleissner.jutil.collection;

import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FieldPartitionerTest {

    @Test
    public void partition() {
        FieldPartitioner.ObjectBuilder b = () -> new LinkedList<Integer>();
        FieldPartitioner.FieldAdder<List<Integer>, Integer> a = (list, i) -> {
            if (list.size() < 3) {
                list.add(i);
                return Optional.of(list);
            } else {
                return Optional.empty();
            }
        };
        Collection<List<Integer>> lists = FieldPartitioner.partition(b, a, list(1, 2, 3, 4, 5, 6, 7));
        assertThat(lists, is(newArrayList(list(1, 2, 3), list(4, 5, 6), list(7))));

    }

    private List<Integer> list(int... i) {
        return IntStream.of(i).mapToObj(Integer::new).collect(toList());
    }
}