package org.matyas94k.birthdays.util;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class MongoUtil {
    public static final String HOSTNAME = "localhost";
    public static final int PORT = 27017;
    public static final String DATABASE_NAME = "company_data";

    private MongoUtil() {}

    public static MongoClient getMongoClient() {
        return new MongoClient(MongoUtil.HOSTNAME, MongoUtil.PORT);
    }

    public static boolean dBRunning() {
        try (MongoClient mongoClient = MongoUtil.getMongoClient()) {
            return mongoClient.getDatabase(DATABASE_NAME).runCommand(new BasicDBObject("ping", "1")).containsKey("ok");
        } catch (MongoException e) {
            return false;
        }
    }

}
