package practicas.practica1;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Lectura y escritura del almacén plano. No valida tipos ni reglas de negocio:
 * cualquier texto se puede guardar. La conversión ocurre al leer, en la aplicación.
 */
public final class InventarioCsv {

    public static final String CABECERA = "id,nombre,precio,stock";

    public static final List<Producto> CATALOGO_INICIAL = List.of(
            new Producto(1, "Auriculares Bluetooth Pulse X", new BigDecimal("59.99"), 25),
            new Producto(2, "Cargador USB-C GaN 65W", new BigDecimal("19.50"), 8),
            new Producto(3, "Mouse inalámbrico Orbit Mini", new BigDecimal("34.90"), 1)
    );

    private InventarioCsv() {
    }

    public static void crearCatalogoInicial() throws IOException {
        guardar(Config.csv(), CATALOGO_INICIAL);
        Files.copy(Config.csv(), Config.csvInicial(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void restaurar() throws IOException {
        Path inicial = Config.csvInicial();
        if (!Files.isRegularFile(inicial)) {
            crearCatalogoInicial();
            return;
        }
        Files.copy(inicial, Config.csv(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void guardar(List<Producto> productos) throws IOException {
        guardar(Config.csv(), productos);
    }

    public static void guardar(Path path, List<Producto> productos) throws IOException {
        Files.createDirectories(path.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append(CABECERA).append('\n');
        for (Producto producto : productos) {
            sb.append(producto.aLineaCsv()).append('\n');
        }
        Files.writeString(path, sb, StandardCharsets.UTF_8);
    }

    public static List<Producto> leer() throws IOException {
        return leer(Config.csv());
    }

    /**
     * Lee el CSV línea por línea y convierte texto a número.
     * Si una celda no tiene el tipo esperado, lanza NumberFormatException.
     */
    public static List<Producto> leer(Path path) throws IOException {
        List<String> lineas = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Producto> productos = new ArrayList<>();
        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).strip();
            if (linea.isEmpty()) {
                continue;
            }
            if (i == 0 && linea.equalsIgnoreCase(CABECERA)) {
                continue;
            }
            productos.add(parsearLinea(linea, i + 1));
        }
        return productos;
    }

    public static Producto parsearLinea(String linea, int numeroLinea) {
        String[] partes = linea.split(",", -1);
        if (partes.length < 4) {
            throw new IllegalArgumentException(
                    "Línea %d: se esperaban 4 columnas y hay %d: %s"
                            .formatted(numeroLinea, partes.length, linea));
        }
        try {
            int id = Integer.parseInt(partes[0].strip());
            String nombre = partes[1].strip();
            BigDecimal precio = new BigDecimal(partes[2].strip());
            int stock = Integer.parseInt(partes[3].strip());
            return new Producto(id, nombre, precio, stock);
        } catch (NumberFormatException e) {
            throw new NumberFormatException(
                    "Línea %d: no se pudo convertir '%s' (precio o stock no numérico): %s"
                            .formatted(numeroLinea, linea, e.getMessage()));
        }
    }

    public static Producto buscar(List<Producto> productos, int id) {
        return productos.stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el producto id=" + id));
    }

    public static List<Producto> reemplazar(List<Producto> productos, Producto actualizado) {
        List<Producto> copia = new ArrayList<>(productos.size());
        for (Producto producto : productos) {
            copia.add(producto.id() == actualizado.id() ? actualizado : producto);
        }
        return copia;
    }

    public static void inyectarLineaCorrupta() throws IOException {
        String extra = "4,Webcam HD Studio,GRATIS,-5\n";
        Files.writeString(
                Config.csv(),
                Files.readString(Config.csv(), StandardCharsets.UTF_8) + extra,
                StandardCharsets.UTF_8);
    }

    public static void vaciar() throws IOException {
        Files.writeString(Config.csv(), "", StandardCharsets.UTF_8);
    }

    public static String contenido() throws IOException {
        return Files.readString(Config.csv(), StandardCharsets.UTF_8);
    }
}
