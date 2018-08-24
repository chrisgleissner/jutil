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

import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FieldPartitionerTest {

    @Test
    public void partition() {
        FieldPartitioner.ObjectBuilder<List<Integer>> b = LinkedList::new;
        FieldPartitioner.FieldAdder<List<Integer>, Integer> a = (list, i) -> {
            if (list.size() < 3) {
                list.add(i);
                return Optional.of(list);
            } else {
                return empty();
            }
        };
        Collection<List<Integer>> lists = FieldPartitioner.partition(b, a, list(1, 2, 3, 4, 5, 6, 7));
        assertThat(lists, is(newArrayList(list(1, 2, 3), list(4, 5, 6), list(7))));

    }

    private List<Integer> list(int... i) {
        return IntStream.of(i).boxed().collect(toList());
    }
}