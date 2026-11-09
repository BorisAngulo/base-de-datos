package practicas.practica1;

import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** ETAPA 6: segregación de accesos. Permiso de archivo vs. privilegios de columna. */
public final class Etapa6 {

    private Etapa6() {
    }

    public static void ejecutar() throws Exception {
        Practica1.titulo("ETAPA 6", "Segregación de accesos y seguridad a nivel de datos");
        destruccionPlana();
        rolYPrivilegios();
    }

    static void destruccionPlana() throws Exception {
        Practica1.reto("Reto 1 (Destrucción por permisos planos)", "Quien puede escribir puede vaciar");
        InventarioCsv.restaurar();
        var perms = PosixFilePermissions.toString(Files.getPosixFilePermissions(Config.csv()));
        System.out.println("Permisos POSIX de inventario.csv: " + perms);
        System.out.println("Cualquier proceso con escritura puede truncar el archivo entero.");
        VaciarCsv.ejecutar();
        System.out.println("""
                No hay forma, en un CSV, de conceder "solo actualizar la columna stock".
                chmod u+w / chmod a+w abre la puerta a borrar, reescribir precios o
                sustituir el catálogo. El sistema de archivos no entiende columnas.
                """);
        InventarioCsv.restaurar();
        System.out.println("CSV restaurado para no dejar la práctica sin catálogo.");
    }

    static void rolYPrivilegios() throws Exception {
        Practica1.reto("Retos 2 y 3 (Rol SQL)", "Crear auxiliar_bodega con SELECT y UPDATE(stock)");
        BaseDatos.asegurarServidor();
        BaseDatos.ejecutarArchivo("00_reset_catalogo.sql");
        borrarAuxiliarSiExiste();
        BaseDatos.ejecutarArchivo("11_crear_rol.sql");
        System.out.println("Rol auxiliar_bodega creado. Privilegio de columna: UPDATE (stock).");

        Practica1.reto("Reto 4 (Test de seguridad)", "UPDATE stock (ok) y UPDATE precio (denegado)");
        try (Connection conn = BaseDatos.conectar(Config.ROL_AUXILIAR, Config.ROL_AUXILIAR_PASSWORD)) {
            System.out.println("Sesión abierta como " + Config.ROL_AUXILIAR);

            System.out.println("\n1) UPDATE producto SET stock = stock + 1 WHERE id = 2");
            try {
                BaseDatos.ejecutarSql(conn, "UPDATE producto SET stock = stock + 1 WHERE id = 2");
                System.out.println("  Permitido: el auxiliar puede tocar stock.");
                BaseDatos.consultar(conn, "SELECT id, nombre, precio, stock FROM producto WHERE id = 2");
            } catch (SQLException e) {
                System.out.println("  INESPERADO al actualizar stock: " + BaseDatos.describirError(e));
            }

            System.out.println("\n2) UPDATE producto SET precio = 1.00 WHERE id = 2");
            try {
                BaseDatos.ejecutarSql(conn, "UPDATE producto SET precio = 1.00 WHERE id = 2");
                System.out.println("  INESPERADO: pudo cambiar el precio.");
            } catch (SQLException e) {
                System.out.println("  Denegado, como se esperaba:");
                System.out.println("  " + BaseDatos.describirError(e));
                System.out.println("""
                        El motor aplica GRANT UPDATE (stock) a nivel de columna.
                        El mismo usuario no puede alterar precio, nombre ni id.
                        Eso no tiene equivalente en un archivo plano.
                        """);
            }
        }
    }

    /** Permite reejecutar la etapa: DROP ROLE falla si aún hay privilegios concedidos. */
    static void borrarAuxiliarSiExiste() throws SQLException {
        try (Connection conn = BaseDatos.conectar(); Statement st = conn.createStatement()) {
            boolean existe;
            try (var rs = st.executeQuery(
                    "SELECT 1 FROM pg_roles WHERE rolname = '" + Config.ROL_AUXILIAR + "'")) {
                existe = rs.next();
            }
            if (!existe) {
                return;
            }
            st.execute("DROP OWNED BY " + Config.ROL_AUXILIAR);
            st.execute("DROP ROLE " + Config.ROL_AUXILIAR);
        }
    }
}
