← [Índice](../MANUAL-SQL.md) · [← Agregaciones](10-agregaciones.md) · [Vistas →](12-vistas.md)

# 11. Subconsultas

Un `SELECT` dentro de otro. Sirven cuando el filtro o el valor **dependen
de una consulta**, no de una constante.

## 11.1. En el `WHERE` (escalar)

Devuelve **una** fila y **una** columna.

```sql
SELECT nombre, precio
FROM producto
WHERE precio > (SELECT AVG(precio) FROM producto);
```

Si la subconsulta no devuelve fila, el valor es `NULL` y el `WHERE` no
cumple. Si devuelve **más de una** fila, el motor error
(`more than one row returned` / `Subquery returns more than 1 row`).

## 11.2. `IN` / `NOT IN`

La subconsulta devuelve una **lista**.

```sql
SELECT nombre
FROM producto
WHERE id IN (
    SELECT producto_id FROM venta
);

-- Productos que nunca se vendieron
SELECT nombre
FROM producto
WHERE id NOT IN (
    SELECT producto_id FROM venta
);
```

`NOT IN` con `NULL` en la lista se vuelve peligroso: toda la comparación
queda desconocida y el resultado puede salir vacío. Prefiere `NOT EXISTS`
si la subconsulta puede traer nulos.

## 11.3. `EXISTS`

“¿Hay al menos una fila?” No te importa el valor, solo la existencia.

```sql
SELECT c.nombre
FROM cliente c
WHERE EXISTS (
    SELECT 1
    FROM venta v
    WHERE v.cliente_id = c.id
      AND v.cantidad >= 2
);
```

La subconsulta se correlaciona: usa `c.id` de **afuera**. Se evalúa
(conceptualmente) por cada cliente.

## 11.4. En el `FROM`

La subconsulta actúa como tabla temporal. En PostgreSQL necesita alias;
en MySQL también.

```sql
SELECT g.categoria, g.articulos
FROM (
    SELECT categoria_id AS categoria, COUNT(*) AS articulos
    FROM producto
    GROUP BY categoria_id
) AS g
WHERE g.articulos >= 1;
```

## 11.5. En el `SELECT`

```sql
SELECT p.nombre,
       p.precio,
       (SELECT AVG(precio) FROM producto) AS promedio_general
FROM producto p;
```

Se calcula (a menudo) una vez y se replica. Útil para comparar cada fila
con un total.

## 11.6. Subconsulta vs. JOIN

Muchas subconsultas `IN` se reescriben como `JOIN`. El planificador suele
elegir bien. Escribe la que se lea más clara; mide si hay un millón de filas.

```sql
-- Equivalentes en intención
SELECT nombre FROM producto WHERE id IN (SELECT producto_id FROM venta);

SELECT DISTINCT p.nombre
FROM producto p
JOIN venta v ON v.producto_id = p.id;
```

← [Índice](../MANUAL-SQL.md) · [← Agregaciones](10-agregaciones.md) · [Vistas →](12-vistas.md)
