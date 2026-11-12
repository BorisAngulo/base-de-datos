← [Índice](../MANUAL-SQL.md) · [← Índices](13-indices.md) · [Transacciones →](15-transacciones.md)

# 14. Usuarios y privilegios

Crear la base no da permiso a cualquiera. El motor distingue **quién**
entra y **qué** puede hacer. Un `GRANT` hecho como administrador no se
prueba en esa sesión: el superusuario se salta los denegados.

## 14.1. Usuario, rol y login

| Idea | PostgreSQL | MySQL |
|------|------------|--------|
| Identidad | `ROLE` (un usuario es un rol con `LOGIN`) | `USER` |
| Contraseña | `PASSWORD '...'` | `IDENTIFIED BY '...'` |
| Alcance | rol global al clúster; privilegios por base | `'nombre'@'host'` |

`CREATE USER x` en PostgreSQL = `CREATE ROLE x LOGIN`.

## 14.2. Crear, alterar, borrar

**PostgreSQL** (como `postgres`):

```sql
CREATE ROLE auxiliar_bodega LOGIN PASSWORD 'bodega123';
ALTER  ROLE auxiliar_bodega PASSWORD 'nueva_clave';

DROP OWNED BY auxiliar_bodega;
DROP ROLE auxiliar_bodega;
```

**MySQL:**

```sql
CREATE USER 'auxiliar_bodega'@'localhost' IDENTIFIED BY 'bodega123';
CREATE USER 'auxiliar_bodega'@'%'         IDENTIFIED BY 'bodega123';

ALTER USER 'auxiliar_bodega'@'localhost' IDENTIFIED BY 'nueva_clave';
DROP  USER 'auxiliar_bodega'@'localhost';
```

`'user'@'localhost'` solo desde esta máquina. `'user'@'%'` desde cualquier
host (si el servidor acepta conexiones remotas).

## 14.3. Poder conectarse

Tener el usuario **no** implica usar `inventario`.

```sql
-- PostgreSQL
GRANT CONNECT ON DATABASE inventario TO auxiliar_bodega;
GRANT USAGE   ON SCHEMA public       TO auxiliar_bodega;  -- PG 15+

-- MySQL
GRANT USAGE ON inventario.* TO 'auxiliar_bodega'@'localhost';
FLUSH PRIVILEGES;
```

`USAGE` = “puede llegar”, no “puede leer tablas”.

## 14.4. Privilegios de tabla

| Privilegio | Permite |
|------------|---------|
| `SELECT` | leer |
| `INSERT` | agregar filas |
| `UPDATE` | modificar |
| `DELETE` | borrar filas |
| `ALL PRIVILEGES` | todo lo anterior (no te hace dueño) |

```sql
-- PostgreSQL
GRANT SELECT ON TABLE producto TO auxiliar_bodega;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO auxiliar_bodega;

-- MySQL
GRANT SELECT, INSERT ON inventario.producto
    TO 'auxiliar_bodega'@'localhost';
FLUSH PRIVILEGES;
```

## 14.5. Por columna

```sql
GRANT SELECT ON TABLE producto TO auxiliar_bodega;
GRANT UPDATE (stock) ON TABLE producto TO auxiliar_bodega;
```

MySQL: `GRANT SELECT, UPDATE (stock) ON inventario.producto TO ...;`

Eso **no existe** en un CSV: quien escribe el archivo puede cambiar el
precio o vaciarlo.

## 14.6. Revocar

```sql
REVOKE UPDATE ON TABLE producto FROM auxiliar_bodega;
REVOKE CONNECT ON DATABASE inventario FROM auxiliar_bodega;

REVOKE UPDATE ON inventario.producto FROM 'auxiliar_bodega'@'localhost';
FLUSH PRIVILEGES;
```

## 14.7. Cambiar de usuario para corroborar

Los privilegios se evalúan en **la conexión**.

### Misma consola

PostgreSQL: `\c inventario auxiliar_bodega` (pide clave).
Volver: `\c inventario postgres`.

MySQL: no hay `\c` de usuario. `EXIT` y:

```bash
mysql -h 127.0.0.1 -P 3306 -u auxiliar_bodega -p inventario
```

### Dos terminales (recomendado)

| Admin | Auxiliar |
|-------|----------|
| `psql -U postgres -d inventario` | `psql -U auxiliar_bodega -d inventario` |
| `mysql -u root -p inventario` | `mysql -u auxiliar_bodega -p inventario` |

### `SET ROLE` (PostgreSQL)

```sql
SET ROLE auxiliar_bodega;
SELECT current_user;
RESET ROLE;
```

Solo si tu usuario **es miembro** de ese rol. No es un login de verdad.

### Prueba (tras el GRANT de 14.5)

Como `auxiliar_bodega`:

```sql
SELECT current_user;                                 -- auxiliar_bodega
SELECT id, nombre, precio, stock FROM producto;      -- ok
UPDATE producto SET stock = stock + 1 WHERE id = 2; -- ok
UPDATE producto SET precio = 1.00 WHERE id = 2;     -- DENEGADO
```

```text
PostgreSQL: permission denied for table producto
MySQL:      UPDATE command denied to user 'auxiliar_bodega'@'localhost'
```

Si las tres “funcionan”, sigues como administrador.

Confirmar identidad:

```sql
-- PostgreSQL
SELECT current_user, session_user;

-- MySQL
SELECT USER(), CURRENT_USER();
SHOW GRANTS;
```

### Desde Java

En `ListarTabla`, cambia `USUARIO` y `CLAVE`. Otra conexión = otra identidad.

← [Índice](../MANUAL-SQL.md) · [← Índices](13-indices.md) · [Transacciones →](15-transacciones.md)
