package com.bigun.kzp;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Formats inventory statistics and input validation errors for display.
 */
public final class ReportFormatter {
    /**
     * Formats one report in a locale-independent representation.
     *
     * @param stats calculated inventory indicators
     * @param errors line-numbered input errors
     * @return report text ending with a platform newline
     */
    public String format(InventoryStats stats, List<String> errors) {
        StringBuilder report = new StringBuilder();
        report.append(String.format(Locale.ROOT, "Звіт: склад автозапчастин%n"));
        report.append(String.format(Locale.ROOT, "Кількість коректних записів: %d%n", stats.validRecordCount()));
        report.append(String.format(Locale.ROOT, "Загальна кількість одиниць на складі: %d%n", stats.totalStock()));
        report.append(String.format(Locale.ROOT, "Загальна вартість запасу: %.2f%n", stats.totalValue()));
        Optional<InventoryItem> mostExpensive = stats.mostExpensive();
        if (mostExpensive.isPresent()) {
            InventoryItem item = mostExpensive.orElseThrow();
            report.append(String.format(Locale.ROOT, "Найдорожча деталь: %s (артикул %s), ціна %.2f%n",
                    item.name(), item.sku(), item.unitPrice()));
        } else {
            report.append(String.format(Locale.ROOT, "Найдорожча деталь: немає коректних записів%n"));
        }
        report.append(String.format(Locale.ROOT, "Помилок у вхідних даних: %d%n", errors.size()));
        if (!errors.isEmpty()) {
            report.append(String.format(Locale.ROOT, "Список помилок:%n"));
            errors.forEach(error -> report.append("- ").append(error).append(System.lineSeparator()));
        }
        return report.toString();
    }
}
