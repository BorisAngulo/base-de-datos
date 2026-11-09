package practicas.practica1;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Etapa 2, Reto 1 / Etapa 3, Reto 2: lectura <em>imperativa</em> del CSV.
 * Recorre el archivo línea por línea, convierte texto a número e imprime
 * solo los productos con stock menor a 10. Si el archivo está corrupto,
 * el error sale a la superficie: el CSV no protege los tipos.
 */
public final class FiltrarStockBajo {

    public static int ejecutar() throws IOException {
        System.out.println("Leyendo " + Config.csv() + " línea por línea...");
        int encontrados = 0;
        int numeroLinea = 1;
        try (BufferedReader lector = Files.newBufferedReader(Config.csv(), StandardCharsets.UTF_8)) {
            String cabecera = lector.readLine();
            if (cabecera == null) {
                throw new IOException("El CSV está vacío");
            }
            String linea;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                Producto producto = InventarioCsv.parsearLinea(linea, numeroLinea);
                if (producto.stock() < Config.UMBRAL_STOCK) {
                    encontrados++;
                    System.out.printf(
                            "  id=%d  nombre=%s  precio=%s  stock=%d%n",
                            producto.id(),
                            producto.nombre(),
                            producto.precio().setScale(2, java.math.RoundingMode.HALF_UP),
                            producto.stock());
                }
            }
        }
        System.out.println("Productos con stock < " + Config.UMBRAL_STOCK + ": " + encontrados);
        return encontrados;
    }

    public static void main(String[] args) throws Exception {
        ejecutar();
    }
}
