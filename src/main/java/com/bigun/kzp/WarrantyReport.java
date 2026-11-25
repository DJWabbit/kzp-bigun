package com.bigun.kzp;

import java.util.List;
import java.util.Locale;

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
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "Гарантійна вартість%n"));
        double total = 0;
        for (SparePart item : items) {
            // Реалізацію warrantyCost обирає фактичний підтип.
            double cost = item.warrantyCost();
            total += cost;
            report.append(String.format(Locale.ROOT, "- %s (%s): %.2f%n",
                    item.getSku(), item.getKind().getLabel(), cost));
        }
        report.append(String.format(Locale.ROOT, "Разом: %.2f%n", total));
        return report.toString();
    }
}
