# jutil

[![Build Status](https://travis-ci.org/chrisgleissner/jutil.svg?branch=master)](https://travis-ci.org/chrisgleissner/jutil)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/jutil/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/jutil?branch=master)

Various Java utilities for Protobuf, Spring Batch and Quartz. 

## Protobuf

[ProtobufFieldPartitioner](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/uk/gleissner/jutil/protobuf/ProtobufFieldPartitioner.java)

Distributing the elements of a repeated field in a Protobuf message over multiple newly created messages. This allows for sending a Protobuf message where size restrictions exist, for examine in the case when using Azure Message ServiceBus.

Example:
```java
Collection<Message> msgs = ProtbufFieldPartitioner.partition(msg, repeatedFieldToBePartitioned, 100);
```

## Spring Batch

[AdHocScheduler](https://github.com/chrisgleissner/jutil/blob/master/spring-batch/src/main/java/uk/gleissner/jutil/spring/batch/adhoc/AdHocScheduler.java)

Ad-hoc scheduler to register and trigger Spring Batch jobs using Quartz CRON triggers at run-time rather than Spring
wiring time. This allows for simplified set-up of a large number of jobs that only differ slightly.

Example:
```java
adHocScheduler.schedule("jobName", () -> job, "0/30 * * * * ?");

```
