package com.biblioteca.notificacion.infraestructure.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class NeonDataSourceConfigTest {

    private final NeonDataSourceConfig config = new NeonDataSourceConfig();
    private final String directorioOriginal = System.getProperty("user.dir");

    @AfterEach
    void restaurarDirectorioDeTrabajo() {
        System.setProperty("user.dir", directorioOriginal);
    }

    @Test
    @SuppressWarnings("unchecked")
    void readEnvFileIgnoraComentariosYNormalizaComillas(@TempDir Path temp) throws Exception {
        Files.writeString(temp.resolve(".env.local"), String.join("\n",
                "# comentario",
                "",
                "DATABASE_URL_UNPOOLED='postgres://usuario:clave@host:5432/bd'",
                "DATABASE_URL=\"postgres://usuario:clave@host:5432/bd2\"",
                "SIN_SEPARADOR",
                "=VALOR_SIN_CLAVE",
                "OTRA=valor"
        ));
        System.setProperty("user.dir", temp.toString());

        Map<String, String> valores = (Map<String, String>) ReflectionTestUtils
                .invokeMethod(config, "readEnvFile");

        assertNotNull(valores);
        assertEquals("postgres://usuario:clave@host:5432/bd", valores.get("DATABASE_URL_UNPOOLED"));
        assertEquals("postgres://usuario:clave@host:5432/bd2", valores.get("DATABASE_URL"));
        assertEquals("valor", valores.get("OTRA"));
        assertNull(valores.get("SIN_SEPARADOR"));
    }

    @Test
    void envDirectoriesIncluyeElDirectorioDeTrabajo(@TempDir Path temp) {
        System.setProperty("user.dir", temp.toString());

        List<Path> directorios = ReflectionTestUtils.invokeMethod(config, "envDirectories");

        assertNotNull(directorios);
        assertTrue(directorios.size() > 0);
        assertEquals(temp.toAbsolutePath(), directorios.get(0).toAbsolutePath());
    }

    @Test
    void resolveDatabaseUrlLeeElArchivoCuandoNoHayVariablesDeEntorno(@TempDir Path temp)
            throws Exception {
        assumeTrue(isBlank(System.getenv("DATABASE_URL_UNPOOLED")));
        assumeTrue(isBlank(System.getenv("DATABASE_URL")));
        Files.writeString(temp.resolve(".env.local"),
                "DATABASE_URL_UNPOOLED=postgres://usuario:clave@localhost:5432/pruebas\n");
        System.setProperty("user.dir", temp.toString());

        String url = ReflectionTestUtils.invokeMethod(config, "resolveDatabaseUrl");

        assertEquals("postgres://usuario:clave@localhost:5432/pruebas", url);
    }

    @Test
    void isBlankDetectaNulosYTextosVacios() {
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(config, "isBlank", (String) null));
        assertTrue((Boolean) ReflectionTestUtils.invokeMethod(config, "isBlank", "   "));
        assertFalse((Boolean) ReflectionTestUtils.invokeMethod(config, "isBlank", "dato"));
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
