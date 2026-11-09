# Práctica 1 — Archivos planos vs. base de datos relacional

El enunciado pedía Python para los scripts del CSV. Este es un proyecto **Java**,
así que toda la parte de archivos planos se implementó en el paquete
`practicas.practica1`. Las sentencias SQL se conservan en `sql/` para ejecutarlas
también a mano en `psql`.

## Qué hay aquí

| Ruta | Contenido |
|------|-----------|
| `src/main/java/practicas/practica1/` | Código Java (etapas 1–7) |
| `practicas/practica1/datos/` | `inventario.csv` y copia inicial |
| `practicas/practica1/sql/` | Scripts de PostgreSQL por reto |
| `practicas/practica1/scripts/` | Clúster local y lanzador |
| `practicas/practica1/respaldos/` | Dump generado en la etapa 7 |
| `practicas/practica1/INFORME.md` | Observaciones y análisis final |

## Requisitos

- JDK 26 (el del proyecto; en esta máquina: `~/.jdks/openjdk-26.0.1`)
- Maven
- PostgreSQL 18 (`psql`, `initdb`, `pg_ctl`, `pg_dump`)

No hace falta un PostgreSQL de sistema. `scripts/db.sh` crea un clúster en
`.pgdata` (puerto **55432**, usuario `practica`, base `inventario`).

## Cómo ejecutar

Desde la raíz del repositorio:

```bash
chmod +x practicas/practica1/scripts/*.sh
./practicas/practica1/scripts/run.sh todo
```

Eso arranca PostgreSQL, compila y recorre las 7 etapas. La salida se copia a
`evidencias/salida_completa.txt`.

Por etapa, o los programas sueltos:

```bash
./practicas/practica1/scripts/run.sh etapa1
./practicas/practica1/scripts/run.sh filtrar
./practicas/practica1/scripts/run.sh venta --cajero 1   # terminal A
./practicas/practica1/scripts/run.sh venta --cajero 2   # terminal B
```

En IntelliJ: ejecutar `practicas.practica1.Practica1` con argumento de programa
`todo` (o `etapa4`, `venta --cajero 1`, …).

Consola SQL a mano:

```bash
./practicas/practica1/scripts/db.sh start
./practicas/practica1/scripts/db.sh psql -f practicas/practica1/sql/01_crear_tabla.sql
```

## Mapa de retos

1. Crear CSV, `CREATE TABLE` con PK y `CHECK (stock >= 0)`, `INSERT`, inspeccionar.
2. Filtrar stock &lt; 10 en Java (imperativo) y en SQL (declarativo); `COUNT`/`AVG`.
3. Meter `GRATIS` y stock negativo en el CSV; el mismo `INSERT` en PostgreSQL.
4. Dos cajeros sobre el CSV (lost update) vs. dos transacciones (lock + CHECK).
5. Transferencia A→B con “apagón” en el CSV vs. `ROLLBACK` atómico.
6. Vaciar el CSV con permiso de escritura vs. rol `auxiliar_bodega` (solo `stock`).
7. `pg_dump` y tres casos de uso para cada modelo.
