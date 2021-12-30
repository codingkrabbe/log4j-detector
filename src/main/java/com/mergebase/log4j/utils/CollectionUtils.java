package com.mergebase.log4j.utils;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class CollectionUtils {

    private CollectionUtils() {}

    public static <T> boolean allMatchNonEmpty(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream().filter(predicate).count() == collection.size();
    }

    public static <T> boolean allFilteredMatchNonEmpty(Collection<T> collection, Predicate<T> filter, Predicate<T> test) {
        List<T> filtered = collection.stream().filter(filter).collect(Collectors.toList());
        return !filtered.isEmpty() && allMatchNonEmpty(filtered, test);
    }

}
