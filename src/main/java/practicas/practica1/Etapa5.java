package practicas.practica1;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** ETAPA 5: interrupción forzada y atomicidad. */
public final class Etapa5 {

    private Etapa5() {
    }

    public static void ejecutar() throws Exception {
        Practica1.titulo("ETAPA 5", "Interrupción forzada del sistema y atomicidad");
        apagonCsv();
        falloSql();
    }

    static void apagonCsv() throws Exception {
        Practica1.reto("Retos 1 y 2 (CSV)", "Transferir 5 unidades A→B y apagar a mitad de camino");
        InventarioCsv.restaurar();
        try {
            TransferenciaApagada.ejecutar();
        } catch (TransferenciaApagada.ApagonSimuladoException e) {
            System.out.println("\n" + e.getMessage());
        }

        Practica1.reto("Reto 2 (Pérdida de datos)", "¿Dónde están las 5 unidades?");
        System.out.println("CSV después del apagón:");
        System.out.println(InventarioCsv.contenido());
        var productos = InventarioCsv.leer();
        var a = InventarioCsv.buscar(productos, Config.PRODUCTO_A);
        var b = InventarioCsv.buscar(productos, Config.PRODUCTO_B);
        System.out.printf(
                "A pasó de 25 a %d (−%d). B sigue en %d. Las unidades no están en ningún lado.%n",
                a.stock(), Config.TRANSFERENCIA, b.stock());
        System.out.println("Un archivo plano no tiene transacción: cada save() es definitivo.");
    }

    static void falloSql() throws Exception {
        Practica1.reto("Retos 3 y 4 (SQL)", "BEGIN, restar a A, inducir división por cero, ROLLBACK");
        InventarioCsv.restaurar();
        BaseDatos.asegurarServidor();
        BaseDatos.ejecutarArchivo("00_reset_catalogo.sql");

        try (Connection conn = BaseDatos.conectar(); Statement st = conn.createStatement()) {
            conn.setAutoCommit(false);
            st.executeUpdate("UPDATE producto SET stock = stock - 5 WHERE id = 1");
            System.out.println("Después de restar 5 al producto A (aún sin COMMIT):");
            BaseDatos.consultar(conn, "SELECT id, nombre, stock FROM producto WHERE id IN (1, 2) ORDER BY id");

            try {
                System.out.println("Induciendo error crítico: UPDATE ... stock + (5 / 0) ...");
                st.executeUpdate("UPDATE producto SET stock = stock + (5 / 0) WHERE id = 2");
                conn.commit();
                System.out.println("INESPERADO: la división entre cero no abortó.");
            } catch (SQLException e) {
                System.out.println("Error inducido: " + BaseDatos.describirError(e));
                conn.rollback();
                System.out.println("ROLLBACK ejecutado. Toda la transacción se deshizo.");
            }
        }

        System.out.println("Stock persistido (nueva conexión, lo que sobrevivió al 'apagón'):");
        BaseDatos.consultar("SELECT id, nombre, stock FROM producto WHERE id IN (1, 2) ORDER BY id");
        System.out.println("""
                El producto A volvió a 25. La resta nunca se confirmó. Eso es
                atomicidad: o ocurren las dos operaciones, o no ocurre ninguna.
                El script equivalente está en sql/10_atomicidad_fallo.sql.
                """);
    }
}
