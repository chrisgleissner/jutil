# jutil

[![Build Status](https://travis-ci.org/chrisgleissner/jutil.svg?branch=master)](https://travis-ci.org/chrisgleissner/jutil)
[![Maven Central](https://img.shields.io/maven-metadata/v/http/central.maven.org/maven2/com/github/chrisgleissner/jutil-protobuf/maven-metadata.xml.svg)](https://search.maven.org/artifact/com.github.chrisgleissner/jutil-protobuf)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/jutil/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/jutil?branch=master)

Various Java utilities.

## Getting Started


Maven dependency:

```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>jutil-protobuf</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Protobuf

[ProtobufFieldPartitioner](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/uk/gleissner/jutil/protobuf/ProtobufFieldPartitioner.java)

Distributing the elements of a repeated field in a Protobuf message over multiple newly created messages. This allows for sending a Protobuf message where size restrictions exist, for examine in the case when using Azure Message ServiceBus.

Example:
```java
Collection<Message> msgs = ProtbufFieldPartitioner.partition(msg, repeatedFieldToBePartitioned, 100);
```

