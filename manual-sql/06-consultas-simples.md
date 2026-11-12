← [Índice](../MANUAL-SQL.md) · [← DML](05-insertar-actualizar-borrar.md) · [Predicados →](07-predicados-y-funciones.md)

# 6. Consultas simples (`SELECT`)

`SELECT` es **declarativo**: describes el resultado, no el bucle.
El motor decide si barre la tabla o usa un índice ([capítulo 13](13-indices.md)).

## 6.1. Proyección (columnas)

```sql
SELECT * FROM producto;

SELECT id, nombre, precio FROM producto;
SELECT nombre AS articulo, precio AS precio_lista FROM producto;
```

`*` es cómodo para explorar. En código de aplicación, nombra las columnas.

## 6.2. Selección (filas): `WHERE`

```sql
SELECT id, nombre, stock
FROM producto
WHERE stock < 10;

SELECT nombre, precio
FROM producto
WHERE precio >= 20 AND stock > 0;

SELECT nombre
FROM producto
WHERE categoria_id = 1 OR stock = 1;
```

Operadores: `=`, `<>`, `<`, `>`, `<=`, `>=`, `AND`, `OR`, `NOT`.
Paréntesis cuando mezcles `AND` y `OR`.

## 6.3. Orden y recorte

```sql
SELECT nombre, precio
FROM producto
ORDER BY precio DESC, nombre ASC;

-- PostgreSQL / estándar
SELECT nombre, stock FROM producto
ORDER BY stock
LIMIT 2 OFFSET 0;

-- MySQL admite LIMIT igual
SELECT nombre, stock FROM producto
ORDER BY stock
LIMIT 2;

-- SQL Server no es este curso; en MySQL 8 / PostgreSQL también:
SELECT nombre FROM producto
ORDER BY id
OFFSET 2 ROWS FETCH NEXT 1 ROW ONLY;
```

Sin `ORDER BY`, el orden de las filas **no está garantizado**.

## 6.4. `DISTINCT`

```sql
SELECT DISTINCT categoria_id FROM producto;
```

Elimina duplicados del resultado. Cuesta más que un `SELECT` simple.

## 6.5. Alias de tabla

```sql
SELECT p.nombre, p.stock
FROM producto AS p
WHERE p.stock < 10;
```

En PostgreSQL `AS` es opcional. En MySQL, mejor escribirlo.
Hace falta en cuanto hay [JOIN](09-combinar-tablas.md).

## 6.6. Expresiones en el `SELECT`

```sql
SELECT nombre,
       precio,
       stock,
       precio * stock AS valor_en_bodega
FROM producto;
```

No puedes usar el alias `valor_en_bodega` en el `WHERE` de la misma
consulta (se evalúa después). Repite la expresión o usa una subconsulta /
[vista](12-vistas.md).

## 6.7. Equivalente mental en Java

```text
Java:  abrir CSV → para cada línea → parsear → if (stock < 10) imprimir
SQL:   SELECT ... FROM producto WHERE stock < 10
```

Con un millón de filas y tres condiciones, Java recorre todo (`O(n)`).
SQL puede usar un índice y no tocar el millón.

← [Índice](../MANUAL-SQL.md) · [← DML](05-insertar-actualizar-borrar.md) · [Predicados →](07-predicados-y-funciones.md)
