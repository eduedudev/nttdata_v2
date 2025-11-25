package com.nttdata.customer.client.domain;

public class PersonMother {

    public static Person.PersonBuilder<?, ?> validPerson() {
        return Person.builder()
                .name("John Doe")
                .gender(Gender.MALE)
                .identification("1234567890")
                .address("123 Main Street")
                .phone("+573001234567");
    }

    public static Person createDefault() {
        return validPerson().build();
    }

    public static Person createFemale() {
        return validPerson()
                .name("Jane Doe")
                .gender(Gender.FEMALE)
                .identification("0987654321")
                .build();
    }

    public static Person createWithGender(Gender gender) {
        return validPerson().gender(gender).build();
    }

    public static Person createWithName(String name) {
        return validPerson().name(name).build();
    }

    public static Person createWithIdentification(String identification) {
        return validPerson().identification(identification).build();
    }
}
