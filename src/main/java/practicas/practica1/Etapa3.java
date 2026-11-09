package practicas.practica1;

import java.sql.SQLException;

/** ETAPA 3: integridad de datos y validación de tipos. */
public final class Etapa3 {

    private Etapa3() {
    }

    public static void ejecutar() throws Exception {
        Practica1.titulo("ETAPA 3", "Prueba de integridad de datos y validación de tipos");
        InventarioCsv.restaurar();
        BaseDatos.asegurarServidor();
        BaseDatos.ejecutarArchivo("00_reset_catalogo.sql");

        Practica1.reto("Reto 1 (Inyección de error en CSV)", "Texto en precio y stock negativo");
        InventarioCsv.inyectarLineaCorrupta();
        System.out.println("CSV después de insertar la línea corrupta:");
        System.out.println(InventarioCsv.contenido());
        System.out.println("El sistema de archivos aceptó el registro sin protestar.");

        Practica1.reto("Reto 2 (Fallo en la aplicación)", "Reejecutar el filtro de stock bajo");
        try {
            FiltrarStockBajo.ejecutar();
            System.out.println("INESPERADO: el script no falló.");
        } catch (NumberFormatException e) {
            System.out.println("ERROR al convertir tipos en el CSV:");
            System.out.println("  " + e.getMessage());
            System.out.println("""
                    El archivo plano NO rechazó el dato. Quien paga el costo es la
                    aplicación, y solo si el programador convirtió tipos. Un lector
                    que trate todo como String ni siquiera se enteraría.
                    """);
        }

        Practica1.reto("Reto 3 (Protección de dominio SQL)", "INSERT con precio 'GRATIS' y stock -5");
        try {
            BaseDatos.ejecutarArchivo("06_dato_corrupto.sql");
            System.out.println("INESPERADO: PostgreSQL aceptó el registro corrupto.");
        } catch (SQLException e) {
            System.out.println("El motor rechazó el INSERT:");
            System.out.println("  " + BaseDatos.describirError(e));
            System.out.println("  Tipo inválido: el dominio NUMERIC no admite la cadena GRATIS.");
        }

        Practica1.reto("Reto 4 (Violación de regla de negocio)", "Precio válido, stock negativo");
        try {
            BaseDatos.ejecutarArchivo("07_stock_negativo.sql");
            System.out.println("INESPERADO: se insertó stock negativo.");
        } catch (SQLException e) {
            System.out.println("La restricción CHECK bloqueó la operación:");
            System.out.println("  " + BaseDatos.describirError(e));
            System.out.println("  chk_stock_no_negativo vive en el esquema, no en el código cliente.");
        }

        InventarioCsv.restaurar();
        System.out.println("CSV restaurado al catálogo inicial.");
    }
}
