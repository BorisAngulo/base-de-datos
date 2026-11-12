← [Índice](../MANUAL-SQL.md) · [← Tablas](04-tablas-y-restricciones.md) · [SELECT →](06-consultas-simples.md)

# 5. Insertar, actualizar y borrar (DML)

Con la tabla creada, las filas entran, cambian o salen. El motor aplica tipos
y `CHECK` **en cada sentencia**.

## 5.1. INSERT

```sql
INSERT INTO categoria (nombre) VALUES
    ('Audio'),
    ('Energía'),
    ('Periféricos');

INSERT INTO cliente (nombre) VALUES
    ('Ana'),
    ('Luis');

INSERT INTO producto (nombre, precio, stock, categoria_id) VALUES
    ('Auriculares Bluetooth Pulse X', 59.99, 25, 1),
    ('Cargador USB-C GaN 65W',        19.50,  8, 2),
    ('Mouse inalámbrico Orbit Mini',  34.90,  1, 3);
```

Si omites una columna con `DEFAULT`, el motor la rellena (`stock`, `fecha`).
Si omites una `NOT NULL` sin default, falla.

Varias filas en un solo `INSERT` (como arriba) es una sola sentencia: o
entran todas, o no entra ninguna (dentro de una transacción; ver
[capítulo 15](15-transacciones.md)).

### Devolver el id generado

```sql
-- PostgreSQL
INSERT INTO producto (nombre, precio, stock)
VALUES ('Teclado', 45.00, 10)
RETURNING id;

-- MySQL
INSERT INTO producto (nombre, precio, stock)
VALUES ('Teclado', 45.00, 10);
SELECT LAST_INSERT_ID();
```

## 5.2. Cuando el motor dice que no

```sql
-- Tipo inválido
INSERT INTO producto (nombre, precio, stock)
VALUES ('Webcam HD Studio', 'GRATIS', 4);

-- PostgreSQL: invalid input syntax for type numeric  (SQLState 22P02)
-- MySQL:     Incorrect decimal value / truncated     (errno 1366 / 1292)

-- Regla de negocio
INSERT INTO producto (nombre, precio, stock)
VALUES ('Webcam HD Studio', 49.90, -5);

-- Ambos: CHECK constraint violated  (PostgreSQL 23514)
```

En un CSV esas dos líneas se guardarían. Aquí no llegan a persistir.

Clave duplicada:

```sql
INSERT INTO categoria (id, nombre) VALUES (1, 'Otra');
-- PostgreSQL: duplicate key value violates unique constraint
-- MySQL:      Duplicate entry '1' for key 'PRIMARY'
```

## 5.3. UPDATE

```sql
UPDATE producto
SET stock = stock - 1
WHERE id = 3;

UPDATE producto
SET precio = 17.90,
    stock  = 12
WHERE nombre = 'Cargador USB-C GaN 65W';
```

**Sin `WHERE` se actualizan todas las filas.** Es el error más caro del DML.
En duda, primero:

```sql
SELECT id, nombre, stock FROM producto WHERE id = 3;
```

Un `UPDATE` que deja `stock = -1` choca con el `CHECK` y **no cambia la fila**.

## 5.4. DELETE y TRUNCATE

```sql
DELETE FROM venta WHERE id = 1;

DELETE FROM producto WHERE stock = 0;   -- puede ser 0 filas, no es error
```

`TRUNCATE` vacía la tabla entera, más rápido, y en PostgreSQL no dispara
`WHERE`. A menudo exige privilegio aparte y falla si hay FK apuntando.

```sql
TRUNCATE TABLE venta;                 -- PostgreSQL: reinicia IDENTITY
TRUNCATE TABLE venta;                 -- MySQL InnoDB: también
```

No puedes `TRUNCATE producto` mientras `venta` la referencie.

## 5.5. INSERT … SELECT

Copiar o transformar:

```sql
INSERT INTO producto (nombre, precio, stock, categoria_id)
SELECT nombre, precio, 0, categoria_id
FROM producto
WHERE stock < 5;
```

(Ese ejemplo duplicaría nombres; es solo la forma de la sentencia.)

← [Índice](../MANUAL-SQL.md) · [← Tablas](04-tablas-y-restricciones.md) · [SELECT →](06-consultas-simples.md)
