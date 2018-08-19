package uk.gleissner.jutil.protobuf;

import com.google.protobuf.Descriptors.FieldDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import uk.gleissner.jutil.converter.ByteConverter;
import uk.gleissner.jutil.protobuf.TestProtos.Parent;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gleissner.jutil.converter.ByteConverter.toHex;
import static uk.gleissner.jutil.protobuf.ProtobufFieldPartitioner.partitition;
import static uk.gleissner.jutil.protobuf.TestProtos.Parent.CHILDREN_FIELD_NUMBER;

public class ProtobufFieldPartitionerTest {

    private static final Logger logger = getLogger(ProtobufFieldPartitionerTest.class);
    private static FieldDescriptor childrenField = Parent.Builder.getDescriptor().findFieldByNumber(CHILDREN_FIELD_NUMBER);

    @Test
    public void canPartition() {
        int parentId = 100;
        Parent parent = Parent.newBuilder().setId(parentId).addAllChildren(children(1,2,3,4,5)).build();
        int maxPartitionSizeInBytes = 12;

        Collection<Parent> parents = partitition(parent, childrenField, maxPartitionSizeInBytes);

        assertThat(parents, is(newArrayList(parent(
                parentId, 1, 2),
                parent(parentId, 3, 4),
                parent(parentId,5))));

        parents.stream().forEach(p -> {
            logger.debug("Parent: {}", toHex(p.toByteArray()));
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

        Collection<Parent> parents = partitition(parent, childrenField, maxPartitionSizeInBytes);

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

        Collection<Parent> parents = partitition(parent, childrenField, maxPartitionSizeInBytes);

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

        Collection<Parent> parents = partitition(parent, childrenField, maxPartitionSizeInBytes);

        assertThat(parents, is(newArrayList(parent(parentId, 1, 2))));

        log(parent, parents);
    }

    private Collection<Parent.Child> children(int... ids) {
        return IntStream.of(ids).mapToObj(id -> Parent.Child.newBuilder().setId(id).build()).collect(toList());
    }

    private Parent.Child child(int id, int nameLength) {
        return Parent.Child.newBuilder().setId(id).setName(StringUtils.repeat("a", nameLength)).build();
    }

    private Parent parent(int parentId, int... childrenIds) {
        Collection<Parent.Child> children = IntStream.of(childrenIds).mapToObj(id -> Parent.Child.newBuilder().setId(id).build()).collect(toList());
        return Parent.newBuilder().setId(parentId).addAllChildren(children).build();
    }

    private void log(Parent source, Collection<Parent> targets) {
        logger.debug("\n\nOriginal parent:\n{}\n\nTarget parent(s):\n{}", toHex(source),
                targets.stream().map(ByteConverter::toHex).collect(joining("\n")));
    }
}