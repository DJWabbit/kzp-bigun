package com.bigun.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private static final String HELP = """
            Використання: java -jar kzp-bigun-1.0-SNAPSHOT.jar [опції]
              --help             показати цю довідку
              --version          показати версію у форматі version+build
              --input <шлях>     CSV-файл (за замовчуванням data/input.csv)
              --output <шлях>    файл звіту (за замовчуванням out/report.txt)
            """;

    private Main() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException | IOException exception) {
            System.err.println("Помилка: " + exception.getMessage());
            System.exit(1);
        }
    }

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
    }
}
