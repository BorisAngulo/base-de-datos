package practicas.practica1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ETAPA 4: simulación de concurrencia. Dos cajeros sobre el CSV (condición de
 * carrera) frente a dos transacciones SQL (bloqueo de fila).
 */
public final class Etapa4 {

    private Etapa4() {
    }

    public static void ejecutar() throws Exception {
        Practica1.titulo("ETAPA 4", "Simulación de concurrencia y conflicto de actualización");
        carreraCsv();
        cajerosSql();
        System.out.println("""
                Para repetir el reto CSV a mano, abre dos terminales:

                  ./practicas/practica1/scripts/run.sh venta --cajero 1
                  ./practicas/practica1/scripts/run.sh venta --cajero 2

                Ambos leen stock=1, pulsas ENTER en los dos, y el último que escribe gana.
                """);
    }

    static void carreraCsv() throws Exception {
        Practica1.reto("Retos 1 y 2 (CSV)", "Dos cajeros leen stock=1 y confirman a la vez");
        InventarioCsv.restaurar();
        System.out.println("CSV restaurado. El mouse (id=3) debe tener stock=1:");
        System.out.println(InventarioCsv.contenido());

        CountDownLatch leido = new CountDownLatch(2);
        CountDownLatch confirmar = new CountDownLatch(1);
        CountDownLatch terminado = new CountDownLatch(2);
        AtomicReference<Exception> error = new AtomicReference<>();

        Thread cajero1 = new Thread(() -> correrCajero("1", leido, confirmar, terminado, error), "cajero-1");
        Thread cajero2 = new Thread(() -> correrCajero("2", leido, confirmar, terminado, error), "cajero-2");
        cajero1.start();
        cajero2.start();

        if (!leido.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Los cajeros no llegaron a leer el stock");
        }
        System.out.println("\nAmbos cajeros ya leyeron el stock. Confirmando las dos ventas...\n");
        confirmar.countDown();
        if (!terminado.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Los cajeros no terminaron de escribir");
        }
        if (error.get() != null) {
            throw error.get();
        }

        System.out.println("\nEstado final del CSV:");
        System.out.println(InventarioCsv.contenido());
        System.out.println("""
                Los dos cajeros vendieron 1 unidad partiendo de stock=1, pero el
                archivo queda en 0 (la última escritura gana). Se vendieron 2
                unidades que no existían: lost update clásico, sin bloqueo.
                """);
    }

    private static void correrCajero(
            String etiqueta,
            CountDownLatch leido,
            CountDownLatch confirmar,
            CountDownLatch terminado,
            AtomicReference<Exception> error) {
        try {
            SimularVenta.Argumentos opciones = new SimularVenta.Argumentos();
            opciones.cajero = etiqueta;
            opciones.confirmacion = () -> {
                try {
                    confirmar.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            };
            // Marca "ya leí" justo después de imprimir el stock: vendemos a través
            // de un wrapper que cuenta el latch en el momento correcto.
            venderConBarrera(opciones, leido);
        } catch (Exception e) {
            error.compareAndSet(null, e);
        } finally {
            terminado.countDown();
        }
    }

    private static void venderConBarrera(SimularVenta.Argumentos opciones, CountDownLatch leido)
            throws Exception {
        var productos = InventarioCsv.leer(opciones.csv);
        var producto = InventarioCsv.buscar(productos, opciones.productoId);
        int stockLeido = producto.stock();
        System.out.printf(
                "[Cajero %s] Producto '%s' stock leído = %d%n",
                opciones.cajero, producto.nombre(), stockLeido);
        leido.countDown();
        opciones.confirmacion.run();
        Producto vendido = producto.conStock(stockLeido - 1);
        InventarioCsv.guardar(opciones.csv, InventarioCsv.reemplazar(productos, vendido));
        System.out.printf(
                "[Cajero %s] Venta confirmada. Nuevo stock escrito = %d%n",
                opciones.cajero, vendido.stock());
    }

    static void cajerosSql() throws Exception {
        Practica1.reto("Retos 3 y 4 (SQL)", "Cajero 1 deja la transacción abierta; Cajero 2 espera el lock");
        BaseDatos.asegurarServidor();
        BaseDatos.ejecutarArchivo("00_reset_catalogo.sql");
        System.out.println("Stock inicial del mouse:");
        BaseDatos.consultar("SELECT id, nombre, stock FROM producto WHERE id = 3");

        CountDownLatch cajero1Actualizo = new CountDownLatch(1);
        CountDownLatch cajero1Commit = new CountDownLatch(1);
        CountDownLatch cajero2Termino = new CountDownLatch(1);
        AtomicReference<String> resultadoCajero2 = new AtomicReference<>();
        AtomicReference<SQLException> errorCajero2 = new AtomicReference<>();

        Thread t1 = new Thread(() -> {
            try (Connection conn = BaseDatos.conectar()) {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE producto SET stock = stock - 1 WHERE id = 3")) {
                    ps.executeUpdate();
                }
                System.out.println("Cajero 1: stock descontado, transacción ABIERTA (sin COMMIT).");
                BaseDatos.consultar(conn, "SELECT id, nombre, stock FROM producto WHERE id = 3");
                cajero1Actualizo.countDown();
                if (!cajero1Commit.await(15, TimeUnit.SECONDS)) {
                    conn.rollback();
                    throw new IllegalStateException("Timeout esperando la señal de COMMIT");
                }
                conn.commit();
                System.out.println("Cajero 1: COMMIT realizado.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "sql-cajero-1");

        Thread t2 = new Thread(() -> {
            try {
                if (!cajero1Actualizo.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Cajero 1 no llegó a actualizar");
                }
                System.out.println("Cajero 2: intentando descontar (puede quedar en espera del lock)...");
                long t0 = System.nanoTime();
                try (Connection conn = BaseDatos.conectar()) {
                    conn.setAutoCommit(false);
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE producto SET stock = stock - 1 WHERE id = 3")) {
                        ps.executeUpdate();
                    }
                    conn.commit();
                    long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
                    resultadoCajero2.set("COMMIT tras esperar " + ms + " ms");
                } catch (SQLException e) {
                    long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0);
                    errorCajero2.set(e);
                    resultadoCajero2.set("ERROR tras esperar " + ms + " ms: " + BaseDatos.describirError(e));
                }
            } catch (Exception e) {
                resultadoCajero2.set("fallo de coordinación: " + e.getMessage());
            } finally {
                cajero2Termino.countDown();
            }
        }, "sql-cajero-2");

        t1.start();
        t2.start();

        if (!cajero1Actualizo.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Cajero 1 no actualizó");
        }
        System.out.println("Pausa: el Cajero 2 debería estar bloqueado en el lock de fila...");
        Thread.sleep(2500);
        if (cajero2Termino.getCount() == 0) {
            System.out.println("AVISO: el Cajero 2 no esperó. ¿El aislamiento no bloqueó?");
        } else {
            System.out.println("Confirmado: Cajero 2 sigue esperando el lock.");
        }
        System.out.println("Cajero 1 confirma ahora.");
        cajero1Commit.countDown();
        t1.join(10_000);
        if (!cajero2Termino.await(10, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Cajero 2 no terminó tras el COMMIT");
        }

        System.out.println("Cajero 2: " + resultadoCajero2.get());
        if (errorCajero2.get() != null) {
            System.out.println("""
                    Tras el COMMIT del Cajero 1 el stock quedó en 0. El UPDATE del
                    Cajero 2 intentó dejar stock = -1 y chk_stock_no_negativo lo
                    rechazó. El lock evitó la carrera; el CHECK evitó el stock negativo.
                    """);
        }
        System.out.println("Stock final en la base:");
        BaseDatos.consultar("SELECT id, nombre, stock FROM producto WHERE id = 3");

        Path sqlCajero1 = Config.sql("08_transaccion_cajero1.sql");
        Path sqlCajero2 = Config.sql("09_transaccion_cajero2.sql");
        if (Files.isRegularFile(sqlCajero1) && Files.isRegularFile(sqlCajero2)) {
            System.out.println("Las mismas operaciones están en:");
            System.out.println("  " + sqlCajero1.getFileName() + "  (sesión 1, con pg_sleep)");
            System.out.println("  " + sqlCajero2.getFileName() + "  (sesión 2, espera el lock)");
        }
    }
}
