package uk.gleissner.jutil.collection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Partitions F(ield) instances across newly created O(bject) instances.
 */
public class FieldPartitioner {

    @FunctionalInterface
    public interface ObjectBuilder<O> {
        O build();
    }

    @FunctionalInterface
    public interface FieldAdder<O, F> {
        Optional<O> tryToAdd(O o, F f);
    }

    public static <O, F> Collection<O> partition(ObjectBuilder<O> objectBuilder,
                                                 FieldAdder<O, F> fieldAdder,
                                                 Collection<F> fields) {
        Collection<O> os = new LinkedList<>();
        O o = objectBuilder.build();
        Optional<O> addResult = empty();

        for (F f : fields) {
            addResult = fieldAdder.tryToAdd(o, f);
            if (!addResult.isPresent()) {
                os.add(o);
                o = objectBuilder.build();
                addResult = fieldAdder.tryToAdd(o, f);
            }
            o = addResult.get();
        }

        if (addResult.isPresent()) {
            os.add(o);
        }

        return os;
    }
}
