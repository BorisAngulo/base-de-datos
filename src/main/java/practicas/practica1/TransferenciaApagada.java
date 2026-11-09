package practicas.practica1;

import java.io.IOException;
import java.util.List;

/**
 * Etapa 5, Retos 1 y 2: transfiere 5 unidades de A hacia B en el CSV y
 * simula un apagón a mitad de camino. El archivo se guarda después de
 * restar a A y antes de sumar a B: las unidades desaparecen.
 */
public final class TransferenciaApagada {

    public static final class ApagonSimuladoException extends RuntimeException {
        public ApagonSimuladoException(String mensaje) {
            super(mensaje);
        }
    }

    public static void ejecutar() throws IOException {
        List<Producto> productos = InventarioCsv.leer();
        Producto a = InventarioCsv.buscar(productos, Config.PRODUCTO_A);
        Producto b = InventarioCsv.buscar(productos, Config.PRODUCTO_B);

        System.out.println("Estado ANTES de la transferencia:");
        System.out.println("  A  " + a);
        System.out.println("  B  " + b);

        if (a.stock() < Config.TRANSFERENCIA) {
            throw new IllegalStateException("El producto A no tiene stock suficiente");
        }

        Producto aDescontado = a.conStock(a.stock() - Config.TRANSFERENCIA);
        InventarioCsv.guardar(InventarioCsv.reemplazar(productos, aDescontado));
        System.out.printf("%nSe restaron %d unidades a A y se GUARDÓ el CSV.%n", Config.TRANSFERENCIA);
        System.out.println("Simulando apagón antes de acreditar a B...");

        throw new ApagonSimuladoException(
                "APAGÓN SIMULADO: el proceso se detiene a mitad de la transferencia. "
                        + "Las " + Config.TRANSFERENCIA + " unidades desaparecieron del producto A "
                        + "y nunca llegaron al producto B.");

        // Inalcanzable a propósito: es lo que un apagón impide ejecutar.
        // Producto bAcreditado = b.conStock(b.stock() + Config.TRANSFERENCIA);
        // InventarioCsv.guardar(InventarioCsv.reemplazar(productos, bAcreditado));
    }

    public static void main(String[] args) throws Exception {
        ejecutar();
    }
}
