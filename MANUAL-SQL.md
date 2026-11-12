# Manual de SQL — Base de Datos I

Guía de curso para **PostgreSQL** y **MySQL**. El catálogo de ejemplo es
`inventario` (productos, categorías, ventas).

Lee los capítulos **en orden**: cada uno asume el anterior.
Los archivos viven en [`manual-sql/`](manual-sql/).

---

## Índice

| # | Capítulo | Qué vas a poder hacer al terminarlo |
|---|----------|-------------------------------------|
| 1 | [Introducción y modelo relacional](manual-sql/01-introduccion.md) | Distinguir archivo plano vs. SGBD; hablar de tabla, tupla, atributo y clave. |
| 2 | [Entorno, conexión y bases de datos](manual-sql/02-entorno-y-conexion.md) | Conectarte con `psql`/`mysql`, crear y borrar una base, entender host y puerto. |
| 3 | [Tipos de datos](manual-sql/03-tipos-de-datos.md) | Elegir `INTEGER`, `NUMERIC`, `TEXT`/`VARCHAR`, fechas y nulos. |
| 4 | [Tablas y restricciones](manual-sql/04-tablas-y-restricciones.md) | Crear, alterar y borrar tablas con PK, `NOT NULL`, `UNIQUE`, `CHECK` y `DEFAULT`. |
| 5 | [Insertar, actualizar y borrar](manual-sql/05-insertar-actualizar-borrar.md) | Cargar el catálogo y ver cómo el motor rechaza datos corruptos. |
| 6 | [Consultas simples](manual-sql/06-consultas-simples.md) | Listar, filtrar, ordenar y limitar filas con `SELECT`. |
| 7 | [Predicados y funciones](manual-sql/07-predicados-y-funciones.md) | Usar `LIKE`, `IN`, `BETWEEN`, `COALESCE`, fechas y conversiones. |
| 8 | [Claves foráneas](manual-sql/08-claves-foraneas.md) | Relacionar tablas y definir qué pasa al borrar un padre. |
| 9 | [Combinar tablas (JOIN)](manual-sql/09-combinar-tablas.md) | Armar un resultado con datos de dos o más tablas. |
| 10 | [Agregaciones](manual-sql/10-agregaciones.md) | Contar, sumar y promediar con `GROUP BY` y `HAVING`. |
| 11 | [Subconsultas](manual-sql/11-subconsultas.md) | Anidar un `SELECT` dentro de otro. |
| 12 | [Vistas](manual-sql/12-vistas.md) | Guardar una consulta con nombre y consultarla como tabla. |
| 13 | [Índices](manual-sql/13-indices.md) | Acelerar búsquedas y ver el coste de escribir. |
| 14 | [Usuarios y privilegios](manual-sql/14-usuarios-y-privilegios.md) | Crear roles, conceder/revocar permisos y probarlos cambiando de usuario. |
| 15 | [Transacciones y concurrencia](manual-sql/15-transacciones.md) | `BEGIN`/`COMMIT`/`ROLLBACK`, ACID, aislamiento y bloqueos. |
| 16 | [Respaldo y restauración](manual-sql/16-respaldo.md) | Generar un dump y recuperarlo. |

---

## Cómo está ordenado

```
entorno → tipos → tablas → datos → consultas → relaciones →
agregar → subconsultas → vistas → índices → permisos →
transacciones → respaldo
```

Los **permisos** van después de las tablas porque `GRANT` se aplica a objetos
que ya existen. Las **transacciones** van al final del SQL de curso: hacen
falta `UPDATE` y dos sesiones para entenderlas.

Fuera de Base de Datos I (no están aquí): disparadores, procedimientos
almacenados, funciones de ventana, CTE recursivos, replicación.