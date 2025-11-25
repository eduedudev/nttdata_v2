package com.nttdata.customer.client.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Test
    void shouldCreatePersonWithAllFields() {
        Person person = PersonMother.createDefault();

        assertNotNull(person);
        assertEquals("John Doe", person.getName());
        assertEquals(Gender.MALE, person.getGender());
        assertEquals("1234567890", person.getIdentification());
        assertEquals("123 Main Street", person.getAddress());
        assertEquals("+573001234567", person.getPhone());
    }

    @Test
    void shouldCreateFemalePerson() {
        Person person = PersonMother.createFemale();

        assertEquals("Jane Doe", person.getName());
        assertEquals(Gender.FEMALE, person.getGender());
        assertEquals("0987654321", person.getIdentification());
    }

    @Test
    void shouldCreatePersonWithSpecificGender() {
        Person person = PersonMother.createWithGender(Gender.OTHER);

        assertEquals(Gender.OTHER, person.getGender());
    }

    @Test
    void shouldCreatePersonWithSpecificName() {
        Person person = PersonMother.createWithName("Custom Name");

        assertEquals("Custom Name", person.getName());
    }

    @Test
    void shouldCreatePersonWithSpecificIdentification() {
        Person person = PersonMother.createWithIdentification("9999999999");

        assertEquals("9999999999", person.getIdentification());
    }

    @Test
    void shouldAllowSettersToModifyPerson() {
        Person person = new Person();
        person.setName("Modified Name");
        person.setGender(Gender.FEMALE);
        person.setIdentification("5555555555");
        person.setAddress("Modified Address");
        person.setPhone("+571234567890");

        assertEquals("Modified Name", person.getName());
        assertEquals(Gender.FEMALE, person.getGender());
        assertEquals("5555555555", person.getIdentification());
        assertEquals("Modified Address", person.getAddress());
        assertEquals("+571234567890", person.getPhone());
    }
}
