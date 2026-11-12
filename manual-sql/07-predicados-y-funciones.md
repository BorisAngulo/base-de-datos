← [Índice](../MANUAL-SQL.md) · [← SELECT](06-consultas-simples.md) · [FK →](08-claves-foraneas.md)

# 7. Predicados y funciones

Filtros que no son un simple `=`, y funciones que transforman valores
**en el motor** (no en Java).

## 7.1. `IN`, `BETWEEN`, `LIKE`

```sql
SELECT nombre FROM producto
WHERE categoria_id IN (1, 3);

SELECT nombre, precio FROM producto
WHERE precio BETWEEN 15 AND 40;          -- inclusive

SELECT nombre FROM producto
WHERE nombre LIKE '%USB%';               -- contiene USB

SELECT nombre FROM producto
WHERE nombre LIKE 'A%';                  -- empieza por A
```

`LIKE` distingue mayúsculas en PostgreSQL por defecto (depende de la
collation). En MySQL, con collation `ci` (case insensitive), no.

Comodines: `%` (cualquier cadena), `_` (un carácter).

Para un patrón que ignore mayúsculas en PostgreSQL:

```sql
WHERE nombre ILIKE '%usb%'               -- solo PostgreSQL
-- o, en ambos:
WHERE LOWER(nombre) LIKE '%usb%'
```

## 7.2. `NULL` otra vez

```sql
SELECT nombre FROM producto
WHERE categoria_id IS NULL;

SELECT nombre FROM producto
WHERE categoria_id IS NOT NULL;
```

`LIKE`, `IN` y comparaciones con `NULL` no dan `TRUE`: la fila desaparece
del `WHERE`. Por eso `IS NULL` es un predicado aparte.

## 7.3. `COALESCE` y `NULLIF`

```sql
-- Si categoria_id es NULL, muestra 0
SELECT nombre, COALESCE(categoria_id, 0) AS cat
FROM producto;

-- NULLIF(a, b) → NULL si a = b
SELECT NULLIF(stock, 0);                 -- evita dividir por cero más adelante
```

## 7.4. Conversión de tipos

```sql
-- PostgreSQL
SELECT precio::TEXT FROM producto;
SELECT CAST(stock AS NUMERIC) / 2 FROM producto;

-- MySQL
SELECT CAST(precio AS CHAR) FROM producto;
SELECT CAST(stock AS DECIMAL(10,2)) / 2 FROM producto;
```

`CAST` es el estándar. `::` es atajo de PostgreSQL.

## 7.5. Cadenas

```sql
SELECT UPPER(nombre), LOWER(nombre) FROM producto;
SELECT LENGTH(nombre) FROM producto;           -- PostgreSQL: caracteres
                                               -- MySQL LENGTH = bytes;
                                               -- CHAR_LENGTH = caracteres
SELECT SUBSTRING(nombre FROM 1 FOR 8) FROM producto;  -- PostgreSQL
SELECT SUBSTRING(nombre, 1, 8) FROM producto;         -- MySQL (y PG también)
SELECT TRIM(nombre) FROM producto;
```

Concatenar:

```sql
-- Estándar / PostgreSQL / MySQL 8
SELECT nombre || ' (' || stock || ' uds)' FROM producto;

-- MySQL clásico
SELECT CONCAT(nombre, ' (', stock, ' uds)') FROM producto;
```

## 7.6. Fechas

```sql
SELECT CURRENT_DATE, CURRENT_TIMESTAMP;

-- PostgreSQL
SELECT fecha, fecha + 7 FROM venta;                    -- + 7 días
SELECT fecha FROM venta WHERE fecha >= CURRENT_DATE - INTERVAL '30 days';
EXTRACT(YEAR FROM fecha)

-- MySQL
SELECT fecha, DATE_ADD(fecha, INTERVAL 7 DAY) FROM venta;
SELECT fecha FROM venta WHERE fecha >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY);
YEAR(fecha)
```

## 7.7. `CASE`

```sql
SELECT nombre, stock,
       CASE
           WHEN stock = 0 THEN 'agotado'
           WHEN stock < 10 THEN 'bajo'
           ELSE 'ok'
       END AS nivel
FROM producto;
```

← [Índice](../MANUAL-SQL.md) · [← SELECT](06-consultas-simples.md) · [FK →](08-claves-foraneas.md)
