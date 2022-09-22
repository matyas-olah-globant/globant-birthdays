package org.matyas94k.birthdays.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {
    private CollectionUtils() {}

    public static <T> List<List<T>> split(List<T> original, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException(String.format("Illegal batch size: %d. It should be a positive integer", batchSize));
        }
        List<List<T>> lists = new ArrayList<>();
        int from = 0;
        final int size = original.size();
        while (from < size) {
            int to = Math.min(from + batchSize, size);
            lists.add(original.subList(from, to));
            from = to;
        }
        return lists;
    }

}
