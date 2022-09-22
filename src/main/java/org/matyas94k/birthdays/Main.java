package org.matyas94k.birthdays;

import java.util.Arrays;

import org.matyas94k.birthdays.dao.EmployeeDAO;
import org.matyas94k.birthdays.util.FileReader;
import org.matyas94k.birthdays.util.MongoUtil;

public class Main {

    public static void main(String[] args) {
        if (!MongoUtil.dBRunning()) {
            System.err.println("Database is down!");
            System.exit(1);
        }
        FileReader.initCurrentDirectory(System.getProperty("user.dir"));
        if (args.length == 0) {
            System.out.println("No files specified.");
        }
        //EmployeeDAO.upsertOne(new Employee("john.doe@example.com", "John Doe", "Java Developer", "EX/AM/Ple", MonthDay.of(9, 13)));
        Arrays.stream(args).forEach(FileReader::readFileByFilename);
        System.out.printf("%d employees saved so far.%n", EmployeeDAO.getCount());
        //EmployeeDAO.listExistingBirthdays();
    }

}
