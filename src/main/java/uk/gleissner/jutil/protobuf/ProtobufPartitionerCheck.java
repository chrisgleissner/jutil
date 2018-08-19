package uk.gleissner.jutil.protobuf;

import com.google.common.collect.Lists;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import uk.gleissner.jutil.collection.CollectionPartitioner;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Partitions Protobuf messages for setting them in a containing message to ensure the overall serialized
 * size of each containing message doesn't exceed a threshold.
 */
public class ProtobufPartitionerCheck<T extends Message> implements CollectionPartitioner.Check<T> {

    private static final Logger logger = getLogger(ProtobufPartitionerCheck.class);
    private static final int BITS_FOR_FIELD_TYPE_ENCODING = 3;

    private Message containingMessage;
    private final Descriptors.FieldDescriptor fieldDescriptorInContainingMessage;
    private final int maxContainingMessageSizeInBytes;

    public ProtobufPartitionerCheck(Message containingMessage,
                                    Descriptors.FieldDescriptor fieldDescriptorInContainingMessage,
                                    int maxEncodedPartitionSizeInBytes) {
        this.containingMessage = containingMessage;
        this.fieldDescriptorInContainingMessage = fieldDescriptorInContainingMessage;
        this.maxContainingMessageSizeInBytes = maxEncodedPartitionSizeInBytes;
    }

    @Override
    public boolean canBeAdded(Collection<T> partition, T toAdd) {
        long containingMessageOverhead = containingMessage.getSerializedSize();
        long partitionOverhead = bytesToEncodeUnsignedVarInt(partition.size(), 0)
                + bytesToEncodeUnsignedVarInt(fieldDescriptorInContainingMessage.getIndex(), BITS_FOR_FIELD_TYPE_ENCODING);
        long bytes = partition.stream().mapToLong(m -> serializedSizeIncludingOverhead(m)).sum() + serializedSizeIncludingOverhead(toAdd);
        boolean canBeAdded = containingMessageOverhead + partitionOverhead + bytes <= maxContainingMessageSizeInBytes;

        if (logger.isDebugEnabled()) {
            logger.debug("canBeAdded: partition.size={}, toAdd={}, containingMessageOverhead={}, partitionOverhead={}, " +
                            "bytes={}, canBeAdded={}",
                    partition.size(), toAdd, containingMessageOverhead, partitionOverhead, bytes, canBeAdded);
            List<T> newPartition = newArrayList(partition);
            if (canBeAdded)
                newPartition.add(toAdd);
            logger.debug("New message after add: {}\n", ProtobufUtil.toHex(containingMessage.toBuilder()
                    .setField(fieldDescriptorInContainingMessage, newPartition).build()));

        }
        return canBeAdded;
    }

    private int serializedSizeIncludingOverhead(Message m) {
        return bytesToEncodeUnsignedVarInt(m.getSerializedSize(), BITS_FOR_FIELD_TYPE_ENCODING) + m.getSerializedSize();
    }

    private int bytesToEncodeUnsignedVarInt(int toEncode, int unavailableBits) {
        int n = 0;
        while (toEncode >> n > 0) {
            n++;
        }
        return (n - unavailableBits) / 7 + 1;
    }
}
