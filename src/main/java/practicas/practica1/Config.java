package practicas.practica1;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Rutas y parámetros de conexión de la práctica 1.
 * El paquete Java es {@code practicas.practica1}; los datos, SQL y evidencias
 * viven en {@code practicas/practica1} en la raíz del proyecto.
 */
public final class Config {

    public static final int PRODUCTO_MOUSE = 3;
    public static final int PRODUCTO_A = 1;
    public static final int PRODUCTO_B = 2;
    public static final int UMBRAL_STOCK = 10;
    public static final int TRANSFERENCIA = 5;

    public static final String ROL_AUXILIAR = "auxiliar_bodega";
    public static final String ROL_AUXILIAR_PASSWORD = "bodega123";

    private Config() {
    }

    public static Path proyectoRaiz() {
        Path cwd = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path cursor = cwd;
        while (cursor != null) {
            if (Files.isRegularFile(cursor.resolve("pom.xml"))) {
                return cursor;
            }
            cursor = cursor.getParent();
        }
        return cwd;
    }

    public static Path practica() {
        return proyectoRaiz().resolve("practicas").resolve("practica1");
    }

    public static Path csv() {
        return practica().resolve("datos").resolve("inventario.csv");
    }

    public static Path csvInicial() {
        return practica().resolve("datos").resolve("inventario_inicial.csv");
    }

    public static Path sql(String archivo) {
        return practica().resolve("sql").resolve(archivo);
    }

    public static Path respaldos() {
        return practica().resolve("respaldos");
    }

    public static Path evidencias() {
        return practica().resolve("evidencias");
    }

    public static Path script(String archivo) {
        return practica().resolve("scripts").resolve(archivo);
    }

    public static String pgHostTcp() {
        String host = env("PGHOST", "127.0.0.1");
        if (host.startsWith("/") || host.contains("pgsocket")) {
            return "127.0.0.1";
        }
        return host;
    }

    public static String pgHostPsql() {
        return env("PGHOST", practica().resolve(".pgsocket").toString());
    }

    public static String pgPort() {
        return env("PGPORT", "55432");
    }

    public static String pgUser() {
        return env("PGUSER", "practica");
    }

    public static String pgDatabase() {
        return env("PGDATABASE", "inventario");
    }

    public static String pgPassword() {
        return env("PGPASSWORD", "");
    }

    public static String jdbcUrl() {
        return "jdbc:postgresql://%s:%s/%s".formatted(pgHostTcp(), pgPort(), pgDatabase());
    }

    private static String env(String clave, String valorPorDefecto) {
        String valor = System.getenv(clave);
        return (valor == null || valor.isBlank()) ? valorPorDefecto : valor;
    }
}
