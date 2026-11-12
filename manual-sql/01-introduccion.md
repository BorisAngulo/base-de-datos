← [Índice](../MANUAL-SQL.md) · [Siguiente: Entorno →](02-entorno-y-conexion.md)

# 1. Introducción y modelo relacional

SQL es el lenguaje con el que **pides** cosas a un motor de base de datos.
No recorre un archivo línea a línea: describes el resultado y el SGBD decide
cómo obtenerlo.

## 1.1. Archivo plano vs. base relacional

| | Archivo CSV | Base relacional |
|--|-------------|-----------------|
| Definición | Una cabecera de texto | Esquema con tipos y restricciones |
| Un precio `"GRATIS"` | Se guarda | El motor lo rechaza |
| Dos cajeros a la vez | Último que escribe gana | Bloqueo / transacción |
| Permisos | Quien puede escribir el archivo, puede vaciarlo | `GRANT UPDATE (stock)` |
| Corte de luz a mitad de un traslado | Datos a medias | `ROLLBACK` deja todo como estaba |

## 1.2. Vocabulario

| Término relacional | En la práctica | Ejemplo |
|--------------------|----------------|---------|
| Relación | Tabla | `producto` |
| Tupla | Fila / registro | `(1, 'Mouse', 34.90, 1)` |
| Atributo | Columna / campo | `precio` |
| Dominio | Tipo permitido | `NUMERIC(10,2)` |
| Clave primaria | Identifica una fila | `id` |
| Clave foránea | Apunta a otra tabla | `categoria_id` |
| NULL | “no hay valor”, no es cero ni `''` | producto sin categoría |

## 1.3. Familias de sentencias

| Familia | Para qué | Ejemplos |
|---------|----------|----------|
| **DDL** | Definir estructura | `CREATE`, `ALTER`, `DROP` |
| **DML** | Manipular filas | `INSERT`, `UPDATE`, `DELETE`, `SELECT` |
| **DCL** | Permisos | `GRANT`, `REVOKE` |
| **TCL** | Transacciones | `BEGIN`, `COMMIT`, `ROLLBACK` |

`SELECT` a veces se llama DQL (consulta). En este manual queda con el DML.

## 1.4. PostgreSQL y MySQL

Ambos hablan SQL, pero no el mismo dialecto. Donde importan las diferencias
(tipos, `\c` vs `USE`, aislamiento por defecto, `AUTO_INCREMENT` vs `SERIAL`)
este manual muestra **los dos**. El estándar común se escribe una sola vez.

## 1.5. Catálogo del curso

A partir del capítulo 4 se usa este mini-mundo:

- `categoria` — tipos de producto
- `producto` — el inventario
- `cliente` — quién compra
- `venta` — una línea de venta (qué, a quién, cuántas unidades)

```
categoria 1──* producto 1──* venta *──1 cliente
```

← [Índice](../MANUAL-SQL.md) · [Siguiente: Entorno →](02-entorno-y-conexion.md)
