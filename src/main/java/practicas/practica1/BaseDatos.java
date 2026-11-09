package practicas.practica1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/** Acceso JDBC a PostgreSQL y ejecución de los scripts de {@code practicas/practica1/sql}. */
public final class BaseDatos {

    private BaseDatos() {
    }

    public static Connection conectar() throws SQLException {
        return conectar(Config.pgUser(), Config.pgPassword());
    }

    public static Connection conectar(String usuario, String password) throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", usuario);
        props.setProperty("password", password == null ? "" : password);
        props.setProperty("loginTimeout", "8");
        props.setProperty("connectTimeout", "8");
        props.setProperty("ApplicationName", "practica1");
        return DriverManager.getConnection(Config.jdbcUrl(), props);
    }

    public static void asegurarServidor() throws IOException, InterruptedException, SQLException {
        try (Connection ignored = conectar()) {
            return;
        } catch (SQLException primera) {
            Path dbSh = Config.script("db.sh");
            if (!Files.isExecutable(dbSh) && Files.isRegularFile(dbSh)) {
                dbSh.toFile().setExecutable(true);
            }
            if (!Files.isRegularFile(dbSh)) {
                throw new SQLException(
                        "No hay conexión JDBC y no existe " + dbSh + ": " + primera.getMessage(),
                        primera);
            }
            System.out.println("PostgreSQL no responde. Arrancando clúster local...");
            Process proceso = new ProcessBuilder(dbSh.toString(), "start")
                    .directory(Config.practica().toFile())
                    .inheritIO()
                    .start();
            int codigo = proceso.waitFor();
            if (codigo != 0) {
                throw new SQLException("scripts/db.sh start terminó con código " + codigo, primera);
            }
            esperarConexion(Duration.ofSeconds(10));
        }
    }

    public static void esperarConexion(Duration tope) throws SQLException, InterruptedException {
        long limite = System.nanoTime() + tope.toNanos();
        SQLException ultima = null;
        while (System.nanoTime() < limite) {
            try (Connection ignored = conectar()) {
                return;
            } catch (SQLException e) {
                ultima = e;
                Thread.sleep(200);
            }
        }
        throw new SQLException("No se pudo conectar a " + Config.jdbcUrl(), ultima);
    }

    public static void ejecutarSql(String sql) throws SQLException {
        try (Connection conn = conectar()) {
            ejecutarSql(conn, sql);
        }
    }

    public static void ejecutarSql(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) {
            boolean hayResultado = st.execute(sql);
            int lote = 0;
            while (true) {
                if (hayResultado) {
                    try (ResultSet rs = st.getResultSet()) {
                        imprimir(rs);
                    }
                } else if (st.getUpdateCount() != -1) {
                    System.out.println("  → filas afectadas: " + st.getUpdateCount());
                }
                hayResultado = st.getMoreResults();
                if (!hayResultado && st.getUpdateCount() == -1) {
                    break;
                }
                lote++;
                if (lote > 50) {
                    break;
                }
            }
        }
    }

    public static void ejecutarArchivo(String nombre) throws IOException, SQLException {
        ejecutarArchivo(conectar(), Config.sql(nombre), true);
    }

    public static void ejecutarArchivo(Connection conn, Path archivo, boolean cerrar)
            throws IOException, SQLException {
        try {
            for (String sentencia : partirSentencias(Files.readString(archivo, StandardCharsets.UTF_8))) {
                System.out.println("SQL> " + compactar(sentencia));
                ejecutarSql(conn, sentencia);
            }
        } finally {
            if (cerrar) {
                conn.close();
            }
        }
    }

    public static void consultar(String sql) throws SQLException {
        try (Connection conn = conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            imprimir(rs);
        }
    }

    public static void consultar(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            imprimir(rs);
        }
    }

    public static void imprimir(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        StringBuilder cabecera = new StringBuilder("  ");
        StringBuilder linea = new StringBuilder("  ");
        int[] anchos = new int[cols];
        for (int i = 0; i < cols; i++) {
            anchos[i] = Math.max(12, md.getColumnLabel(i + 1).length());
            cabecera.append(pad(md.getColumnLabel(i + 1), anchos[i])).append("  ");
            linea.append("-".repeat(anchos[i])).append("  ");
        }
        System.out.println(cabecera);
        System.out.println(linea);
        int filas = 0;
        while (rs.next()) {
            StringBuilder fila = new StringBuilder("  ");
            for (int i = 0; i < cols; i++) {
                String valor = rs.getString(i + 1);
                fila.append(pad(valor == null ? "NULL" : valor, anchos[i])).append("  ");
            }
            System.out.println(fila);
            filas++;
        }
        if (filas == 0) {
            System.out.println("  (sin filas)");
        }
    }

    public static String describirError(SQLException e) {
        return "SQLState=%s  errorCode=%d  mensaje=%s"
                .formatted(e.getSQLState(), e.getErrorCode(), e.getMessage().strip());
    }

    public static List<String> partirSentencias(String texto) {
        List<String> sentencias = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String linea : texto.split("\n", -1)) {
            String recortada = linea.strip();
            if (recortada.startsWith("--") || recortada.isEmpty()) {
                continue;
            }
            actual.append(linea).append('\n');
        }
        for (String trozo : actual.toString().split(";")) {
            String sentencia = trozo.strip();
            if (!sentencia.isEmpty()) {
                sentencias.add(sentencia);
            }
        }
        return sentencias;
    }

    private static String compactar(String sql) {
        return sql.replaceAll("\\s+", " ").strip();
    }

    private static String pad(String texto, int ancho) {
        if (texto.length() >= ancho) {
            return texto;
        }
        return texto + " ".repeat(ancho - texto.length());
    }
}
