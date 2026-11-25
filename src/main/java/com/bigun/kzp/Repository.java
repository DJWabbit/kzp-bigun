package com.bigun.kzp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Type-safe in-memory storage of records.
 *
 * @param <T> record type
 */
public final class Repository<T> {
    private final List<T> items = new ArrayList<>();

    /**
     * Adds a record.
     *
     * @param item record to store
     * @throws NullPointerException if the record is null
     */
    public void add(T item) {
        items.add(Objects.requireNonNull(item, "Запис не може бути null"));
    }

    /**
     * Returns an unmodifiable snapshot of all records in the order of adding.
     *
     * @return records stored at the moment of the call
     */
    public List<T> all() {
        return List.copyOf(items);
    }

    /**
     * Returns the records that satisfy a condition.
     *
     * @param predicate condition for the record type or its supertype
     * @return unmodifiable list of matching records in the order of adding
     * @throws NullPointerException if the condition is null
     */
    public List<T> find(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "Предикат не може бути null");
        return items.stream()
                .filter(predicate)
                .toList();
    }
}
