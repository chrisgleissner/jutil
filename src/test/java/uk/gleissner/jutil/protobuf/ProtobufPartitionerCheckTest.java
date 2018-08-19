package uk.gleissner.jutil.protobuf;

import org.junit.Test;
import org.slf4j.Logger;
import uk.gleissner.jutil.protobuf.TestProtos.Parent;

import java.util.Collection;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gleissner.jutil.collection.CollectionPartitioner.partition;
import static uk.gleissner.jutil.converter.ByteConverter.toHex;
import static uk.gleissner.jutil.protobuf.TestProtos.Parent.CHILDREN_FIELD_NUMBER;
import static uk.gleissner.jutil.protobuf.TestProtos.Parent.Child;

public class ProtobufPartitionerCheckTest {

    private static final Logger logger = getLogger(ProtobufPartitionerCheckTest.class);

    @Test
    public void canPartition() {
        Parent containingNode = Parent.newBuilder().setId(100).build();
        int maxPartitionSizeInBytes = 10;
        Collection<Collection<Child>> partitions = partition(children(1, 2, 3, 4, 5),
                new ProtobufPartitionerCheck<>(
                        containingNode,
                        Parent.getDescriptor().findFieldByNumber(CHILDREN_FIELD_NUMBER),
                        maxPartitionSizeInBytes));

        assertThat(partitions, is(newArrayList(children(1, 2), children(3, 4), children(5))));
        Collection<Parent> parent = partitions.stream().map(children
                -> Parent.newBuilder(containingNode).addAllChildren(children).build()).collect(toList());

        logger.debug("Partitioned nodes: {}", parent);
        parent.stream().forEach(n -> {
            logger.debug("Node: {}", toHex(n.toByteArray()));
            assertThat(n.getSerializedSize(), is(lessThanOrEqualTo(maxPartitionSizeInBytes)));
        });
    }

    private Collection<Child> children(int... ids) {
        return IntStream.of(ids).mapToObj(id -> Child.newBuilder().setId(id).build()).collect(toList());
    }
}