package com.github.chrisgleissner.jutil.jdbi.mapping;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Slf4j
public abstract class AbstractColumnNameMapping implements ColumnNameMapping {
    private final Map<String, String> mappings;

    protected AbstractColumnNameMapping(Map<String, String> mappings) {
        this.mappings = mappings.entrySet().stream()
                .collect(toMap(e -> e.getKey().toLowerCase(), e -> e.getValue().toLowerCase()));
    }

    @Override
    public String apply(String label) {
        val mappedLabel = mappings.get(label.toLowerCase());
        if (mappedLabel == null) {
            log.info("No mapping found for column {}", label);
            return label;
        } else {
            log.debug("Mapping column {} to {}", label, mappedLabel);
            return mappedLabel;
        }
    }
}
