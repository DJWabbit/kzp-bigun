package com.bigun.kzp;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Formats the results of {@link PartStreamQueries} for the console.
 */
public final class QueryReport {
    /** Parts with a smaller stock are reported as low stock. */
    public static final int LOW_STOCK_THRESHOLD = 10;
    /** Number of the most expensive parts in the report. */
    public static final int TOP_LIMIT = 5;

    private static final String NONE = "немає";

    private QueryReport() {
    }

    /**
     * Formats the results of the five collection queries.
     *
     * @param parts spare parts
     * @return report text ending with a platform newline
     */
    public static String format(List<SparePart> parts) {
        List<SparePart> lowStock = PartStreamQueries.findLowStock(parts, LOW_STOCK_THRESHOLD);
        String suppliers = PartStreamQueries.stockValueBySupplier(parts).entrySet().stream()
                .map(entry -> String.format(Locale.ROOT, "- %s: %.2f%n", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining());
        String top = PartStreamQueries.topExpensiveParts(parts, TOP_LIMIT).stream()
                .map(part -> String.format(Locale.ROOT, "- %s%n", describe(part)))
                .collect(Collectors.joining());
        return String.format(Locale.ROOT, "Запити до складу%n")
                + String.format(Locale.ROOT, "Залишок менше %d: %s%n", LOW_STOCK_THRESHOLD,
                        join(PartStreamQueries.extractSkus(lowStock)))
                + String.format(Locale.ROOT, "Артикули: %s%n", join(PartStreamQueries.extractSkus(parts)))
                + section("Вартість запасу за виробником", suppliers)
                + formatPrices(PartStreamQueries.priceStatistics(parts))
                + section("Топ-%d за ціною".formatted(TOP_LIMIT), top);
    }

    /**
     * Formats the result of the search by SKU.
     *
     * @param parts spare parts
     * @param sku requested SKU
     * @return report text ending with a platform newline
     */
    public static String formatSearch(List<SparePart> parts, String sku) {
        String result = PartStreamQueries.findBySku(parts, sku)
                .map(QueryReport::describe)
                .orElse("не знайдено");
        return String.format(Locale.ROOT, "Пошук %s: %s%n", sku, result);
    }

    /**
     * Formats price statistics; an empty set has no minimum or maximum to show.
     *
     * @param statistics price statistics
     * @return one report line
     */
    private static String formatPrices(DoubleSummaryStatistics statistics) {
        if (statistics.getCount() == 0) {
            return String.format(Locale.ROOT, "Ціни: немає даних%n");
        }
        return String.format(Locale.ROOT,
                "Ціни: записів %d, сума %.2f, середня %.2f, мінімальна %.2f, максимальна %.2f%n",
                statistics.getCount(), statistics.getSum(), statistics.getAverage(),
                statistics.getMin(), statistics.getMax());
    }

    private static String describe(SparePart part) {
        return String.format(Locale.ROOT, "%s (%s, %s): %.2f",
                part.getName(), part.getSku(), part.getKind().getLabel(), part.getUnitPrice());
    }

    private static String join(List<String> values) {
        return values.isEmpty() ? NONE : String.join(", ", values);
    }

    private static String section(String title, String lines) {
        return lines.isEmpty()
                ? String.format(Locale.ROOT, "%s: %s%n", title, NONE)
                : String.format(Locale.ROOT, "%s:%n%s", title, lines);
    }
}
