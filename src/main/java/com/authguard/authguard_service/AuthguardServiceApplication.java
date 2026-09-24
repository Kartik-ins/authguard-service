package com.authguard.authguard_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class AuthguardServiceApplication {

    public static void main(String[] args) {
        loadDotEnv();
        parseDatabaseUrlIfPresent();
        SpringApplication.run(AuthguardServiceApplication.class, args);
    }

    /**
     * Reads local .env file if present and sets System properties
     * for seamless Spring Boot property resolution.
     */
    private static void loadDotEnv() {
        File envFile = new File(".env");
        if (envFile.exists() && envFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int idx = line.indexOf('=');
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Automatically parses DATABASE_URL_POOLED or DATABASE_URL into
     * DB_URL (jdbc:postgresql://...), DB_USER, and DB_PASSWORD if provided in URI format.
     */
    private static void parseDatabaseUrlIfPresent() {
        String dbUrl = getPropertyOrEnv("DATABASE_URL_POOLED");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = getPropertyOrEnv("DATABASE_URL");
        }

        if (dbUrl != null && !dbUrl.isBlank() && !dbUrl.contains("your-neon-host")) {
            String cleanUrl = dbUrl.trim();
            if ((cleanUrl.startsWith("\"") && cleanUrl.endsWith("\"")) ||
                (cleanUrl.startsWith("'") && cleanUrl.endsWith("'"))) {
                cleanUrl = cleanUrl.substring(1, cleanUrl.length() - 1).trim();
            }

            if (cleanUrl.startsWith("postgres://")) {
                cleanUrl = "postgresql://" + cleanUrl.substring("postgres://".length());
            }

            if (cleanUrl.startsWith("postgresql://")) {
                try {
                    int protoEnd = "postgresql://".length();
                    int atIndex = cleanUrl.lastIndexOf('@');
                    if (atIndex > protoEnd) {
                        String userPass = cleanUrl.substring(protoEnd, atIndex);
                        int colonIndex = userPass.indexOf(':');
                        if (colonIndex != -1) {
                            String user = userPass.substring(0, colonIndex);
                            String pass = userPass.substring(colonIndex + 1);
                            System.setProperty("DB_USER", user);
                            System.setProperty("DB_PASSWORD", pass);
                        }
                        String hostPortPathQuery = cleanUrl.substring(atIndex + 1);
                        String jdbcUrl = "jdbc:postgresql://" + hostPortPathQuery;
                        System.setProperty("DB_URL", jdbcUrl);
                    }
                } catch (Exception ignored) {
                }
            } else if (cleanUrl.startsWith("jdbc:")) {
                System.setProperty("DB_URL", cleanUrl);
            }
        }
    }

    private static String getPropertyOrEnv(String key) {
        String val = System.getProperty(key);
        if (val == null || val.isBlank()) {
            val = System.getenv(key);
        }
        return val;
    }
}
