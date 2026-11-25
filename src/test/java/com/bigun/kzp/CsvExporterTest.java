package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {
    /* Предметна область книжок доводить, що експортер не прив'язаний до деталей. */
    private record Book(@CsvColumn("Назва") String title, @CsvColumn("Автор") String author,
                        @CsvColumn("Рік") int year, String secret) {
    }

    private static class Animal {
        @CsvColumn("ім'я")
        private final String name;

        Animal(String name) {
            this.name = name;
        }
    }

    private static final class Dog extends Animal {
        @CsvColumn("вік")
        private final int age;

        Dog(String name, int age) {
            super(name);
            this.age = age;
        }
    }

    private record NoColumns(String value) {
    }

    private record RepeatedColumns(@CsvColumn("поле") String first, @CsvColumn("поле") String second) {
    }

    private record Single(@CsvColumn("значення") String value) {
    }

    private record OnlyStatic(String value) {
        @CsvColumn("константа")
        private static final String CONSTANT = "x";
    }

    @Test
    void annotationIsAvailableAtRuntimeOnFieldsOnly() {
        assertEquals(RetentionPolicy.RUNTIME, CsvColumn.class.getAnnotation(Retention.class).value());
        assertArrayEquals(new ElementType[]{ElementType.FIELD}, CsvColumn.class.getAnnotation(Target.class).value());
    }

    @Test
    void exportsAnyClassWithColumnsSortedByFieldName() throws DataStorageException {
        List<Book> books = List.of(new Book("Кобзар", "Шевченко", 1840, "a"), new Book("Мавка", "Українка", 1911, "b"));

        assertEquals("Автор,Назва,Рік\nШевченко,Кобзар,1840\nУкраїнка,Мавка,1911\n",
                CsvExporter.toCsv(Book.class, books));
    }

    @Test
    void headerDoesNotDependOnRecords() throws DataStorageException {
        assertEquals(List.of("Автор", "Назва", "Рік"), CsvExporter.header(Book.class));
        assertEquals("Автор,Назва,Рік\n", CsvExporter.toCsv(Book.class, List.of()));
    }

    @Test
    void exportsInheritedFields() throws DataStorageException {
        assertEquals("вік,ім'я\n3,Рекс\n", CsvExporter.toCsv(Dog.class, List.of(new Dog("Рекс", 3))));
        assertEquals("ім'я\nРекс\n", CsvExporter.toCsv(Animal.class, List.of(new Dog("Рекс", 3))));
    }

    @Test
    void escapesSpecialCharactersOnly() {
        assertEquals("текст", CsvExporter.escape("текст"));
        assertEquals("", CsvExporter.escape(""));
        assertEquals(" з пробілами ", CsvExporter.escape(" з пробілами "));
        assertEquals("а;б", CsvExporter.escape("а;б"));
        assertEquals("\"а,б\"", CsvExporter.escape("а,б"));
        assertEquals("\"\"\"а\"\"\"", CsvExporter.escape("\"а\""));
        assertEquals("\"а\nб\"", CsvExporter.escape("а\nб"));
        assertEquals("\"а\r\nб\"", CsvExporter.escape("а\r\nб"));
        assertEquals("\"\r\"", CsvExporter.escape("\r"));
    }

    @Test
    void missingValueBecomesEmptyField() throws DataStorageException {
        assertEquals("Автор,Назва,Рік\n,Казка,2000\n",
                CsvExporter.toCsv(Book.class, List.of(new Book("Казка", null, 2000, ""))));
    }

    @Test
    void rejectsClassWithoutSuitableColumns() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.header(NoColumns.class));

        assertTrue(exception.getMessage().contains("NoColumns"));
        assertThrows(DataStorageException.class, () -> CsvExporter.header(OnlyStatic.class));
        assertEquals("x", OnlyStatic.CONSTANT);
    }

    @Test
    void rejectsRepeatedColumnNames() {
        assertThrows(DataStorageException.class, () -> CsvExporter.header(RepeatedColumns.class));
    }

    @Test
    void doesNotChangeExportedList() throws DataStorageException {
        List<Single> records = List.of(new Single("а"), new Single("б"));

        assertEquals("значення\nа\nб\n", CsvExporter.toCsv(Single.class, records));
        assertEquals(List.of(new Single("а"), new Single("б")), records);
    }

    @Test
    void rejectsNullArguments() {
        assertThrows(NullPointerException.class, () -> CsvExporter.toCsv(null, List.of()));
        assertThrows(NullPointerException.class, () -> CsvExporter.toCsv(Single.class, null));
        assertThrows(NullPointerException.class,
                () -> CsvExporter.toCsv(Single.class, java.util.Collections.singletonList(null)));
    }
}
