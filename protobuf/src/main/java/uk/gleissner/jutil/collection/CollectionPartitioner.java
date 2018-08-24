/*
 * Copyright (C) 2018 Christian Gleissner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gleissner.jutil.collection;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Partitions a T collection into a collection of T collections whilst ensuring {@link Check}s are met.
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
            if (check.canBeAdded(partition, t))
                partition.add(t);
            else {
                partitions.add(partition);
                partition = new LinkedList<>();
                partition.add(t);
            }
        }
        if (!partition.isEmpty())
            partitions.add(partition);
        return partitions;
    }
}
