← [Índice](../MANUAL-SQL.md) · [← Subconsultas](11-subconsultas.md) · [Índices →](13-indices.md)

# 12. Vistas

Una vista es un `SELECT` **con nombre**. Se consulta como tabla, pero no
guarda filas propias: se recalcula (salvo vistas materializadas, que no
entran en este curso).

## 12.1. Crear y usar

```sql
CREATE VIEW v_stock_bajo AS
SELECT id, nombre, precio, stock
FROM producto
WHERE stock < 10;

SELECT * FROM v_stock_bajo ORDER BY stock;
```

Sirve para:

- no repetir un `JOIN` largo.
- ocultar columnas (el auxiliar ve la vista, no la tabla).
- documentar una pregunta de negocio (“productos con stock bajo”).

## 12.2. Cambiar y borrar

```sql
CREATE OR REPLACE VIEW v_stock_bajo AS
SELECT id, nombre, stock
FROM producto
WHERE stock < 5;

DROP VIEW IF EXISTS v_stock_bajo;
```

`CREATE OR REPLACE` existe en PostgreSQL y MySQL 8. En MySQL 5.7 a veces
hace falta `DROP` + `CREATE`.

## 12.3. Vistas y permisos

```sql
GRANT SELECT ON v_stock_bajo TO auxiliar_bodega;
```

El auxiliar puede leer el recorte **sin** `SELECT` sobre `producto` si
la vista es suya o tiene `GRANT` y el dueño de la vista es dueño de la
tabla (regla de PostgreSQL: *security invoker* vs *definer* — en este
curso basta con: el dueño crea la vista y concede `SELECT` sobre ella).

MySQL: `SQL SECURITY DEFINER` (por defecto) ejecuta con los privilegios
del creador; `INVOKER`, con los de quien consulta.

## 12.4. ¿Se puede hacer `INSERT` en una vista?

Solo si el motor puede traducir la fila a **una** tabla base, sin
agregación ni `DISTINCT`. En la práctica, para Base de Datos I trata las
vistas como **de solo lectura**.

```sql
-- Suele fallar o ser una mala idea
INSERT INTO v_stock_bajo (nombre, precio, stock)
VALUES ('X', 1, 0);
```

## 12.5. No es una copia

Si actualizas `producto`, la vista cambia al instante. No hay que
“refrescarla”. Eso las distingue de una tabla de reporte que llenaste
con `INSERT … SELECT`.

← [Índice](../MANUAL-SQL.md) · [← Subconsultas](11-subconsultas.md) · [Índices →](13-indices.md)
