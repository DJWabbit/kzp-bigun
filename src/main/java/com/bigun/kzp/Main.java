package com.bigun.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Command-line application for generating an automobile-parts inventory report.
 */
public final class Main {
    private static final String HELP = """
            Використання: java -jar kzp-bigun-1.0.0.jar [опції]
              --help             показати цю довідку
              --version          показати версію у форматі version+build
              --input <шлях>     CSV-файл (за замовчуванням data/input.csv)
              --output <шлях>    файл звіту (за замовчуванням out/report.txt)
              --warranty         вивести в консоль гарантійну вартість деталей
              --queries          вивести в консоль результати запитів до складу
              --find <артикул>   знайти деталь за артикулом і вивести її в консоль
            """;

    private Main() {
    }

    /**
     * Parses arguments and runs the requested command.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException | IOException exception) {
            printError(exception.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints an error as UTF-8 so that Cyrillic text does not depend on the system charset.
     *
     * @param message error description
     */
    static void printError(String message) {
        String line = String.format(Locale.ROOT, "Помилка: %s%n", message);
        System.err.writeBytes(line.getBytes(StandardCharsets.UTF_8));
        System.err.flush();
    }

    /**
     * Executes the requested mode and writes the report to the console and to the output file.
     *
     * @param args command-line arguments
     * @throws IOException if the input, output, or version resource cannot be accessed
     */
    private static void run(String[] args) throws IOException {
        CliOptions options = CliOptions.parse(args);
        if (options.help()) {
            System.out.write(HELP.getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (options.version()) {
            System.out.println(VersionInfo.formattedVersion());
            return;
        }

        InventoryFileReader.ReadResult result = new InventoryFileReader().read(options.input());
        InventoryStats stats = InventoryStats.calculate(result.items());
        String report = new ReportFormatter().format(stats, result.errors());
        System.out.write(report.getBytes(StandardCharsets.UTF_8));
        System.out.flush();
        Path output = options.output();
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, report, StandardCharsets.UTF_8);
        StringBuilder details = new StringBuilder();
        if (options.warranty()) {
            details.append(WarrantyReport.format(result.items()));
        }
        if (options.queries()) {
            details.append(QueryReport.format(result.items()));
        }
        options.find().ifPresent(sku -> details.append(QueryReport.formatSearch(result.items(), sku)));
        System.out.write(details.toString().getBytes(StandardCharsets.UTF_8));
        System.out.flush();
    }
}
