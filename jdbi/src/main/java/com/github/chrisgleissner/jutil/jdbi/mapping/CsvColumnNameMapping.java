package com.github.chrisgleissner.jutil.jdbi.mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class CsvColumnNameMapping extends MapColumnNameMapping {

    public CsvColumnNameMapping(InputStream mappings) {
        super(readCsv(mappings));
    }

    public CsvColumnNameMapping(Path mappings) {
        super(readCsv(mappings));
    }

    private static Map<String, String> readCsv(InputStream mappings) {
        try {
            return new BufferedReader(new InputStreamReader(mappings, StandardCharsets.UTF_8)).lines()
                    .map(line -> Arrays.asList(line.split(",")))
                    .collect(toMap(line -> line.get(0).trim(), line -> line.get(1).trim()));
        } catch (Exception e) {
            throw new RuntimeException("Could not read mappings", e);
        }
    }

    private static Map<String, String> readCsv(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return readCsv(is);
        } catch (IOException e) {
            throw new RuntimeException("Could not read mappings from " + path, e);
        }
    }
}
