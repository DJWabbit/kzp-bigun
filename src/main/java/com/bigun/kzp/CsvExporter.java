package com.bigun.kzp;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts objects of any class with {@link CsvColumn} fields to a CSV document.
 *
 * <p>Columns are sorted by Java field names; the header contains the names from the annotation.
 */
public final class CsvExporter {
    private static final char SEPARATOR = ',';
    private static final String LINE_END = "\n";

    private CsvExporter() {
    }

    /**
     * Creates a CSV document with a header and one record per object.
     *
     * @param type class of the records; it is needed to build the header of an empty document
     * @param records records to export
     * @param <T> record type
     * @return CSV document that ends with a line feed
     * @throws DataStorageException if the class has no suitable fields or a field cannot be read
     */
    public static <T> String toCsv(Class<T> type, List<? extends T> records) throws DataStorageException {
        Objects.requireNonNull(records, "Список записів не може бути null");
        List<Field> fields = annotatedFields(type);
        StringBuilder csv = new StringBuilder(line(columnNames(fields)));
        for (T item : records) {
            csv.append(line(values(fields, item)));
        }
        return csv.toString();
    }

    /**
     * Returns the column names of a class in the export order.
     *
     * @param type class with annotated fields
     * @return unmodifiable list of column names
     * @throws DataStorageException if the class has no suitable fields
     */
    public static List<String> header(Class<?> type) throws DataStorageException {
        return columnNames(annotatedFields(type));
    }

    /**
     * Collects annotated instance fields of the class and its superclasses.
     *
     * @param type class to inspect
     * @return accessible fields sorted by name
     * @throws DataStorageException if there are no fields, columns repeat, or access is denied
     */
    private static List<Field> annotatedFields(Class<?> type) throws DataStorageException {
        Objects.requireNonNull(type, "Клас не може бути null");
        List<Field> fields = Stream.<Class<?>>iterate(type, Objects::nonNull, Class::getSuperclass)
                .flatMap(level -> Arrays.stream(level.getDeclaredFields()))
                .filter(field -> field.isAnnotationPresent(CsvColumn.class))
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .sorted(Comparator.comparing(Field::getName)
                        .thenComparing(field -> field.getDeclaringClass().getName()))
                .toList();
        if (fields.isEmpty()) {
            throw new DataStorageException("Клас " + type.getSimpleName() + " не має полів із @CsvColumn");
        }
        List<String> names = columnNames(fields);
        if (names.stream().distinct().count() != names.size()) {
            throw new DataStorageException("Клас " + type.getSimpleName() + " має однакові назви стовпців");
        }
        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                throw new DataStorageException("Немає доступу до поля " + field.getName());
            }
        }
        return fields;
    }

    private static List<String> columnNames(List<Field> fields) {
        return fields.stream()
                .map(field -> field.getAnnotation(CsvColumn.class).value())
                .toList();
    }

    /**
     * Reads field values; a missing value becomes an empty field.
     *
     * @param fields accessible fields
     * @param item object to read
     * @return values as text
     * @throws DataStorageException if a field cannot be read
     */
    private static List<String> values(List<Field> fields, Object item) throws DataStorageException {
        Objects.requireNonNull(item, "Запис не може бути null");
        String[] values = new String[fields.size()];
        for (int index = 0; index < values.length; index++) {
            Field field = fields.get(index);
            try {
                values[index] = Objects.toString(field.get(item), "");
            } catch (IllegalAccessException exception) {
                throw new DataStorageException("Не вдалося прочитати поле " + field.getName(), exception);
            }
        }
        return Arrays.asList(values);
    }

    private static String line(List<String> values) {
        return values.stream()
                .map(CsvExporter::escape)
                .collect(Collectors.joining(String.valueOf(SEPARATOR), "", LINE_END));
    }

    /**
     * Encloses a value in quotes if it contains a separator, a quote, or a line break.
     *
     * @param value field value
     * @return escaped value
     */
    static String escape(String value) {
        boolean special = value.indexOf(SEPARATOR) >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        return special ? '"' + value.replace("\"", "\"\"") + '"' : value;
    }
}
