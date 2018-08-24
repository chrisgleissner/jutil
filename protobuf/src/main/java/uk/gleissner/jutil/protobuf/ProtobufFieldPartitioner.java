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
import com.google.protobuf.Message;
import uk.gleissner.jutil.collection.FieldPartitioner;
import uk.gleissner.jutil.collection.FieldPartitioner.FieldAdder;
import uk.gleissner.jutil.collection.FieldPartitioner.ObjectBuilder;

import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Optional.empty;

/**
 * Splits a large Protobuf message into multiple messages by equally distributing the contents of one of its
 * fields across newly created instances. All fields other than the distributed field remain as per the original Protobuf
 * message.
 *
 * <p>One use case of this class is for sending a Protobuf message across a network where hard size
 * limits per message are in place, for example when using Azure Service Bus and its 256KB size limit.</p>
 */
public class ProtobufFieldPartitioner {

    @SuppressWarnings("unchecked")
    public static <M extends Message> Collection<M> partition(M msg, FieldDescriptor repeatedField, long maxMsgSizeInBytes) {
        checkNotNull(msg, "msg");
        checkArgument(repeatedField.isRepeated(), "repeatedField needs to be repeated but was %s", repeatedField.getType());
        checkArgument(maxMsgSizeInBytes > 0, "maxMsgSizeInBytes");

        ObjectBuilder<M> objectBuilder = () -> (M) msg.toBuilder().clearField(repeatedField).build();

        FieldAdder<M, ?> fieldAdder = (m, f) -> {
            M msgWithField = (M) m.toBuilder().addRepeatedField(repeatedField, f).build();
            if (msgWithField.getSerializedSize() <= maxMsgSizeInBytes || msgWithField.getRepeatedFieldCount(repeatedField) == 1)
                return Optional.of(msgWithField);
            else return empty();
        };

        return FieldPartitioner.partition(objectBuilder, fieldAdder, (Collection) msg.getField(repeatedField));
    }
}
