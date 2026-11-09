package practicas.practica1;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Un producto del catálogo. En el CSV todo esto es texto; aquí ya tiene tipo. */
public record Producto(int id, String nombre, BigDecimal precio, int stock) {

    public Producto conStock(int nuevoStock) {
        return new Producto(id, nombre, precio, nuevoStock);
    }

    public String aLineaCsv() {
        return id + "," + nombre + "," + precio.setScale(2, RoundingMode.HALF_UP) + "," + stock;
    }

    @Override
    public String toString() {
        return "id=%d  nombre=%s  precio=%s  stock=%d"
                .formatted(id, nombre, precio.setScale(2, RoundingMode.HALF_UP), stock);
    }
}
