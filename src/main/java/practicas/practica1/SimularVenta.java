package practicas.practica1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Etapa 4, Retos 1 y 2: simula una venta sobre {@code inventario.csv}.
 * Lee el stock, espera confirmación, resta 1 y guarda. No hay bloqueo de
 * archivo ni transacción: dos cajeros pueden leer el mismo stock y
 * sobrescribirse.
 *
 * <pre>
 *   java ... practicas.practica1.SimularVenta --cajero 1
 *   java ... practicas.practica1.SimularVenta --cajero 2
 * </pre>
 */
public final class SimularVenta {

    public static void main(String[] args) throws Exception {
        Argumentos opciones = Argumentos.parsear(args);
        vender(opciones);
    }

    public static void vender(Argumentos opciones) throws IOException, InterruptedException {
        List<Producto> productos = InventarioCsv.leer(opciones.csv);
        Producto producto = InventarioCsv.buscar(productos, opciones.productoId);
        int stockLeido = producto.stock();
        System.out.printf(
                "[Cajero %s] Producto '%s' stock leído = %d%n",
                opciones.cajero, producto.nombre(), stockLeido);

        marcarListo(opciones);

        if (stockLeido < 1) {
            System.out.printf("[Cajero %s] No hay existencias. Abortando.%n", opciones.cajero);
            return;
        }

        esperarConfirmacion(opciones);

        Producto vendido = producto.conStock(stockLeido - 1);
        InventarioCsv.guardar(opciones.csv, InventarioCsv.reemplazar(productos, vendido));
        System.out.printf(
                "[Cajero %s] Venta confirmada. Nuevo stock escrito = %d%n",
                opciones.cajero, vendido.stock());
    }

    private static void esperarConfirmacion(Argumentos opciones) throws InterruptedException, IOException {
        if (opciones.waitFile != null) {
            System.out.printf("[Cajero %s] Esperando señal en %s ...%n", opciones.cajero, opciones.waitFile);
            while (!Files.exists(opciones.waitFile)) {
                Thread.sleep(50);
            }
            return;
        }
        if (opciones.confirmacion != null) {
            opciones.confirmacion.run();
            return;
        }
        System.out.printf("[Cajero %s] Pulsa ENTER para confirmar la venta...%n", opciones.cajero);
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        }
    }

    private static void marcarListo(Argumentos opciones) throws IOException {
        if (opciones.readyFile != null) {
            Files.writeString(opciones.readyFile, "ready\n");
        }
    }

    public static final class Argumentos {
        String cajero = "1";
        int productoId = Config.PRODUCTO_MOUSE;
        Path csv = Config.csv();
        Path waitFile;
        Path readyFile;
        Runnable confirmacion;

        static Argumentos parsear(String[] args) {
            Argumentos opciones = new Argumentos();
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--cajero" -> opciones.cajero = args[++i];
                    case "--id" -> opciones.productoId = Integer.parseInt(args[++i]);
                    case "--csv" -> opciones.csv = Path.of(args[++i]);
                    case "--wait-file" -> opciones.waitFile = Path.of(args[++i]);
                    case "--ready-file" -> opciones.readyFile = Path.of(args[++i]);
                    default -> throw new IllegalArgumentException("Argumento desconocido: " + args[i]);
                }
            }
            return opciones;
        }
    }
}
