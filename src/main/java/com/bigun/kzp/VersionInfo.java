package com.bigun.kzp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class VersionInfo {
    private VersionInfo() {
    }

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
