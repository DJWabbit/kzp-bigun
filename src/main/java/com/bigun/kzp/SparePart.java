package com.bigun.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * An immutable automobile spare part stored in the warehouse.
 */
public final class SparePart {
    private static final int FIELD_COUNT = 5;

    private final String sku;
    private final String name;
    private final int stock;
    private final double unitPrice;
    private final String supplier;

    /**
     * Creates a spare part and validates all of its fields.
     *
     * @param sku stock keeping unit
     * @param name part name
     * @param stock non-negative quantity in stock
     * @param unitPrice non-negative finite price per unit
     * @param supplier supplier name
     * @throws NullPointerException if a text field is null
     * @throws IllegalArgumentException if a field violates its invariant
     */
    public SparePart(String sku, String name, int stock, double unitPrice, String supplier) {
        requireFilled(sku, name, supplier);
        if (!Double.isFinite(unitPrice)) {
            throw new IllegalArgumentException("ціна має бути скінченним числом");
        }
        if (unitPrice < 0 || stock < 0) {
            throw new IllegalArgumentException("ціна та кількість не можуть бути від'ємними");
        }
        this.sku = sku;
        this.name = name;
        this.stock = stock;
        this.unitPrice = unitPrice;
        this.supplier = supplier;
    }

    /**
     * Parses a semicolon-delimited CSV row into a validated spare part.
     *
     * @param line CSV row in {@code sku;name;stock;unitPrice;supplier} format
     * @return the parsed spare part
     * @throws NullPointerException if the row is null
     * @throws IllegalArgumentException if the row is blank, malformed, or contains invalid values
     */
    public static SparePart fromCsv(String line) {
        Objects.requireNonNull(line, "line");
        if (line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "очікується %d полів, отримано %d".formatted(FIELD_COUNT, fields.length));
        }
        String sku = fields[0].trim();
        String name = fields[1].trim();
        String supplier = fields[4].trim();
        // Текст перевіряється до чисел, щоб причина помилки збігалася з попередньою роботою.
        requireFilled(sku, name, supplier);

        final int stock;
        final double unitPrice;
        try {
            stock = Integer.parseInt(fields[2].trim());
            unitPrice = Double.parseDouble(fields[3].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("ціна та кількість мають бути числами", exception);
        }
        return new SparePart(sku, name, stock, unitPrice, supplier);
    }

    /**
     * Returns the stock keeping unit.
     *
     * @return part SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Returns the part name.
     *
     * @return part name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the quantity in stock.
     *
     * @return stock quantity
     */
    public int getStock() {
        return stock;
    }

    /**
     * Returns the price per unit.
     *
     * @return unit price
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Returns the supplier name.
     *
     * @return supplier name
     */
    public String getSupplier() {
        return supplier;
    }

    /**
     * Returns a locale-independent representation of this part.
     *
     * @return formatted part representation
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT,
                "SparePart{sku=\"%s\", name=\"%s\", stock=%d, unitPrice=%.2f, supplier=\"%s\"}",
                sku, name, stock, unitPrice, supplier);
    }

    /**
     * Checks the mandatory text fields; the only place where this rule is defined.
     *
     * @param sku stock keeping unit
     * @param name part name
     * @param supplier supplier name
     * @throws NullPointerException if a value is null
     * @throws IllegalArgumentException if a value is blank
     */
    private static void requireFilled(String sku, String name, String supplier) {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(supplier, "supplier");
        if (sku.isBlank() || name.isBlank() || supplier.isBlank()) {
            throw new IllegalArgumentException("назва, артикул і постачальник не можуть бути порожніми");
        }
    }
}
