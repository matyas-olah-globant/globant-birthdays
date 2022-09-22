package org.matyas94k.birthdays.model;

import java.time.Month;
import java.time.MonthDay;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Employee {
    private final String email;
    private final String name;
    private String role;
    private String location;
    private MonthDay birthday;

    @Override
    public String toString() {
        return name + " (" + role + ") " + email + " - " + location;
    }

    @NotNull
    public static Employee fromDocument(Document document) {
        String email = document.getString("email");
        String name = document.getString("name");
        String role = document.getString("role");
        String location = document.getString("location");
        Document birthdayDocument = document.get("birthday", Document.class);
        Month month = Month.valueOf(birthdayDocument.getString("month"));
        int day = birthdayDocument.getInteger("day");
        return new Employee(email, name, role, location, MonthDay.of(month, day));
    }

    public Document toDocument() {
        Document birthDayDocument = new Document("month", birthday.getMonth().name()).append("day", birthday.getDayOfMonth());
        return new Document("email", email).append("name", name).append("role", role).append("location", location).append("birthday", birthDayDocument);
    }

}
