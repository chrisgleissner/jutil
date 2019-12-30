package com.github.chrisgleissner.jutil.jdbi;

import com.github.chrisgleissner.jutil.jdbi.mapping.CsvColumnNameMapping;
import com.github.chrisgleissner.jutil.jdbi.mapping.MapColumnNameMapping;
import com.google.common.collect.ImmutableMap;
import lombok.Value;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.ClassRule;
import org.junit.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RenamingRowMapperTest {

    @ClassRule
    public static JdbiRule jdbiRule = JdbiRule.h2().withMigration(new Migration().withDefaultPath());

    @Test
    public void mapsColumnNamesViaRowMapperFactory() {
        jdbiRule.getHandle().registerRowMapper(RenamingRowMapperFactory.mapColNames(
                ConstructorMapper.factory(Person.class),
                new MapColumnNameMapping(ImmutableMap.of("nachname", "surname", "geburtstag", "birthday"))));
        List<Person> persons = jdbiRule.getHandle().createQuery("select id, nachname, geburtstag from person").mapTo(Person.class)
                .collect(Collectors.toList());
        assertPersonListInitialized(persons);
    }

    @Test
    public void mapsColumnNamesViaRowMapperFactoryAndMappingsFromCsvFile() {
        jdbiRule.getHandle().registerRowMapper(RenamingRowMapperFactory.mapColNames(
                ConstructorMapper.factory(Person.class),
                new CsvColumnNameMapping(Path.of("src/test/resources/columnNameMappings.csv"))));
        List<Person> persons = jdbiRule.getHandle().createQuery("select id, nachname, geburtstag from person")
                .mapTo(Person.class)
                .collect(Collectors.toList());
        assertPersonListInitialized(persons);
    }


    private void assertPersonListInitialized(Collection<Person> persons) {
        assertThat(persons).extracting(p -> p.id).containsExactly(1, 2);
        assertThat(persons).extracting(Person::getSurname).containsExactly("Miller", "Farmer");
        assertThat(persons).extracting(Person::getBirthday).hasSize(2);
    }

    @Test
    public void mapsColumnNamesViaCustomRowMapper() {
        List<Person> persons = jdbiRule.getHandle().createQuery("select id, nachname, geburtstag from person")
                .map(RenamingRowMapper.mapColNames((rs, ctx) ->
                                new Person(rs.getInt("id"), rs.getString("nachname"), rs.getDate("geburtstag").toLocalDate()),
                        new MapColumnNameMapping(ImmutableMap.of("nachname", "surname", "geburtstag", "birthday"))))
                .collect(Collectors.toList());
        assertPersonListInitialized(persons);
    }

    @Test
    public void doesntMapColumnNamesIfNoMappingFound() {
        jdbiRule.getHandle().registerRowMapper(RenamingRowMapperFactory.mapColNames(
                ConstructorMapper.factory(DifferentLanguagesPerson.class),
                new MapColumnNameMapping(ImmutableMap.of("nachname", "surname"))));
        List<DifferentLanguagesPerson> persons = jdbiRule.getHandle().createQuery("select id, nachname, geburtstag from person")
                .mapTo(DifferentLanguagesPerson.class)
                .collect(Collectors.toList());
        assertThat(persons).extracting(p -> p.id).containsExactly(1, 2);
        assertThat(persons).extracting(DifferentLanguagesPerson::getSurname).containsExactly("Miller", "Farmer");
        assertThat(persons).extracting(DifferentLanguagesPerson::getGeburtstag).hasSize(2);
    }

    @Value
    public static class Person {
        int id;
        String surname;
        LocalDate birthday;
    }

    @Value
    public static class DifferentLanguagesPerson {
        int id;
        String surname;
        LocalDate geburtstag;
    }
}