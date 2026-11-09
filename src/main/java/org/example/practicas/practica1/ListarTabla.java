package org.example.practicas.practica1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/**
 * Ejemplo sencillo: conecta a PostgreSQL o MySQL y muestra las columnas
 * y las tuplas de una tabla.
 *
 * Uso (IntelliJ: ejecutar esta clase, o por consola):
 *   mvn -q exec:java -Dexec.args=producto
 */
public class ListarTabla {

    // Cambia a "mysql" si tu motor es MySQL.
    static final String MOTOR = "postgres";

    static final String HOST = "127.0.0.1";
    static final String PUERTO_POSTGRES = "5432";
    static final String PUERTO_MYSQL = "3306";
    static final String BASE = "inventario";
    static final String USUARIO = "practica";
    static final String CLAVE = "";
    static final String TABLA_POR_DEFECTO = "producto";

    public static void main(String[] args) throws Exception {
        String tabla = args.length > 0 ? args[0] : TABLA_POR_DEFECTO;
        if (!tabla.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            System.err.println("Nombre de tabla inválido: " + tabla);
            return;
        }

        String url;
        String citada;
        if (MOTOR.equals("mysql")) {
            url = "jdbc:mysql://" + HOST + ":" + PUERTO_MYSQL + "/" + BASE;
            citada = "`" + tabla + "`";
        } else {
            url = "jdbc:postgresql://" + HOST + ":" + PUERTO_POSTGRES + "/" + BASE;
            citada = "\"" + tabla + "\"";
        }

        try (Connection conexion = DriverManager.getConnection(url, USUARIO, CLAVE);
             Statement consulta = conexion.createStatement();
             ResultSet filas = consulta.executeQuery("SELECT * FROM " + citada)) {

            ResultSetMetaData meta = filas.getMetaData();
            int columnas = meta.getColumnCount();

            System.out.println("Motor : " + MOTOR);
            System.out.println("Tabla : " + tabla);
            System.out.println();

            System.out.println("Columnas:");
            for (int i = 1; i <= columnas; i++) {
                System.out.printf("  %s (%s)%n",
                        meta.getColumnLabel(i),
                        meta.getColumnTypeName(i));
            }

            System.out.println();
            System.out.println("Tuplas:");
            int n = 0;
            while (filas.next()) {
                n++;
                System.out.print("  (");
                for (int i = 1; i <= columnas; i++) {
                    if (i > 1) {
                        System.out.print(", ");
                    }
                    System.out.print(filas.getString(i));
                }
                System.out.println(")");
            }
            System.out.println();
            System.out.println("Total: " + n + " tupla(s)");
        }
    }
}
