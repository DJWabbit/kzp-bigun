package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoryTest {
    @Test
    void keepsRecordsInOrderOfAdding() {
        Repository<String> repository = new Repository<>();
        repository.add("б");
        repository.add("а");
        repository.add("б");

        assertEquals(List.of("б", "а", "б"), repository.all());
    }

    @Test
    void newRepositoryIsEmpty() {
        assertTrue(new Repository<SparePart>().all().isEmpty());
        assertTrue(new Repository<SparePart>().find(part -> true).isEmpty());
    }

    @Test
    void snapshotDoesNotChangeAndIsUnmodifiable() {
        Repository<Integer> repository = new Repository<>();
        repository.add(1);
        List<Integer> snapshot = repository.all();
        repository.add(2);

        assertEquals(List.of(1), snapshot);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(3));
        assertEquals(List.of(1, 2), repository.all());
    }

    @Test
    void findsRecordsByCondition() {
        Repository<SparePart> repository = new Repository<>();
        repository.add(SparePart.fromCsv("A-1;Деталь;1;10;П"));
        repository.add(SparePart.fromCsv("A-1-R;Деталь;5;10;П"));

        List<SparePart> found = repository.find(part -> part.getStock() > 3);
        assertEquals(1, found.size());
        assertEquals("A-1-R", found.get(0).getSku());
        assertTrue(repository.find(part -> part.getStock() > 100).isEmpty());
        assertThrows(UnsupportedOperationException.class, found::clear);
    }

    @Test
    void acceptsConditionForSupertype() {
        Repository<SparePart> repository = new Repository<>();
        repository.add(SparePart.fromCsv("A-1-R;Деталь;5;10;П"));
        Predicate<Object> isReplacement = item -> item instanceof ReplacementPart;

        assertEquals(1, repository.find(isReplacement).size());
    }

    @Test
    void rejectsNullRecordAndCondition() {
        Repository<String> repository = new Repository<>();

        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertThrows(NullPointerException.class, () -> repository.find(null));
        assertTrue(repository.all().isEmpty());
    }

    @Test
    void exceptionKeepsMessageAndCause() {
        IOException cause = new IOException("диск");
        DataStorageException exception = new DataStorageException("Не вдалося записати", cause);

        assertEquals("Не вдалося записати", exception.getMessage());
        assertSame(cause, exception.getCause());
        assertNull(new DataStorageException("Помилка").getCause());
    }
}
