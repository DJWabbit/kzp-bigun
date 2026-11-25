package com.bigun.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * An immutable automobile spare part.
 */
public final class SparePart {
    private final String name;
    private final String sku;
    private final double unitPrice;
    private final int stock;
    private final String supplier;

    /**
     * Creates a spare part and validates all of its fields.
     *
     * @param name part name
     * @param sku part stock keeping unit
     * @param unitPrice non-negative finite price per unit
     * @param stock non-negative quantity in stock
     * @param supplier supplier name
     * @throws NullPointerException if a text field is null
     * @throws IllegalArgumentException if a field violates its invariant
     */
    public SparePart(String name, String sku, double unitPrice, int stock, String supplier) {
        this.name = Objects.requireNonNull(name, "name");
        this.sku = Objects.requireNonNull(sku, "sku");
        this.supplier = Objects.requireNonNull(supplier, "supplier");
        if (this.name.isBlank() || this.sku.isBlank() || this.supplier.isBlank()) {
            throw new IllegalArgumentException("назва, артикул і постачальник не можуть бути порожніми");
        }
        if (!Double.isFinite(unitPrice)) {
            throw new IllegalArgumentException("ціна має бути скінченним числом");
        }
        if (unitPrice < 0 || stock < 0) {
            throw new IllegalArgumentException("ціна та кількість не можуть бути від'ємними");
        }
        this.unitPrice = unitPrice;
        this.stock = stock;
    }

    /**
     * Parses a semicolon-delimited CSV row into a validated spare part.
     *
     * @param line CSV row in {@code name;sku;unitPrice;stock;supplier} format
     * @return the parsed spare part
     * @throws NullPointerException if the row is null
     * @throws IllegalArgumentException if the row is malformed or contains invalid values
     */
    public static SparePart fromCsv(String line) {
        Objects.requireNonNull(line, "line");
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("очікується 5 полів, отримано " + fields.length);
        }
        if (fields[0].trim().isEmpty() || fields[1].trim().isEmpty() || fields[4].trim().isEmpty()) {
            throw new IllegalArgumentException("назва, артикул і постачальник не можуть бути порожніми");
        }

        final double unitPrice;
        final int stock;
        try {
            unitPrice = Double.parseDouble(fields[2].trim());
            stock = Integer.parseInt(fields[3].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("ціна та кількість мають бути числами", exception);
        }

        return new SparePart(fields[0].trim(), fields[1].trim(), unitPrice, stock, fields[4].trim());
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
     * Returns the stock keeping unit.
     *
     * @return part SKU
     */
    public String getSku() {
        return sku;
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
     * Returns the quantity in stock.
     *
     * @return stock quantity
     */
    public int getStock() {
        return stock;
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
                "SparePart{name=\"%s\", sku=\"%s\", unitPrice=%.2f, stock=%d, supplier=\"%s\"}",
                name, sku, unitPrice, stock, supplier);
    }
}
