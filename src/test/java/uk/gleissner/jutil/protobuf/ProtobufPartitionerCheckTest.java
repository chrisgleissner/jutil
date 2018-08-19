package uk.gleissner.jutil.protobuf;

import org.junit.Test;
import org.slf4j.Logger;
import uk.gleissner.jutil.protobuf.NodeProtos.Node;

import java.util.Collection;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static uk.gleissner.jutil.collection.CollectionPartitioner.partition;
import static uk.gleissner.jutil.protobuf.NodeProtos.Node.CHILDREN_FIELD_NUMBER;

public class ProtobufPartitionerCheckTest {

    private static final Logger logger = getLogger(ProtobufPartitionerCheckTest.class);

    @Test
    public void canPartition() {
        Node.Builder containingNodeBuilder = Node.newBuilder().setId(100);
        int maxPartitionSizeInBytes = 10;
        Collection<Collection<Node>> partitions = partition(nodes(1, 2, 3, 4, 5),
                new ProtobufPartitionerCheck<>(containingNodeBuilder.build(), CHILDREN_FIELD_NUMBER, maxPartitionSizeInBytes));

        assertThat(partitions, is(newArrayList(nodes(1, 2, 3), nodes(4, 5))));
        Collection<Node> nodesWithChildren = partitions.stream().map(children
                -> containingNodeBuilder.addAllChildren(children).build()).collect(toList());

        logger.debug("Partitioned nodes: {}", nodesWithChildren);
        nodesWithChildren.stream().forEach(n -> assertThat(n.getSerializedSize(), is(lessThanOrEqualTo(maxPartitionSizeInBytes))));
    }

    private Collection<Node> nodes(int... ids) {
        return IntStream.of(ids).mapToObj(id -> Node.newBuilder().setId(id).build()).collect(toList());
    }
}