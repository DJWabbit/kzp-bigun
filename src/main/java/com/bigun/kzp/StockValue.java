package com.bigun.kzp;

/**
 * Immutable totals of the warehouse stock.
 *
 * @param totalStock total number of items, not negative
 * @param totalValue total value of the stock, finite and not negative
 */
public record StockValue(long totalStock, double totalValue) {
    /**
     * Checks the totals.
     *
     * @param totalStock total number of items, not negative
     * @param totalValue total value of the stock, finite and not negative
     * @throws IllegalArgumentException if a total violates its rule
     */
    public StockValue {
        if (totalStock < 0) {
            throw new IllegalArgumentException("Загальна кількість на складі не може бути від'ємною.");
        }
        if (!Double.isFinite(totalValue) || totalValue < 0) {
            throw new IllegalArgumentException("Загальна вартість має бути скінченним невід'ємним числом.");
        }
    }
}
