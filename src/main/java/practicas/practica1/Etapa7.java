package practicas.practica1;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** ETAPA 7: respaldo estructurado y consolidación. */
public final class Etapa7 {

    private Etapa7() {
    }

    public static void ejecutar() throws Exception {
        Practica1.titulo("ETAPA 7", "Respaldo estructurado y consolidación");
        InventarioCsv.restaurar();
        BaseDatos.asegurarServidor();
        BaseDatos.ejecutarArchivo("00_reset_catalogo.sql");

        Practica1.reto("Reto 1 (Respaldo lógico SQL)", "pg_dump del inventario");
        Files.createDirectories(Config.respaldos());
        Path dump = Config.respaldos().resolve("inventario.dump.sql");
        Process dumpProc = new ProcessBuilder(
                "pg_dump",
                "-h", Config.pgHostPsql(),
                "-p", Config.pgPort(),
                "-U", Config.pgUser(),
                "-d", Config.pgDatabase(),
                "--no-owner",
                "--inserts",
                "-f", dump.toString())
                .inheritIO()
                .start();
        int codigo = dumpProc.waitFor();
        if (codigo != 0) {
            throw new IllegalStateException("pg_dump terminó con código " + codigo);
        }
        System.out.println("Dump escrito en " + dump);
        System.out.println("Tamaño: " + Files.size(dump) + " bytes");

        Practica1.reto("Reto 2 (Inspección del respaldo)", "DDL + DML frente al CSV");
        String contenido = Files.readString(dump, StandardCharsets.UTF_8);
        mostrarExtracto(contenido);
        System.out.println("El CSV, para comparar, solo contiene esto:");
        System.out.println(InventarioCsv.contenido());
        System.out.println("""
                El dump no es una foto opaca de bytes: es un script que recrea
                tipos, PRIMARY KEY, CHECK, y después inserta las filas. Restaurar
                es volver a ejecutar SQL. Copiar inventario.csv copia texto, pero
                no copia reglas, índices, roles ni transacciones a medio camino.
                """);

        Practica1.reto("Reto 3 (Análisis final)", "Tres casos para CSV y tres para la BD relacional");
        System.out.println(ANALISIS_FINAL);
    }

    static void mostrarExtracto(String dump) {
        System.out.println("Fragmentos relevantes del dump:");
        for (String marca : new String[]{
                "CREATE TABLE public.producto",
                "CONSTRAINT producto_pkey",
                "CONSTRAINT chk_stock_no_negativo",
                "INSERT INTO public.producto"
        }) {
            int i = dump.indexOf(marca);
            if (i < 0) {
                System.out.println("  (no se encontró: " + marca + ")");
                continue;
            }
            int fin = dump.indexOf('\n', i);
            if (fin < 0) {
                fin = Math.min(dump.length(), i + 120);
            }
            System.out.println("  " + dump.substring(i, fin).strip());
        }
    }

    static final String ANALISIS_FINAL = """
            Archivo plano conviene cuando:
              1. El dataset es un catálogo pequeño, de un solo escritor, que se
                 versiona en Git (configuración, lista de códigos, export CSV
                 para Excel). No hay cajeros concurrentes ni reglas de stock.
              2. Hay que intercambiar datos entre sistemas heterogéneos: un CSV
                 es el mínimo común denominador. La integridad se valida al
                 importar, no mientras el archivo viaja.
              3. Un script puntual (carga inicial, evidencia de práctica, log
                 append-only) donde abortar a mitad de camino no deja el negocio
                 inconsistente, o se puede regenerar desde la fuente.

            La base relacional es imprescindible cuando:
              1. Varios cajeros venden a la vez el mismo SKU. Sin lock ni
                 transacción, el CSV vende existencias que no hay (Etapa 4).
              2. El dominio tiene reglas (stock ≥ 0, tipos numéricos, PK) que
                 deben cumplirse para TODOS los clientes, no solo para el
                 programa Java que se acordó de validar (Etapas 1 y 3).
              3. Hay que separar deberes: el auxiliar de bodega actualiza stock
                 y no precios; un apagón no puede evaporar 5 unidades a mitad
                 de una transferencia; el respaldo debe recrear esquema y datos
                 de forma consistente (Etapas 5, 6 y 7).
            """;
}
