package com.github.chrisgleissner.jutil.jdbi.mapping;

import java.util.Map;

public class MapColumnNameMapping extends AbstractColumnNameMapping {
    public MapColumnNameMapping(Map<String, String> mappings) {
        super(mappings);
    }
}
