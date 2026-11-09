package practicas.practica1;

import java.nio.file.Files;
import java.util.Arrays;

/**
 * Punto de entrada de la práctica 1: archivos planos vs. base de datos relacional.
 *
 * <pre>
 *   ./practicas/practica1/scripts/run.sh todo
 *   ./practicas/practica1/scripts/run.sh etapa4
 *   ./practicas/practica1/scripts/run.sh venta --cajero 1
 * </pre>
 */
public final class Practica1 {

    public static void main(String[] args) throws Exception {
        String comando = args.length == 0 ? "ayuda" : args[0];
        String[] resto = args.length <= 1 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);

        switch (comando) {
            case "todo" -> ejecutarTodo();
            case "etapa1" -> Etapa1.ejecutar();
            case "etapa2" -> Etapa2.ejecutar();
            case "etapa3" -> Etapa3.ejecutar();
            case "etapa4" -> Etapa4.ejecutar();
            case "etapa5" -> Etapa5.ejecutar();
            case "etapa6" -> Etapa6.ejecutar();
            case "etapa7" -> Etapa7.ejecutar();
            case "filtrar" -> FiltrarStockBajo.main(resto);
            case "venta" -> SimularVenta.main(resto);
            case "apagon" -> TransferenciaApagada.main(resto);
            case "vaciar" -> VaciarCsv.main(resto);
            case "ayuda", "-h", "--help" -> ayuda();
            default -> {
                System.err.println("Comando desconocido: " + comando);
                ayuda();
                System.exit(1);
            }
        }
    }

    static void ejecutarTodo() throws Exception {
        Files.createDirectories(Config.evidencias());
        Etapa1.ejecutar();
        Etapa2.ejecutar();
        Etapa3.ejecutar();
        Etapa4.ejecutar();
        Etapa5.ejecutar();
        Etapa6.ejecutar();
        Etapa7.ejecutar();
        titulo("FIN", "Las 7 etapas se ejecutaron. Informe en practicas/practica1/INFORME.md");
    }

    static void titulo(String etapa, String objetivo) {
        System.out.println();
        System.out.println("=".repeat(72));
        System.out.println("  " + etapa);
        System.out.println("  " + objetivo);
        System.out.println("=".repeat(72));
    }

    static void reto(String nombre, String descripcion) {
        System.out.println();
        System.out.println("── " + nombre + ": " + descripcion);
        System.out.println();
    }

    static void ayuda() {
        System.out.println("""
                Práctica 1 — Archivos planos vs. base de datos relacional
                Paquete Java: practicas.practica1

                Uso:
                  ./practicas/practica1/scripts/run.sh <comando>

                Comandos:
                  todo       Ejecuta las 7 etapas
                  etapa1 ... etapa7
                  filtrar    Etapa 2 / 3: recorre el CSV (falla si está corrupto)
                  venta      Etapa 4: simula un cajero  [--cajero N]
                  apagon     Etapa 5: transferencia CSV con cierre forzado
                  vaciar     Etapa 6: trunca inventario.csv
                  ayuda

                Antes de las etapas SQL: scripts/db.sh start
                """);
    }
}
