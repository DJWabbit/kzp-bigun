package com.bigun.kzp;

/**
 * Original spare part; the warranty costs 5% of the price of every unit.
 */
public final class OriginalPart extends SparePart {
    /**
     * Creates an original part.
     *
     * @param sku stock keeping unit without the {@code -R} suffix
     * @param name part name
     * @param stock non-negative quantity in stock
     * @param unitPrice non-negative finite price per unit
     * @param supplier supplier name
     * @throws NullPointerException if a text field is null
     * @throws IllegalArgumentException if a field violates its invariant
     */
    public OriginalPart(String sku, String name, int stock, double unitPrice, String supplier) {
        super(PartKind.ORIGINAL, sku, name, stock, unitPrice, supplier);
        if (sku.trim().endsWith("-R")) {
            throw new IllegalArgumentException("Оригінальна деталь не може мати суфікс -R");
        }
    }

    /**
     * Returns 5% of the unit price multiplied by the stock.
     *
     * @return warranty cost of the whole stock
     */
    @Override
    public double warrantyCost() {
        return getUnitPrice() * 0.05 * getStock();
    }
}
