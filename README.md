# jutil

[![Build Status](https://travis-ci.org/chrisgleissner/jutil.svg?branch=master)](https://travis-ci.org/chrisgleissner/jutil)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/jutil/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/jutil?branch=master)

Various Java utilies.

## Protobuf Field Partitioner

Distributing the elements of a repeated field in a Protobuf message over multiple newly created messages. This allows for sending a Protobuf message where size restrictions exist, for examine in the case when using Azure Message ServiceBus.

Usage:
```
int maxPartitionSizeInBytes = 100;
Parent msg = Parent.newBuilder().setId(parentId).addAllChildren(children(1,2,3,4,5)).build();
Collection<Parent> partitionedMsgs = partition(msg, childrenField, maxPartitionSizeInBytes);
```
