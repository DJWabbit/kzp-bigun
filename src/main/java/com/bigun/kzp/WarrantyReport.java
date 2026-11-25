package com.bigun.kzp;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Formats the warranty cost of parts through their common type.
 */
public final class WarrantyReport {
    private WarrantyReport() {
    }

    /**
     * Formats the warranty cost of every part and the total.
     *
     * @param items parts of any kind
     * @return report text ending with a platform newline
     */
    public static String format(List<SparePart> items) {
        // Реалізацію warrantyCost обирає фактичний підтип.
        String lines = items.stream()
                .map(item -> String.format(Locale.ROOT, "- %s (%s): %.2f%n",
                        item.getSku(), item.getKind().getLabel(), item.warrantyCost()))
                .collect(Collectors.joining());
        double total = items.stream()
                .mapToDouble(SparePart::warrantyCost)
                .sum();
        return String.format(Locale.ROOT, "Гарантійна вартість%n%sРазом: %.2f%n", lines, total);
    }
}
