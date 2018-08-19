package uk.gleissner.jutil.collection;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Partitions a collection into a collection of collections.
 */
public class CollectionPartitioner {

    @FunctionalInterface
    public interface Check<T> {
        boolean canBeAdded(Collection<T> partition, T toAdd);
    }

    public static <T> Collection<Collection<T>> partition(Collection<T> ts, Check<T> check) {
        Collection<Collection<T>> partitions = new LinkedList<>();
        Collection<T> partition = new LinkedList<>();
        for (T t : ts) {
            if (check.canBeAdded(partition, t)) {
                partition.add(t);
            } else {
                partitions.add(partition);
                partition = new LinkedList<>();
                partition.add(t);
            }
        }
        if (!partition.isEmpty()) {
            partitions.add(partition);
        }
        return partitions;
    }
}
