package com.github.chrisgleissner.jutil.table.provider;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class BeanTableProvider implements TableProvider {
    private final Iterable<? extends Object> beans;
    private Map<String, Method> readMethodsByPropertyName = new HashMap<>();

    public BeanTableProvider(Iterable<? extends Object> beans) {
        this.beans = beans;
        try {
            if (beans.iterator().hasNext()) {
                Object bean = beans.iterator().next();
                BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
                readMethodsByPropertyName = Arrays.stream(beanInfo.getPropertyDescriptors())
                        .filter(pd -> !pd.getName().equals("class"))
                        .collect(LinkedHashMap::new, (map, pd) -> {
                            pd.getReadMethod().setAccessible(true);
                            map.put(pd.getName(), pd.getReadMethod());
                        }, Map::putAll);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't get Bean properties", e);
        }
    }

    @Override
    public Iterable<String> getHeaders() {
        return readMethodsByPropertyName.keySet();
    }

    @Override
    public Iterable<? extends Iterable<String>> getRows() {
        return (Iterable<List<String>>) () -> stream(beans.spliterator(), false)
                .map(bean -> readMethodsByPropertyName.values().stream().map(method -> {
                    try {
                        return "" + method.invoke(bean, null);
                    } catch (Exception e) {
                        throw new RuntimeException(format("Can't access bean property method %s for bean %s", method, bean), e);
                    }
                }).collect(toList())).iterator();
    }
}
