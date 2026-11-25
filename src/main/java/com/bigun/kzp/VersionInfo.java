package com.bigun.kzp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads and formats version metadata bundled with the application.
 */
public final class VersionInfo {
    private VersionInfo() {
    }

    /**
     * Returns the application version in {@code version+build} form.
     *
     * @return formatted version
     * @throws IOException if the metadata resource is missing or unreadable
     */
    public static String formattedVersion() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = VersionInfo.class.getResourceAsStream("/version.properties")) {
            if (stream == null) {
                throw new IOException("Ресурс version.properties не знайдено.");
            }
            properties.load(stream);
        }
        String version = properties.getProperty("version");
        String build = properties.getProperty("build");
        if (version == null || version.isBlank() || build == null || build.isBlank()) {
            throw new IOException("У version.properties мають бути задані version і build.");
        }
        return version + "+" + build;
    }
}
