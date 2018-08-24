package uk.gleissner.jutil.spring.simplebatch;

import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(final Person person) throws Exception {
        return new Person(person.getFirstName().toUpperCase(), person.getLastName().toUpperCase());
    }
}