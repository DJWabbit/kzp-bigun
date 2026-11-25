package com.bigun.kzp;

public record StockValue(long totalStock, double totalValue) {
    public StockValue {
        if (totalStock < 0) {
            throw new IllegalArgumentException("Загальна кількість на складі не може бути від'ємною.");
        }
        if (!Double.isFinite(totalValue) || totalValue < 0) {
            throw new IllegalArgumentException("Загальна вартість має бути скінченним невід'ємним числом.");
        }
    }
}
