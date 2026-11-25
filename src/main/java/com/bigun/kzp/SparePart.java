package com.bigun.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Common immutable state and rules of the automobile spare parts stored in the warehouse.
 */
public abstract class SparePart {
    private static final int FIELD_COUNT = 5;

    private final PartKind kind;
    private final String sku;
    private final String name;
    private final int stock;
    private final double unitPrice;
    private final String supplier;

    /**
     * Creates the common part state and validates all of its fields.
     *
     * @param kind part kind
     * @param sku stock keeping unit
     * @param name part name
     * @param stock non-negative quantity in stock
     * @param unitPrice non-negative finite price per unit
     * @param supplier supplier name
     * @throws NullPointerException if the kind or a text field is null
     * @throws IllegalArgumentException if a field violates its invariant
     */
    protected SparePart(PartKind kind, String sku, String name, int stock, double unitPrice, String supplier) {
        // Перевірка завершується до створення об'єкта, тому частково створеного стану не буває.
        this(requireValid(kind, sku, name, stock, unitPrice, supplier),
                kind, sku, name, stock, unitPrice, supplier);
    }

    /**
     * Assigns values that are already validated.
     *
     * @param validated result of the validation
     * @param kind part kind
     * @param sku stock keeping unit
     * @param name part name
     * @param stock quantity in stock
     * @param unitPrice price per unit
     * @param supplier supplier name
     */
    private SparePart(boolean validated, PartKind kind, String sku, String name, int stock,
                      double unitPrice, String supplier) {
        this.kind = kind;
        this.sku = sku;
        this.name = name;
        this.stock = stock;
        this.unitPrice = unitPrice;
        this.supplier = supplier;
    }

    /**
     * Parses a semicolon-delimited CSV row into a validated part of the kind defined by its SKU.
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
        return PartKind.fromSku(sku).create(sku, name, stock, unitPrice, supplier);
    }

    /**
     * Returns the stock keeping unit.
     *
     * @return part SKU
     */
    public final String getSku() {
        return sku;
    }

    /**
     * Returns the part name.
     *
     * @return part name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the quantity in stock.
     *
     * @return stock quantity
     */
    public final int getStock() {
        return stock;
    }

    /**
     * Returns the price per unit.
     *
     * @return unit price
     */
    public final double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Returns the supplier name.
     *
     * @return supplier name
     */
    public final String getSupplier() {
        return supplier;
    }

    /**
     * Returns the part kind.
     *
     * @return part kind
     */
    public final PartKind getKind() {
        return kind;
    }

    /**
     * Calculates the warranty cost of the whole stock of this part.
     *
     * @return warranty cost
     */
    public abstract double warrantyCost();

    /**
     * Compares parts by subtype and SKU.
     *
     * @param other object to compare with
     * @return whether both objects have the same subtype and SKU
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return sku.equals(((SparePart) other).sku);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return hash code
     */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), sku);
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
     * Checks the common invariants of all kinds of parts.
     *
     * @param kind part kind
     * @param sku stock keeping unit
     * @param name part name
     * @param stock quantity in stock
     * @param unitPrice price per unit
     * @param supplier supplier name
     * @return true when all values are valid
     */
    static boolean requireValid(PartKind kind, String sku, String name, int stock,
                                        double unitPrice, String supplier) {
        Objects.requireNonNull(kind, "kind");
        requireFilled(sku, name, supplier);
        if (!Double.isFinite(unitPrice)) {
            throw new IllegalArgumentException("ціна має бути скінченним числом");
        }
        if (unitPrice < 0 || stock < 0) {
            throw new IllegalArgumentException("ціна та кількість не можуть бути від'ємними");
        }
        return true;
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
