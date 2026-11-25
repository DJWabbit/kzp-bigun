package com.bigun.kzp;

/**
 * Replacement spare part; the warranty costs 3% of the price, but at least 20 UAH per unit.
 */
public final class ReplacementPart extends SparePart {
    /**
     * Creates a replacement part.
     *
     * @param sku stock keeping unit with the {@code -R} suffix
     * @param name part name
     * @param stock non-negative quantity in stock
     * @param unitPrice non-negative finite price per unit
     * @param supplier supplier name
     * @throws NullPointerException if a text field is null
     * @throws IllegalArgumentException if a field violates its invariant
     */
    public ReplacementPart(String sku, String name, int stock, double unitPrice, String supplier) {
        super(PartKind.REPLACEMENT, sku, name, stock, unitPrice, supplier);
        if (!sku.trim().endsWith("-R")) {
            throw new IllegalArgumentException("Деталь-замінник повинна мати суфікс -R");
        }
    }

    /**
     * Returns the larger of 3% of the unit price and 20 UAH, multiplied by the stock.
     *
     * @return warranty cost of the whole stock
     */
    @Override
    public double warrantyCost() {
        double perUnit = Math.max(getUnitPrice() * 0.03, 20.0);
        return perUnit * getStock();
    }
}
