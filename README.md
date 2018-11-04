# util

[![Build Status](https://travis-ci.org/chrisgleissner/util.svg?branch=master)](https://travis-ci.org/chrisgleissner/util)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/util/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/util?branch=master)

Various Java utilities.

## Protobuf

[ProtobufFieldPartitioner](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/uk/gleissner/jutil/protobuf/ProtobufFieldPartitioner.java)

Distributing the elements of a repeated field in a Protobuf message over multiple newly created messages. This allows for sending a Protobuf message where size restrictions exist, for examine in the case when using Azure Message ServiceBus.

Example:
```java
Collection<Message> msgs = ProtbufFieldPartitioner.partition(msg, repeatedFieldToBePartitioned, 100);
```

