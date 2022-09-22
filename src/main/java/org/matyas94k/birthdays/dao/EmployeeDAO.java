package org.matyas94k.birthdays.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;

import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bson.Document;

import org.matyas94k.birthdays.model.Employee;
import org.matyas94k.birthdays.util.MongoUtil;

public class EmployeeDAO {
    private static final String COLLECTION_NAME = "employees";

    private EmployeeDAO() {}

    public static Employee findOneByEmail(final String email) {
        try (MongoClient mongoClient = MongoUtil.getMongoClient()) {
            MongoCollection<Document> collection = mongoClient.getDatabase(MongoUtil.DATABASE_NAME).getCollection(COLLECTION_NAME);
            Document employeeDocument = collection.find(Filters.eq("email", email), Document.class).first();
            return employeeDocument == null ? null : Employee.fromDocument(employeeDocument);
        }
    }

    public static List<Employee> findAll() {
        try (MongoClient mongoClient = MongoUtil.getMongoClient()) {
            MongoCollection<Document> collection = mongoClient.getDatabase(MongoUtil.DATABASE_NAME).getCollection(COLLECTION_NAME);
            List<Employee> employees = new ArrayList<>();
            try (MongoCursor<Employee> iterator = collection.find(Document.class).map(Employee::fromDocument).iterator()) {
                iterator.forEachRemaining(employees::add);
            }
            return employees;
        }
    }

    public static long getCount() {
        try (MongoClient mongoClient = MongoUtil.getMongoClient()) {
            return mongoClient.getDatabase(MongoUtil.DATABASE_NAME).getCollection(COLLECTION_NAME).countDocuments();
        }
    }

    public static void listExistingBirthdays() {
        try (MongoClient mongoClient = MongoUtil.getMongoClient()) {
            MongoCollection<Document> collection = mongoClient.getDatabase(MongoUtil.DATABASE_NAME).getCollection(COLLECTION_NAME);
            Map<Month, List<Integer>> grouped = new EnumMap<>(Month.class);
            Consumer<MonthDay> monthDayConsumer = md -> {
                Month month = md.getMonth();
                if (grouped.containsKey(month)) {
                    grouped.get(month).add(md.getDayOfMonth());
                } else {
                    List<Integer> days = new ArrayList<>();
                    days.add(md.getDayOfMonth());
                    grouped.put(month, days);
                }
            };
            collection.distinct("birthday", Document.class)
                    .map(d -> MonthDay.of(Month.valueOf(d.getString("month")), d.getInteger("day")))
                    .forEach(monthDayConsumer);
            StringBuilder sb;
            for (Map.Entry<Month, List<Integer>> entry : grouped.entrySet()) {
                sb = new StringBuilder(entry.getKey().name()).append(": ");
                while (sb.length() < 11) {
                    sb.append(" ");
                }
                for (Integer i : entry.getValue()) {
                    sb.append(" ").append(i);
                }
                System.out.println(sb);
            }
        }
    }

    public static void upsertOne(Employee employee) {
        try (MongoClient mongoClient = MongoUtil.getMongoClient()) {
            MongoCollection<Document> collection = mongoClient.getDatabase(MongoUtil.DATABASE_NAME).getCollection(COLLECTION_NAME);
            collection.replaceOne(Filters.eq("email", employee.getEmail()), employee.toDocument(), new ReplaceOptions().upsert(true));
        }
    }

    public static void upsertMany(List<Employee> employees) {
        try (MongoClient mongoClient = MongoUtil.getMongoClient()) {
            MongoCollection<Document> collection = mongoClient.getDatabase(MongoUtil.DATABASE_NAME).getCollection(COLLECTION_NAME);
            Function<Employee, WriteModel<Document>> writeModelMapper = employee ->
                    new ReplaceOneModel<>(Filters.eq("email", employee.getEmail()), employee.toDocument(), new ReplaceOptions().upsert(true));
            System.out.printf("Bulk upsert of %d employees.%n", employees.size());
            collection.bulkWrite(employees.stream().map(writeModelMapper).toList());
        }
    }

}
