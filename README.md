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

The [ProtobufFieldPartitioner](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/com/github/chrisgleissner/jutil/protobuf/ProtobufFieldPartitioner.java) 
is useful for distributing the elements of a repeated field in a Protobuf message over multiple newly created messages. 

This allows for sending a Protobuf message where size restrictions exist, for example when using the
<a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quotas">Azure Message ServiceBus</a>.

Example:
```java
Collection<Message> msgs = ProtbufFieldPartitioner.partition(msg, repeatedFieldToBePartitioned, 100);
```

## Table Printer

The [TablePrinter](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/com/github/chrisgleissner/jutil/table/TablePrinter.java) 
serializes a table to a pretty-printed string, either using ASCII or UTF borders.

A table consists of a header and a random number of rows and these can can be specified as an `Iterable<String>` header and `Iterable<Iterable<String>>` rows. 

Alternatively, you can 
extend your existing data structure to implement the [Table](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/com/github/chrisgleissner/jutil/table/Table.java) interface.

For example
```java
Iterable<String> headers = Arrays.asList("firstName", "lastName");
Iterable<Iterable<String>> rows = Arrays.asList(Arrays.asList("john", "doe"), Arrays.asList("joe", "doe"));
System.out.println(DefaultTablePrinter.print(headers, rows));
```
results in:
```
+===========+==========+
| firstName | lastName |
|===========|==========|
| john      | doe      |
| joe       | doe      |
+===========+==========+
```

Alternatively, you can configure the printer whereby
```java
Iterable<String> headers = Arrays.asList("firstName", "lastName");
Iterable<Iterable<String>> rows = Arrays.asList(Arrays.asList("john", "doe"), Arrays.asList("joe", null));
String tableString = TablePrinter.builder()
        .horizontalDividers(true)
        .nullValue("n/a")
        .tableFormat(new Utf8TableFormat())
        .rowNumbers(true)
        .startRow(1)
        .maxCellWidth(5)
        .build().print(headers, rows);
```
results in:
```
╔═══╤═══════╤═══════╗
║ # │ first │ lastN ║
╠═══╪═══════╪═══════╣
║ 1 │ joe   │ n/a   ║
╚═══╧═══════╧═══════╝
```

As per the example above, if you have a very large data structure, you may want to use the `startRow` and `endRow` builder methods 
to only print the specified range.

Likewise, if you have very long columns, you can limit their printed lengths with the `maxCellWidth` method.
