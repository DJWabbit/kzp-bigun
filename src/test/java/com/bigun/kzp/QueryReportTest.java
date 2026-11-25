package com.bigun.kzp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryReportTest {
    @Test
    void formatsAllQueriesForSampleData() throws IOException {
        InventoryFileReader.ReadResult result = new InventoryFileReader().read(Path.of("data", "queries.csv"));

        assertTrue(result.errors().isEmpty());
        assertEquals(String.format("""
                Запити до складу%n\
                Залишок менше 10: TIM-008, AMT-330, LMP-055-R, FLT-014-R%n\
                Артикули: BRK-001, FLT-014-R, SPK-210, TIM-008, OIL-101-R, AMT-330, LMP-055-R, FLT-014-R%n\
                Вартість запасу за виробником:%n\
                - АвтоПостач: 31056.00%n\
                - Деталь+: 17060.00%n\
                - Мотор-Сервіс: 15800.00%n\
                Ціни: записів 8, сума 7296.25, середня 912.03, мінімальна 150.00, максимальна 2100.00%n\
                Топ-5 за ціною:%n\
                - Амортизатор (AMT-330, оригінальна): 2100.00%n\
                - Ремінь ГРМ (TIM-008, оригінальна): 2100.00%n\
                - Гальмівні колодки (BRK-001, оригінальна): 1250.50%n\
                - Олива моторна (OIL-101-R, замінник): 780.00%n\
                - Повітряний фільтр (FLT-014-R, замінник): 410.00%n"""), QueryReport.format(result.items()));
    }

    @Test
    void formatsEmptyListWithoutSpecialValues() {
        assertEquals(String.format("""
                Запити до складу%n\
                Залишок менше 10: немає%n\
                Артикули: немає%n\
                Вартість запасу за виробником: немає%n\
                Ціни: немає даних%n\
                Топ-5 за ціною: немає%n"""), QueryReport.format(List.of()));
    }

    @Test
    void formatsFoundAndMissingPart() {
        List<SparePart> parts = List.of(SparePart.fromCsv("FLT-014-R;Фільтр;25;320.00;Мотор-Сервіс"));

        assertEquals(String.format("Пошук FLT-014-R: Фільтр (FLT-014-R, замінник): 320.00%n"),
                QueryReport.formatSearch(parts, "FLT-014-R"));
        assertEquals(String.format("Пошук XXX: не знайдено%n"), QueryReport.formatSearch(parts, "XXX"));
    }

    @Test
    void percentSignInDataDoesNotBreakFormatting() {
        List<SparePart> parts = List.of(SparePart.fromCsv("A-%d;Деталь 5%;1;10;Знижка 100%"));

        assertTrue(QueryReport.format(parts).contains("- Знижка 100%: 10.00"));
        assertTrue(QueryReport.formatSearch(parts, "A-%d").contains("Деталь 5% (A-%d, оригінальна): 10.00"));
    }

    @Test
    void queryOptionsAreOffByDefault() {
        CliOptions options = CliOptions.parse(new String[0]);

        assertFalse(options.queries());
        assertTrue(options.find().isEmpty());
    }

    @Test
    void parsesQueryOptions() {
        CliOptions options = CliOptions.parse(new String[]{"--queries", "--find", "TIM-008"});

        assertTrue(options.queries());
        assertEquals(Optional.of("TIM-008"), options.find());
        assertThrows(IllegalArgumentException.class, () -> CliOptions.parse(new String[]{"--find"}));
    }
}
