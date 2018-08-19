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
package uk.gleissner.jutil.protobuf;

import com.google.protobuf.Descriptors.FieldDescriptor;
import org.junit.Test;
import org.slf4j.Logger;
import uk.gleissner.jutil.converter.ByteConverter;
import uk.gleissner.jutil.protobuf.TestProtos.Parent;
import uk.gleissner.jutil.protobuf.TestProtos.Parent.Child;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gleissner.jutil.converter.ByteConverter.toSpacedHex;
import static uk.gleissner.jutil.protobuf.ProtobufFieldPartitioner.partition;
import static uk.gleissner.jutil.protobuf.TestProtos.Parent.CHILDREN_FIELD_NUMBER;
import static uk.gleissner.jutil.protobuf.TestProtos.Parent.ID_FIELD_NUMBER;

public class ProtobufFieldPartitionerTest {

    private static final Logger logger = getLogger(ProtobufFieldPartitionerTest.class);
    private static FieldDescriptor childrenField = Parent.Builder.getDescriptor().findFieldByNumber(CHILDREN_FIELD_NUMBER);
    private static FieldDescriptor idField = Parent.Builder.getDescriptor().findFieldByNumber(ID_FIELD_NUMBER);

    @Test
    public void canPartition() {
        int parentId = 100;
        Parent parent = Parent.newBuilder().setId(parentId).addAllChildren(children(1,2,3,4,5)).build();
        int maxPartitionSizeInBytes = 12;

        Collection<Parent> parents = partition(parent, childrenField, maxPartitionSizeInBytes);

        assertThat(parents, is(newArrayList(parent(
                parentId, 1, 2),
                parent(parentId, 3, 4),
                parent(parentId,5))));

        parents.forEach(p -> {
            logger.debug("Parent: {}", toSpacedHex(p.toByteArray()));
            assertThat(p.getSerializedSize(), is(lessThanOrEqualTo(maxPartitionSizeInBytes)));
        });

        log(parent, parents);
    }

    @Test
    public void canPartitionMessagesOfVaryingSizes() {
        int parentId = 100;
        Parent parent = Parent.newBuilder().setId(parentId).addAllChildren(
                newArrayList(child(1, 10), child(2, 20),
                        child(3, 30), child(4, 40), child(5, 50))).build();
        int maxPartitionSizeInBytes = 100;

        Collection<Parent> parents = partition(parent, childrenField, maxPartitionSizeInBytes);

        assertThat(parents.size(), is(3));
        Iterator<Parent> iterator = parents.iterator();
        assertThat(iterator.next().getChildrenCount(), is(3));
        assertThat(iterator.next().getChildrenCount(), is(1));
        assertThat(iterator.next().getChildrenCount(), is(1));

        log(parent, parents);
    }

    @Test
    public void canPartitionWithTinyMaxMessageSize() {
        int parentId = 100;
        Parent parent = Parent.newBuilder().setId(parentId).addAllChildren(children(1,2)).build();
        int maxPartitionSizeInBytes = 1;

        Collection<Parent> parents = partition(parent, childrenField, maxPartitionSizeInBytes);

        assertThat(parents, is(newArrayList(parent(
                parentId, 1),
                parent(parentId, 2))));

        log(parent, parents);
    }

    @Test
    public void canPartitionWithHugeMaxMessageSize() {
        int parentId = 100;
        Parent parent = Parent.newBuilder().setId(parentId).addAllChildren(children(1,2)).build();
        int maxPartitionSizeInBytes = 1000;

        Collection<Parent> parents = partition(parent, childrenField, maxPartitionSizeInBytes);

        assertThat(parents, is(newArrayList(parent(parentId, 1, 2))));

        log(parent, parents);
    }

    @Test
    public void canPartitionEmptyField() {
        Parent parent = Parent.newBuilder().setId(100).build();
        Collection<Parent> parents = partition(parent, childrenField, 1000);
        assertThat(parents, is(newArrayList(parent(100))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfNotRepeatedField() {
        Parent parent = Parent.newBuilder().build();
        partition(parent, idField, 1L);
    }

    @Test(expected = NullPointerException.class)
    public void throwsExceptionIfMsgIsNull() {
        Parent parent = Parent.newBuilder().build();
        partition(null, childrenField, 1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfMaxSizeInBytesIsSmallerThanOne() {
        Parent parent = Parent.newBuilder().build();
        partition(parent, childrenField, 0L);
    }

    private Collection<Child> children(int... ids) {
        return IntStream.of(ids).mapToObj(id -> Child.newBuilder().setId(id).build()).collect(toList());
    }

    private Child child(int id, int nameLength) {
        return Child.newBuilder().setId(id).setName(repeat("a", nameLength)).build();
    }

    private Parent parent(int parentId, int... childrenIds) {
        Collection<Child> children = IntStream.of(childrenIds)
                .mapToObj(id -> Child.newBuilder().setId(id).build()).collect(toList());
        return Parent.newBuilder().setId(parentId).addAllChildren(children).build();
    }

    private void log(Parent source, Collection<Parent> targets) {
        logger.debug("\n\nOriginal parent:\n{}\n\nTarget parent(s):\n{}", toSpacedHex(source),
                targets.stream().map(ByteConverter::toSpacedHex).collect(joining("\n")));
    }
}