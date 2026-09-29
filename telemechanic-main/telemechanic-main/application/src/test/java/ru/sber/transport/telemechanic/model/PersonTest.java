package ru.sber.transport.telemechanic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonTest {
    @Test
    void testEmptyFullName() {
        Person person = new Person("");
        assertEquals("", person.getFirstName());
        assertEquals("", person.getLastName());
        assertEquals("", person.getPatronymic());
    }
    
    @Test
    void testSingleWord() {
        Person person = new Person("Пушкин");
        assertEquals("", person.getFirstName());
        assertEquals("Пушкин", person.getLastName());
        assertEquals("", person.getPatronymic());
    }
    
    @Test
    void testTwoWords() {
        Person person = new Person("Толстой Лев");
        assertEquals("Лев", person.getFirstName());
        assertEquals("Толстой", person.getLastName());
        assertEquals("", person.getPatronymic());
    }
    
    @Test
    void testThreeWords() {
        Person person = new Person("Достоевский Фёдор Михайлович");
        assertEquals("Фёдор", person.getFirstName());
        assertEquals("Достоевский", person.getLastName());
        assertEquals("Михайлович", person.getPatronymic());
    }
    
    @Test
    void testMoreThanThreeParts() {
        Person person = new Person("Лермонтов Михаил Юрьевич Афанасьевич");
        assertEquals("Михаил", person.getFirstName());
        assertEquals("Лермонтов", person.getLastName());
        assertEquals("Юрьевич", person.getPatronymic());
    }
    
    @Test
    void testSpacesBetweenNames() {
        Person person = new Person(" Тургенев Иван Сергеевич ");
        assertEquals("Иван", person.getFirstName().trim());
        assertEquals("Тургенев", person.getLastName().trim());
        assertEquals("Сергеевич", person.getPatronymic().trim());
    }
}