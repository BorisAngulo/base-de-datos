← [Índice](../MANUAL-SQL.md) · [← FK](08-claves-foraneas.md) · [Agregaciones →](10-agregaciones.md)

# 9. Combinar tablas (`JOIN`)

La FK guarda el enlace (`producto_id`). El `JOIN` **usa** ese enlace para
armar una fila con columnas de varias tablas.

## 9.1. `INNER JOIN`

Solo las filas que **hacen match** a ambos lados.

```sql
SELECT p.nombre,
       c.nombre AS categoria,
       p.precio,
       p.stock
FROM producto AS p
INNER JOIN categoria AS c ON c.id = p.categoria_id;
```

Un producto con `categoria_id` NULL **no aparece**.

## 9.2. `LEFT JOIN` (y `RIGHT`)

Todas las filas de la izquierda, aunque no haya padre.

```sql
SELECT p.nombre,
       c.nombre AS categoria
FROM producto AS p
LEFT JOIN categoria AS c ON c.id = p.categoria_id;
```

Si no hay categoría, `c.nombre` sale `NULL`.

`RIGHT JOIN` es el simétrico (todas las de la derecha). Casi siempre se
reescribe como `LEFT` cambiando el orden de las tablas.

## 9.3. `FULL OUTER JOIN`

Todas las de ambos lados. **PostgreSQL sí**, MySQL **no** (hasta versiones
recientes; en 8.0 clásico se simula con `UNION` de un `LEFT` y un `RIGHT`).

```sql
-- PostgreSQL
SELECT p.nombre, c.nombre AS categoria
FROM producto p
FULL OUTER JOIN categoria c ON c.id = p.categoria_id;
```

## 9.4. Tres tablas

```sql
SELECT v.id,
       cl.nombre  AS cliente,
       p.nombre   AS producto,
       v.cantidad,
       v.fecha
FROM venta    AS v
JOIN producto AS p  ON p.id  = v.producto_id
JOIN cliente  AS cl ON cl.id = v.cliente_id
ORDER BY v.fecha, v.id;
```

`JOIN` sin palabra = `INNER JOIN`.

## 9.5. La trampa del producto cartesiano

```sql
-- Casi nunca es lo que quieres
SELECT p.nombre, c.nombre
FROM producto p, categoria c;
```

Sin `ON`/`WHERE` de enlace, cada producto se combina con **cada** categoría.
Con 100 × 20 = 2000 filas inventadas. Usa siempre `JOIN ... ON`.

## 9.6. Filtrar después de juntar

```sql
SELECT cl.nombre, p.nombre, v.cantidad
FROM venta v
JOIN producto p  ON p.id  = v.producto_id
JOIN cliente  cl ON cl.id = v.cliente_id
WHERE v.fecha >= DATE '2026-01-01'     -- PostgreSQL; en MySQL: '2026-01-01'
  AND p.stock < 10;
```

El `WHERE` se aplica al resultado del `JOIN`.
Si filtras una columna de la tabla derecha en un `LEFT JOIN`, puedes
convertirlo accidentalmente en un `INNER` (las filas sin match tienen
`NULL` y caen). Para filtrar el padre, pon la condición en el `ON`:

```sql
FROM producto p
LEFT JOIN categoria c
       ON c.id = p.categoria_id
      AND c.nombre = 'Audio'
```

← [Índice](../MANUAL-SQL.md) · [← FK](08-claves-foraneas.md) · [Agregaciones →](10-agregaciones.md)
