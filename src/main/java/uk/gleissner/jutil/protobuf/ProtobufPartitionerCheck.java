package uk.gleissner.jutil.protobuf;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import uk.gleissner.jutil.collection.CollectionPartitioner;

import java.util.Collection;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Partitions Protobuf messages for setting them in a containing message to ensure the overall serialized
 * size of each containing message doesn't exceed a threshold.
 */
public class ProtobufPartitionerCheck<T extends Message> implements CollectionPartitioner.Check<T> {

    private static final Logger logger = getLogger(ProtobufPartitionerCheck.class);
    private static final int BITS_FOR_FIELD_TYPE_ENCODING = 3;

    private Message containingMessage;
    private final int indexOfPartitionedMessage;
    private final int maxContainingMessageSizeInBytes;

    public ProtobufPartitionerCheck(Message containingMessage,
                                    int partitionFieldIndexInContainingMessage,
                                    int maxEncodedPartitionSizeInBytes) {
        this.containingMessage = containingMessage;
        this.indexOfPartitionedMessage = partitionFieldIndexInContainingMessage;
        this.maxContainingMessageSizeInBytes = maxEncodedPartitionSizeInBytes;
    }

    @Override
    public boolean canBeAdded(Collection<T> partition, T toAdd) {
        long containingMessageOverhead = containingMessage.getSerializedSize();
        long partitionOverhead = bytesToEncodeUnsignedVarInt(partition.size(), 0)
                + bytesToEncodeUnsignedVarInt(indexOfPartitionedMessage, BITS_FOR_FIELD_TYPE_ENCODING);
        long bytes = partition.stream().mapToLong(m -> m.getSerializedSize()).sum() + toAdd.getSerializedSize();
        boolean canBeAdded = containingMessageOverhead + partitionOverhead + bytes <= maxContainingMessageSizeInBytes;
        logger.debug("canBeAdded(partition.size={}, toAdd={}) containingMessageOverhead={}, partitionOverhead={}, " +
                        "bytes={}, canBeAdded={}",
                partition.size(), toAdd, containingMessageOverhead, partitionOverhead, bytes, canBeAdded);
        return canBeAdded;
    }

    private int bytesToEncodeUnsignedVarInt(int toEncode, int unavailableBits) {
        int n = 0;
        while (toEncode >> n > 0) {
            n++;
        }
        return (n - unavailableBits) / 7 + 1;
    }
}
