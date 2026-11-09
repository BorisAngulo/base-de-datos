package practicas.practica1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

/** ETAPA 1: creación del almacén de datos e inserción del catálogo inicial. */
public final class Etapa1 {

    private Etapa1() {
    }

    public static void ejecutar() throws Exception {
        Practica1.titulo("ETAPA 1", "Creación del almacén de datos e inserción del catálogo inicial");

        Practica1.reto("Reto 1 (Archivo plano)", "Crear inventario.csv con cabecera y tres productos");
        InventarioCsv.crearCatalogoInicial();
        System.out.println("Escrito " + Config.csv());
        System.out.println(InventarioCsv.contenido());

        Practica1.reto("Reto 2 (Base de datos)", "CREATE TABLE producto con tipos, PK y CHECK(stock >= 0)");
        BaseDatos.asegurarServidor();
        BaseDatos.ejecutarArchivo("01_crear_tabla.sql");

        Practica1.reto("Reto 3 (Inserción SQL)", "INSERT de los mismos tres productos y SELECT");
        BaseDatos.ejecutarArchivo("02_insertar.sql");
        BaseDatos.ejecutarArchivo("03_consultar.sql");

        Practica1.reto("Reto 4 (Inspección)", "Metadatos físicos del CSV vs. esquema de la tabla");
        inspeccionarCsv();
        inspeccionarTabla();
        System.out.println("""

                Comparación:
                  CSV  → la "definición" es solo la primera línea de texto. El sistema de
                         archivos no sabe qué es un precio ni un stock: guarda bytes.
                  SQL  → el esquema vive en el catálogo del motor (pg_catalog). Tipos,
                         clave primaria y CHECK son metadatos que el motor aplica en
                         cada INSERT/UPDATE, no un comentario en un archivo.
                """);
    }

    static void inspeccionarCsv() throws Exception {
        Path csv = Config.csv();
        BasicFileAttributes attrs = Files.readAttributes(csv, BasicFileAttributes.class);
        System.out.println("Archivo: " + csv.toAbsolutePath());
        System.out.println("  tamaño          = " + attrs.size() + " bytes");
        System.out.println("  creado          = " + attrs.creationTime());
        System.out.println("  modificado      = " + attrs.lastModifiedTime());
        System.out.println("  último acceso   = " + attrs.lastAccessTime());
        System.out.println("  es regular      = " + attrs.isRegularFile());
        try {
            PosixFileAttributes posix = Files.readAttributes(csv, PosixFileAttributes.class);
            System.out.println("  propietario     = " + posix.owner().getName());
            System.out.println("  grupo           = " + posix.group().getName());
            System.out.println("  permisos        = " + PosixFilePermissions.toString(posix.permissions()));
        } catch (UnsupportedOperationException ignored) {
            // Windows u otro FS sin POSIX.
        }
        System.out.println();
        System.out.println("Comando del sistema operativo (`stat`):");
        new ProcessBuilder("stat", csv.toString()).inheritIO().start().waitFor();
    }

    static void inspeccionarTabla() throws Exception {
        System.out.println();
        System.out.println("Esquema (information_schema + pg_constraint):");
        BaseDatos.consultar("""
                SELECT column_name, data_type, numeric_precision, numeric_scale, is_nullable
                FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'producto'
                ORDER BY ordinal_position
                """);
        BaseDatos.consultar("""
                SELECT conname AS constraint_name,
                       pg_get_constraintdef(oid) AS definicion
                FROM pg_constraint
                WHERE conrelid = 'public.producto'::regclass
                """);
        System.out.println();
        System.out.println("Descripción en la consola de PostgreSQL (`\\d producto`):");
        ejecutarPsql("\\d producto");
    }

    static void ejecutarPsql(String comando) throws Exception {
        Process proceso = new ProcessBuilder(
                "psql",
                "-h", Config.pgHostPsql(),
                "-p", Config.pgPort(),
                "-U", Config.pgUser(),
                "-d", Config.pgDatabase(),
                "-c", comando)
                .inheritIO()
                .start();
        int codigo = proceso.waitFor();
        if (codigo != 0) {
            System.out.println("psql terminó con código " + codigo + " (se continúa con JDBC).");
        }
    }
}
