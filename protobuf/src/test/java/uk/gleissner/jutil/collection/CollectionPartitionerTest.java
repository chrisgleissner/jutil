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