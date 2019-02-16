package com.github.chrisgleissner.jutil.sqllog;

import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static javax.persistence.GenerationType.AUTO;

@Entity
@ToString
public class Person {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;
    private String firstName;
    private String lastName;

    protected Person() {
    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}