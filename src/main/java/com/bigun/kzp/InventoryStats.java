package com.bigun.kzp;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record InventoryStats(int validRecordCount, StockValue stockValue,
                             Optional<SparePart> mostExpensive) {
    public InventoryStats {
        if (validRecordCount < 0) {
            throw new IllegalArgumentException("Кількість коректних записів не може бути від'ємною.");
        }
        Objects.requireNonNull(stockValue, "stockValue");
        Objects.requireNonNull(mostExpensive, "mostExpensive");
    }

    public static InventoryStats calculate(List<SparePart> items) {
        long stock = 0;
        double value = 0;
        for (SparePart item : items) {
            stock += item.getStock();
            value += item.getUnitPrice() * item.getStock();
        }
        Optional<SparePart> mostExpensive = items.stream()
                .max(Comparator.comparingDouble(SparePart::getUnitPrice));
        return new InventoryStats(items.size(), new StockValue(stock, value), mostExpensive);
    }
}
