package com.biblioteca.prestamos.infraestructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Misma base de datos Neon que auth y catalogo. Se usa la cadena directa
 * (DATABASE_URL_UNPOOLED) porque Hibernate ejecuta DDL al arrancar.
 */
@Configuration
public class NeonDataSourceConfig {

    private static final String ENV_FILE = ".env.local";
    private static final String ENV_FILE_FALLBACK = ".env";

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource() {
        URI databaseUri = URI.create(resolveDatabaseUrl());

        String[] credentials = databaseUri.getUserInfo() == null
                ? new String[0]
                : databaseUri.getUserInfo().split(":", 2);

        if (credentials.length != 2) {
            throw new IllegalStateException("DATABASE_URL debe incluir usuario y password");
        }

        String jdbcUrl = "jdbc:postgresql://" + databaseUri.getHost()
                + (databaseUri.getPort() > 0 ? ":" + databaseUri.getPort() : "")
                + databaseUri.getPath()
                + "?sslmode=require";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(URLDecoder.decode(credentials[0], StandardCharsets.UTF_8));
        config.setPassword(URLDecoder.decode(credentials[1], StandardCharsets.UTF_8));
        config.setMaximumPoolSize(5);
        config.setPoolName("neon-prestamos-pool");

        return new HikariDataSource(config);
    }

    private String resolveDatabaseUrl() {
        String url = System.getenv("DATABASE_URL_UNPOOLED");
        if (isBlank(url)) {
            url = System.getenv("DATABASE_URL");
        }

        if (isBlank(url)) {
            Map<String, String> envFile = readEnvFile();
            url = envFile.get("DATABASE_URL_UNPOOLED");
            if (isBlank(url)) {
                url = envFile.get("DATABASE_URL");
            }
        }

        if (isBlank(url)) {
            throw new IllegalStateException(
                    "No se encontro DATABASE_URL. Ejecuta 'neon deploy' para generar .env.local "
                            + "o define DATABASE_URL_UNPOOLED en el entorno.");
        }

        return url;
    }

    private Map<String, String> readEnvFile() {
        Map<String, String> values = new HashMap<>();

        for (Path directory : envDirectories()) {
            for (String fileName : List.of(ENV_FILE, ENV_FILE_FALLBACK)) {
                Path path = directory.resolve(fileName);
                if (!Files.isRegularFile(path)) {
                    continue;
                }

                try {
                    for (String line : Files.readAllLines(path)) {
                        String entry = line.trim();
                        if (entry.isEmpty() || entry.startsWith("#")) {
                            continue;
                        }

                        int separator = entry.indexOf('=');
                        if (separator <= 0) {
                            continue;
                        }

                        String key = entry.substring(0, separator).trim();
                        String value = entry.substring(separator + 1).trim();

                        if (value.length() >= 2
                                && ((value.startsWith("\"") && value.endsWith("\""))
                                || (value.startsWith("'") && value.endsWith("'")))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        values.putIfAbsent(key, value);
                    }
                } catch (IOException exception) {
                    throw new IllegalStateException("No se pudo leer " + path, exception);
                }
            }
        }

        return values;
    }

    /**
     * Busca .env.local en el directorio de trabajo y, ademas, junto a las clases
     * compiladas (target/classes) y en la raiz del modulo. Asi funciona tanto al
     * ejecutar el jar desde su carpeta como al arrancar desde el IDE.
     */
    private List<Path> envDirectories() {
        Set<Path> directories = new LinkedHashSet<>();

        String userDir = System.getProperty("user.dir");
        if (!isBlank(userDir)) {
            directories.add(Path.of(userDir));
        }

        try {
            Path codeLocation = Path.of(NeonDataSourceConfig.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .toAbsolutePath();

            Path current = Files.isDirectory(codeLocation) ? codeLocation : codeLocation.getParent();

            // target/classes -> target -> raiz del modulo
            for (int level = 0; current != null && level < 3; level++) {
                directories.add(current);
                current = current.getParent();
            }
        } catch (Exception ignored) {
            // Sin acceso a la ubicacion de las clases: queda el directorio de trabajo.
        }

        return new ArrayList<>(directories);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
