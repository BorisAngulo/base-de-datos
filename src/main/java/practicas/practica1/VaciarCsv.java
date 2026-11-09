package practicas.practica1;

import java.nio.file.Files;

/**
 * Etapa 6, Reto 1: un usuario con permiso de escritura puede destruir el archivo.
 * En un archivo plano, "escribir" implica poder truncar, reescribir o borrar
 * el contenido completo. No existe un privilegio de "solo actualizar stock".
 */
public final class VaciarCsv {

    public static void ejecutar() throws Exception {
        System.out.println("Vaciando " + Config.csv() + " ...");
        InventarioCsv.vaciar();
        System.out.println("Tamaño resultante: " + Files.size(Config.csv()) + " bytes");
        System.out.println("El archivo sigue existiendo, pero ya no tiene catálogo.");
    }

    public static void main(String[] args) throws Exception {
        ejecutar();
    }
}
