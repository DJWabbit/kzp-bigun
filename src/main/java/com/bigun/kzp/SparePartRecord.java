package com.bigun.kzp;

import java.util.List;

/**
 * Flat record of a spare part for CSV; equality covers every field.
 *
 * <p>The kind of the part is not stored: it is restored from the article.
 *
 * @param article stock keeping unit, the key of the record
 * @param name part name
 * @param quantity non-negative quantity in stock
 * @param price non-negative finite price per unit
 * @param manufacturer manufacturer name
 */
public record SparePartRecord(
        @CsvColumn("артикул") String article,
        @CsvColumn("назва") String name,
        @CsvColumn("кількість") int quantity,
        @CsvColumn("ціна") double price,
        @CsvColumn("виробник") String manufacturer) {
    private static final int FIELD_COUNT = 5;

    /**
     * Checks the values by the rules of {@link SparePart}.
     *
     * @param article stock keeping unit, the key of the record
     * @param name part name
     * @param quantity non-negative quantity in stock
     * @param price non-negative finite price per unit
     * @param manufacturer manufacturer name
     * @throws NullPointerException if a text value is null
     * @throws IllegalArgumentException if a value violates its rule
     */
    public SparePartRecord {
        SparePart.requireValid(PartKind.fromSku(article), article, name, quantity, price, manufacturer);
    }

    /**
     * Creates a record from a part of any kind.
     *
     * @param part spare part
     * @return flat record
     */
    public static SparePartRecord from(SparePart part) {
        return new SparePartRecord(part.getSku(), part.getName(), part.getStock(),
                part.getUnitPrice(), part.getSupplier());
    }

    /**
     * Restores a record from CSV fields in the export order.
     *
     * @param fields article, manufacturer, name, price, and quantity
     * @return restored record
     * @throws DataStorageException if the number of fields or a value is wrong
     */
    public static SparePartRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields.size() != FIELD_COUNT) {
            throw new DataStorageException(
                    "очікується %d полів, отримано %d".formatted(FIELD_COUNT, fields.size()));
        }
        try {
            // Текст не обрізається: відновлений запис має збігатися з початковим.
            return new SparePartRecord(fields.get(0), fields.get(2), Integer.parseInt(fields.get(4)),
                    Double.parseDouble(fields.get(3)), fields.get(1));
        } catch (NumberFormatException exception) {
            throw new DataStorageException("ціна та кількість мають бути числами", exception);
        } catch (IllegalArgumentException exception) {
            throw new DataStorageException(exception.getMessage(), exception);
        }
    }

    /**
     * Creates a part; its subtype is defined by the article.
     *
     * @return original or replacement part
     */
    public SparePart toPart() {
        return PartKind.fromSku(article).create(article, name, quantity, price, manufacturer);
    }
}
