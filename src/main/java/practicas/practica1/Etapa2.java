package practicas.practica1;

/** ETAPA 2: filtrado y recuperación. Modelo imperativo (Java) vs. declarativo (SQL). */
public final class Etapa2 {

    private Etapa2() {
    }

    public static void ejecutar() throws Exception {
        Practica1.titulo("ETAPA 2", "Filtrado y recuperación de información");
        InventarioCsv.restaurar();
        BaseDatos.asegurarServidor();
        BaseDatos.ejecutarArchivo("00_reset_catalogo.sql");

        Practica1.reto("Reto 1 (Script Java)", "Leer el CSV línea a línea y filtrar stock < 10");
        FiltrarStockBajo.ejecutar();

        Practica1.reto("Reto 2 (Consulta SQL)", "Una sola sentencia declarativa");
        BaseDatos.ejecutarArchivo("04_filtrar.sql");

        Practica1.reto("Reto 3 (Análisis de complejidad)", "1 millón de registros y 3 condiciones");
        System.out.println("""
                Java (imperativo):
                  Hay que abrir el archivo, saltar la cabecera, partir cada línea,
                  convertir texto a número y evaluar if (stock < 10). Con 1 millón
                  de registros el programa recorre SIEMPRE las 1e6 líneas (O(n)).
                  Tres condiciones distintas (precio, nombre, stock) se traducen en
                  más if, más conversiones y más código de manejo de errores. No hay
                  índice: filtrar por id también es un barrido completo.

                SQL (declarativo):
                  WHERE stock < 10 AND precio > 20 AND nombre LIKE '%USB%' describe
                  el resultado, no el algoritmo. El planificador puede usar un índice
                  en stock, rechazar filas temprano y no tocar el millón de filas
                  si hay selectividad. Añadir una condición es una cláusula, no un
                  nuevo bucle. El motor reutiliza estadísticas y caché.

                Conclusión: el CSV empuja la complejidad a la aplicación; la BD la
                absorbe en el motor, una vez, para todos los clientes.
                """);

        Practica1.reto("Reto 4 (Agregaciones)", "COUNT y AVG en una sola consulta");
        BaseDatos.ejecutarArchivo("05_agregaciones.sql");
        System.out.println("""
                Equivalente en Java leyendo el CSV:
                  int total = 0;
                  BigDecimal suma = BigDecimal.ZERO;
                  for (Producto p : InventarioCsv.leer()) {
                      total++;
                      suma = suma.add(p.precio());
                  }
                  BigDecimal promedio = suma.divide(BigDecimal.valueOf(total), 2, HALF_UP);

                Variables extra: acumuladores, contador, control del divisor (CSV
                vacío), conversión de tipos y un barrido completo sí o sí. En SQL
                el motor hace esa pasada internamente —o usa un índice— y devuelve
                dos números. No hay que programar el bucle ni decidir cómo redondear
                más allá de ROUND(AVG(precio), 2).
                """);
    }
}
