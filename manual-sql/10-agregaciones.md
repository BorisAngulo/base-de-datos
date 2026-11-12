← [Índice](../MANUAL-SQL.md) · [← JOIN](09-combinar-tablas.md) · [Subconsultas →](11-subconsultas.md)

# 10. Agregaciones

Una agregación resume **muchas filas en una** (o en una por grupo).
En Java harían falta acumuladores y un bucle; aquí es una sentencia.

## 10.1. Funciones

| Función | Qué calcula |
|---------|-------------|
| `COUNT(*)` | Número de filas |
| `COUNT(col)` | Filas cuyo `col` no es `NULL` |
| `SUM(col)` | Suma (ignora `NULL`) |
| `AVG(col)` | Promedio |
| `MIN(col)` / `MAX(col)` | Extremos |

```sql
SELECT COUNT(*)                  AS total_articulos,
       ROUND(AVG(precio), 2)     AS precio_promedio,
       SUM(stock)                AS unidades,
       MIN(precio)               AS mas_barato,
       MAX(precio)               AS mas_caro
FROM producto;
```

`ROUND` existe en ambos. En PostgreSQL `AVG` de `INTEGER` sale
`NUMERIC`; en MySQL puede salir `DECIMAL` o `DOUBLE` según tipos.

## 10.2. `GROUP BY`

Una fila de resultado **por cada valor** de la columna agrupada.

```sql
SELECT c.nombre                  AS categoria,
       COUNT(*)                  AS articulos,
       ROUND(AVG(p.precio), 2)   AS precio_promedio
FROM producto p
JOIN categoria c ON c.id = p.categoria_id
GROUP BY c.nombre
ORDER BY articulos DESC;
```

Regla: en el `SELECT` solo pueden ir

1. columnas del `GROUP BY`,
2. funciones de agregación,
3. expresiones que dependan solo de eso.

Esto es **inválido** (PostgreSQL lo rechaza; MySQL con `ONLY_FULL_GROUP_BY`
también):

```sql
SELECT nombre, COUNT(*) FROM producto GROUP BY categoria_id;
-- ¿qué nombre? hay varios por categoría
```

## 10.3. `HAVING`

`WHERE` filtra **filas** antes de agrupar.
`HAVING` filtra **grupos** después.

```sql
SELECT c.nombre, COUNT(*) AS articulos
FROM producto p
JOIN categoria c ON c.id = p.categoria_id
GROUP BY c.nombre
HAVING COUNT(*) >= 2;
```

No pongas `COUNT(*)` en el `WHERE`.

```sql
-- Filtrar productos baratos, y luego agrupar
SELECT categoria_id, AVG(precio)
FROM producto
WHERE precio < 50
GROUP BY categoria_id
HAVING AVG(precio) > 20;
```

## 10.4. `COUNT` y los `NULL`

```sql
SELECT COUNT(*)          AS filas,
       COUNT(categoria_id) AS con_categoria
FROM producto;
```

Si un producto no tiene categoría, entra en `COUNT(*)` y no en
`COUNT(categoria_id)`.

## 10.5. Agregar sobre un JOIN

```sql
SELECT cl.nombre,
       COUNT(*)           AS compras,
       SUM(v.cantidad)    AS unidades
FROM venta v
JOIN cliente cl ON cl.id = v.cliente_id
GROUP BY cl.nombre;
```

Cuidado: un `JOIN` que duplica filas **infla** el `SUM`. Agrupa al nivel
correcto o usa subconsultas ([capítulo 11](11-subconsultas.md)).

← [Índice](../MANUAL-SQL.md) · [← JOIN](09-combinar-tablas.md) · [Subconsultas →](11-subconsultas.md)
