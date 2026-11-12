← [Índice](../MANUAL-SQL.md) · [← Vistas](12-vistas.md) · [Usuarios →](14-usuarios-y-privilegios.md)

# 13. Índices

Un índice es una estructura extra (casi siempre un árbol B) que permite
encontrar filas **sin barrer la tabla**. La PK ya crea uno.

## 13.1. Crear y borrar

```sql
CREATE INDEX idx_producto_stock ON producto (stock);
CREATE INDEX idx_producto_nombre ON producto (nombre);

DROP INDEX idx_producto_stock;                    -- PostgreSQL
DROP INDEX idx_producto_stock ON producto;        -- MySQL
```

Índice único (además de la PK):

```sql
CREATE UNIQUE INDEX uq_producto_nombre ON producto (nombre);
```

## 13.2. Cuándo ayuda

```sql
SELECT * FROM producto WHERE stock < 10;
SELECT * FROM producto WHERE id = 3;          -- ya cubierto por la PK
```

Un índice en `stock` puede servir al `WHERE stock < 10` si hay muchas
filas y pocas cumplen. Si casi todas cumplen, el motor ignora el índice
y barre la tabla (es más barato).

No indexar “por las dudas” cada columna: cada `INSERT`/`UPDATE` también
actualiza los índices.

## 13.3. Ver el plan

**PostgreSQL**

```sql
EXPLAIN ANALYZE
SELECT * FROM producto WHERE stock < 10;
```

Busca `Seq Scan` (barrido) vs `Index Scan` / `Bitmap Index Scan`.

**MySQL**

```sql
EXPLAIN
SELECT * FROM producto WHERE stock < 10;
```

`type: ALL` = barrido. `ref` / `range` = usa índice.

## 13.4. Índices compuestos

```sql
CREATE INDEX idx_venta_cliente_fecha ON venta (cliente_id, fecha);
```

Sirve para `WHERE cliente_id = 1` y para
`WHERE cliente_id = 1 AND fecha >= '2026-01-01'`.
Suele **no** servir para filtrar solo por `fecha` (el primer campo del
índice no está en el `WHERE`).

## 13.5. Relación con Base de Datos I

No hace falta diseñar índices avanzados (GiST, full-text, parciales).
Sí hace falta saber:

1. la PK ya es un índice.
2. las FK se benefician de un índice en la columna hija (`venta.producto_id`)
   para los `JOIN` y para borrar el padre.
3. `EXPLAIN` es la forma de **comprobar**, no de adivinar.

```sql
CREATE INDEX idx_venta_producto ON venta (producto_id);
CREATE INDEX idx_venta_cliente  ON venta (cliente_id);
```

← [Índice](../MANUAL-SQL.md) · [← Vistas](12-vistas.md) · [Usuarios →](14-usuarios-y-privilegios.md)
