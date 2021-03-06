# jutil

[![Maven Central](https://img.shields.io/maven-central/v/com.github.chrisgleissner/jutil-protobuf)](https://search.maven.org/artifact/com.github.chrisgleissner/jutil-protobuf/)
[![Build Status](https://travis-ci.org/chrisgleissner/jutil.svg?branch=master)](https://travis-ci.org/chrisgleissner/jutil)
[![Coverage Status](https://coveralls.io/repos/github/chrisgleissner/jutil/badge.svg?branch=master)](https://coveralls.io/github/chrisgleissner/jutil?branch=master)
[![Maintainability](https://api.codeclimate.com/v1/badges/04051d34bbf92198458e/maintainability)](https://codeclimate.com/github/chrisgleissner/jutil/maintainability)

Java utilities for Protobuf message partitioning, table pretty printing and SQL execution recording.

Features:
* Partitioning of Protobuf messages to remain below a configurable data size.
* JDBI column name remapping
* Pretty-printing of tables with many customization options and adapters for both CSV frameworks and DB ResultSets.
* Record all SQL executions sent via the DataSources in your Spring Boot application, either in memory or to disk.

## Installation

The utilities are packaged in several modules and require at least JDK 8. They are automatically built and tested 
using OpenJDK 8 and 11.

To use them, simply declare a dependency towards the module you are interested in:

### Maven

```xml
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>jutil-protobuf</artifactId>
    <version>1.1.11</version>
</dependency>
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>jutil-sql-log</artifactId>
    <version>1.1.11</version>
</dependency>
<dependency>
    <groupId>com.github.chrisgleissner</groupId>
    <artifactId>jutil-table</artifactId>
    <version>1.1.11</version>
</dependency>
```

### Gradle

```
compile 'com.github.chrisgleissner:jutil-protobuf:1.1.11'
compile 'com.github.chrisgleissner:jutil-sql-log:1.1.11'
compile 'com.github.chrisgleissner:jutil-table:1.1.11'
```

## Protobuf Utilities

[![Javadocs](https://www.javadoc.io/badge/com.github.chrisgleissner/jutil-protobuf.svg)](https://www.javadoc.io/doc/com.github.chrisgleissner/jutil-protobuf)

The [ProtobufFieldPartitioner](https://github.com/chrisgleissner/jutil/blob/master/protobuf/src/main/java/com/github/chrisgleissner/jutil/protobuf/ProtobufFieldPartitioner.java) 
is useful for distributing the elements of a repeated field in a Protobuf message over multiple newly created messages. 

This allows for sending a Protobuf message where size restrictions exist, for example when using the
<a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quotas">Azure Message ServiceBus</a>.

Example:
```java
Collection<Message> msgs = ProtobufFieldPartitioner.partition(msg, repeatedFieldToBePartitioned, 100);
```

## Sql Log

[![Javadocs](https://www.javadoc.io/badge/com.github.chrisgleissner/jutil-sql-log.svg)](https://www.javadoc.io/doc/com.github.chrisgleissner/jutil-sql-log)

The [SqlLog](https://github.com/chrisgleissner/jutil/blob/master/sql-log/src/main/java/com/github/chrisgleissner/jutil/sqllog/SqlLog.java) 
records JSON-formatted SQL executions either in memory or to a file. It gets wired by using Spring Boot 2.3.x auto-configuration and relies on [net.ttddyy:datasource-proxy](https://github.com/ttddyy/datasource-proxy) for proxying data sources. 

To use this feature, declare a dependency on `com.github.chrisgleissner:jutil-sql-log`.

### Start and Stop
To start recording, wire in the `SqlLog` bean and call `startRecording` which returns a `SqlRecording`.
You will now record all SQL messages sent via any DataSource bean as part of your current thread or any thread which it starts. 

The recording is kept either on heap (accessible via `sqlRecording.getMessages` and various methods in `SqlLog`) or written to the specified file. 
All SQL is JSON encoded. 

Call `sqlLog.stopRecording(id)` or close a `SqlRecording` instance to stop its recording. 

### Default Recording

Any SQL message not recorded otherwise is captured by a default recording which you can get via 
`sqlLog.getDefaultRecording()`.

This default recording can't be stopped,
but you can temporarily stop all recording (including for the default recording) by calling `sqlLog.setEnabled(false)`.

### Example 

As per [ExampleTest](https://github.com/chrisgleissner/jutil/blob/master/sql-log/src/test/java/com/github/chrisgleissner/jutil/sqllog/ExampleTest.java),
after wiring 

```java
@Configuration
public class SampleConfig {
    SampleConfig(JdbcTemplate jdbcTemplate, SqlLog sqlLog) {
        try (SqlRecording rec = sqlLog.startRecording("example", new File("sql.json"), Charset.forName("UTF-8"))) {
            jdbcTemplate.execute("create table foo (id int)");
            jdbcTemplate.execute("insert into foo (id) values (1)");
        }
    }
}
```

you will find that the `sql.json` file contains

```json
[{"success":true, "type":"Statement", "batch":false, "querySize":1, "batchSize":0, "query":["create table foo (id int)"], "params":[]},
{"success":true, "type":"Statement", "batch":false, "querySize":1, "batchSize":0, "query":["insert into foo (id) values (1)"], "params":[]}]

```

## JDBI Column Name Mapping

The [JDBI Column Mapper](https://jdbi.org/#_column_mappers) allows for easy mapping of a `ResultSet` into Java objects. 
The `jdbi` utility package in this repository provides a wrapper for remapping the column names exposed by a JDBC `ResultSet` and
can thus decorate any `ColumnMapper` implementation. 

This is useful if the DB schema uses a different language or terminology than the Java model to which it is mapped and
it is an alternative to applying this mapping via SQL query column labels.

Example:

```java
jdbiHandle.registerRowMapper(RenamingRowMapperFactory.mapColNames(
        ConstructorMapper.factory(Person.class),
        new CsvColumnNameMapping(Path.of("columnNameMappings.csv"))));
List<Person> personList = jdbiHandle.createQuery("select id, nachname, geburtstag from person")
        .mapTo(Person.class)
        .collect(Collectors.toList());
```

## Table Printer

[![Javadocs](https://www.javadoc.io/badge/com.github.chrisgleissner/jutil-table.svg)](https://www.javadoc.io/doc/com.github.chrisgleissner/jutil-table)

The [TablePrinter](https://github.com/chrisgleissner/jutil/blob/master/table/src/main/java/com/github/chrisgleissner/jutil/table/TablePrinter.java) 
serializes a table to a pretty-printed string, either using ASCII or UTF borders.

A table consists of a header and a random number of rows. These can can be specified as an `Iterable<String>` header 
and `Iterable<Iterable<String>>` rows. Adapters to various 3rd party frameworks are available, see below. 

Example:
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

Alternatively, you can also print to an `OutputStream`.

### Configuration

The TablePrinter is fully configurable to customize your output:

```java
Iterable<String> headers = Arrays.asList("firstName", "lastName");
Iterable<Iterable<String>> rows = Arrays.asList(
        Arrays.asList("Tom", "Selleck"), 
        Arrays.asList("John", "Hillerman"),
        Arrays.asList("Roger E.", null), 
        Arrays.asList("Larry", "Manetti"));

System.out.println(TablePrinter.builder()
        .horizontalDividers(true)
        .nullValue("n/a")
        .tableFormat(new Utf8TableFormat())
        .rowNumbers(true)
        .startRow(1)
        .endRow(3)
        .maxCellWidth(5)
        .wraparound(false)
        .build().print(headers, rows));
```
results in:
```
╔═══╤═══════╤═══════╗
║ # │ first │ lastN ║
╠═══╪═══════╪═══════╣
║ 1 │ John  │ Hille ║
╟───┼───────┼───────╢
║ 2 │ Roger │ n/a   ║
╟───┼───────┼───────╢
║ 3 │ Larry │ Manet ║
╚═══╧═══════╧═══════╝
```

As per the example above, if you have a very large data structure, you may want to use the `startRow` and `endRow` builder methods 
to only print the specified range. You can also set the maximum cell width (defaults to 100) and control the wrap-aroud of long cells (enabled by default).

Newlines are supported and tabs are rendered as 8 spaces (configurable).

### 3rd Party Adapters

Any data structure that implements the [TableProvider](https://github.com/chrisgleissner/jutil/blob/master/table/src/main/java/com/github/chrisgleissner/jutil/table/provider/TableProvider.java) interface
can be printed and various adapters for this interface are available:

<a href="https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html">DB ResultSet</a>
```java
Class.forName("org.h2.Driver");
Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
String s = DefaultTablePrinter.print(
        new ResultSetTableProvider(conn.createStatement().executeQuery("select * from foo"))));
```

<a href="https://www.univocity.com/pages/about-parsers">Univocity CSV Parser</a> 
```java
CsvParserSettings settings = new CsvParserSettings();
settings.setHeaderExtractionEnabled(true);
CsvParser parser = new CsvParser(settings);
String s = DefaultTablePrinter.print(
        UnivocityTableProvider.of(parser.iterateRecords(new File("sample.csv"))));
```

<a href="https://commons.apache.org/proper/commons-csv/">Apache Commons CSV Parser</a>
```java
String s = DefaultTablePrinter.print(
    new ApacheCsvTableProvider(CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(
        new FileReader(new File("sample.csv")))))
                
```

JavaBeans
```java
Iterable<Person> people = Arrays.asList(new Person("john", "doe", 30),
        new Person("mary", "poppins", 40));
String s = DefaultTablePrinter.print(new BeanTableProvider(people))             
```
