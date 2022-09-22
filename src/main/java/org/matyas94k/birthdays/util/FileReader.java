package org.matyas94k.birthdays.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;

import org.matyas94k.birthdays.dao.EmployeeDAO;
import org.matyas94k.birthdays.exception.FileStructureException;
import org.matyas94k.birthdays.model.Employee;

public class FileReader {
    private static String localPath;
    private static MonthDay date;

    private FileReader() {
    }

    public static void initCurrentDirectory(final String currentDirectory) {
        localPath = currentDirectory + '/';
    }

    public static void readFileByFilename(final String fileName) {
        if (null == localPath) {
            return;
        }
        final Path filePath = Path.of(localPath + fileName);
        if (Files.notExists(filePath)) {
            System.out.printf("The file %s does not exist.%n", fileName);
            return;
        }
        System.out.printf("Reading: %s%n", fileName);
        List<Employee> employees;
        if (Files.isDirectory(filePath)) {
            System.out.printf("%s is a directory.%n", fileName);
            employees = readDirectoryIntoList(filePath);
        } else {
            if (!fileName.endsWith(".eml")) {
                System.out.printf("%s is not a supported format (.eml).%n", fileName);
                return;
            }
            employees = readFileIntoList(filePath);
        }
        System.out.printf("%d employees to save.%n", employees.size());
        CollectionUtils.split(employees, 4096).forEach(EmployeeDAO::upsertMany);
    }

    private static List<Employee> readDirectoryIntoList(final Path directoryPath) {
        List<Employee> employees = new ArrayList<>();
        try (Stream<Path> paths = Files.list(directoryPath)) {
            paths.filter(Files::exists).forEach(path -> {
                if (Files.isDirectory(path)) {
                    employees.addAll(readDirectoryIntoList(path));
                } else if (path.toString().endsWith(".eml")) {
                    employees.addAll(readFileIntoList(path));
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return employees;
    }

    private static List<Employee> readFileIntoList(final Path filePath) {
        List<Employee> employeeList = new ArrayList<>();
        try {
            String emlDataString = Files.readString(filePath);
            Email message = EmailConverter.emlToEmail(emlDataString);
            Elements employeesContent = extractEmployeesContentFromEmail(message);
            Consumer<Element> parseTool = element -> {
                if ("a".equals(element.tagName())) {
                    employeeList.add(new Employee(element.attr("href").substring(7), element.text()));
                } else {
                    String description = element.text();
                    int i = description.indexOf(')');
                    String role = description.substring(1, i);
                    String location = description.substring(i + 2);
                    Employee underProcessing = employeeList.get(employeeList.size() - 1);
                    underProcessing.setRole(role);
                    underProcessing.setLocation(location);
                    underProcessing.setBirthday(date);
                }
            };
            employeesContent.stream().skip(1).filter(element -> !"br".equals(element.tagName())).forEach(parseTool);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FileStructureException e) {
            System.err.println("The file does not have the supported structure - skipping.");
            return Collections.emptyList();
        }
        return employeeList;
    }

    private static Elements extractEmployeesContentFromEmail(Email message) throws FileStructureException {
        String body = message.getHTMLText();
        try {
            Objects.requireNonNull(body);
            Document document = Jsoup.parse(body);
            Elements bodyElements = document.body().getAllElements();
            String dateString = bodyElements.get(6).text();
            date = parseDate(dateString);
            return bodyElements.get(13).getAllElements();
        } catch (NullPointerException e) {
            throw new FileStructureException("body missing");
        } catch (Exception e) {
            throw new FileStructureException("unsupported structure");
        }
    }

    private static MonthDay parseDate(final String dateString) {
        StringTokenizer tokenizer = new StringTokenizer(dateString);
        tokenizer.nextToken(); // omit the day name part
        Month month = Month.valueOf(tokenizer.nextToken().toUpperCase());
        int day = Integer.parseInt(tokenizer.nextToken());
        return MonthDay.of(month, day);
    }

}
