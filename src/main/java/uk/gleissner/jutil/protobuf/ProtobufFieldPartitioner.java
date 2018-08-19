package uk.gleissner.jutil.protobuf;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import uk.gleissner.jutil.collection.FieldPartitioner;

import java.util.Collection;
import java.util.Optional;

/**
 * Splits a large Protobuf message into multiple messages by equally distributing the contents of one of its
 * fields across newly created instances. All fields other than the distributed field remain as per the original Protobuf
 * message.
 *
 * <p>One use case of this class is for sending a Protobuf message across a network where hard size
 * limits per message are in place, for example when using Azure Service Bus and its 256KB size limit.</p>
 */
public class ProtobufFieldPartitioner {

    public static <M extends Message, N> Collection<M> partitition(M msg, Descriptors.FieldDescriptor field,
                                                                    long maxMsgSizeInBytes) {
        FieldPartitioner.ObjectBuilder<M> objectBuilder = () -> (M) msg.toBuilder().clearField(field).build();
        FieldPartitioner.FieldAdder<M, N> fieldAdder = (m, n) -> {
            Optional<M> optionalMessageWithField = Optional.empty();
            M msgWithField = (M) m.toBuilder().addRepeatedField(field, n).build();
            if (msgWithField.getSerializedSize() <= maxMsgSizeInBytes || msgWithField.getRepeatedFieldCount(field) == 1) {
                optionalMessageWithField = Optional.of(msgWithField);
            }
            return optionalMessageWithField;
        };
        Collection<N> fieldValues = (Collection<N>) msg.getField(field);
        return FieldPartitioner.partition(objectBuilder, fieldAdder, fieldValues);
    }
}
