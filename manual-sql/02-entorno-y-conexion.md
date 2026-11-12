← [Índice](../MANUAL-SQL.md) · [← Introducción](01-introduccion.md) · [Tipos →](03-tipos-de-datos.md)

# 2. Entorno, conexión y bases de datos

SQL no abre un archivo: habla con un **proceso servidor** que guarda las bases.

## 2.1. Piezas de una conexión

| Pieza | Qué es | PostgreSQL | MySQL |
|-------|--------|------------|--------|
| Motor | El SGBD | `postgres` | `mysqld` |
| Host | Dónde está | `127.0.0.1` | `127.0.0.1` |
| Puerto | Puerta TCP | **5432** | **3306** |
| Base | Contenedor de tablas | `inventario` | `inventario` |
| Usuario | Identidad | `postgres` / `practica` | `root` / `practica` |

```
motor  →  base de datos  →  esquema  →  tabla  →  filas
```

En **PostgreSQL** un clúster tiene varias bases; dentro de cada una, el esquema
por defecto es `public`. En **MySQL**, “base” y “esquema” son casi sinónimos.

## 2.2. Abrir la consola

Hay que entrar como un usuario que pueda crear bases (`postgres` o `root`):

```bash
psql  -h 127.0.0.1 -p 5432 -U postgres
mysql -h 127.0.0.1 -P 3306 -u root -p
```

Una conexión = host + puerto + usuario + (opcional) base.

## 2.3. Crear y borrar una base

Se hace **conectado a otra base**. No puedes borrar aquella en la que estás.

**PostgreSQL**

```sql
CREATE DATABASE inventario
    WITH ENCODING = 'UTF8'
         TEMPLATE = template0;

SELECT datname FROM pg_database ORDER BY 1;

DROP DATABASE inventario;              -- falla si hay sesiones abiertas
DROP DATABASE IF EXISTS inventario;
```

**MySQL**

```sql
CREATE DATABASE inventario
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

SHOW DATABASES;

DROP DATABASE IF EXISTS inventario;
```

`IF EXISTS` / `IF NOT EXISTS` evitan el error si el objeto ya está (o no).

## 2.4. Entrar a esa base

Crearla no te “mete” dentro.

| | PostgreSQL | MySQL |
|--|------------|--------|
| En la consola ya abierta | `\c inventario` | `USE inventario;` |
| Al arrancar | `psql -U postgres -d inventario` | `mysql -u root -p inventario` |

Desde ahí, `CREATE TABLE` e `INSERT` ocurren **dentro** de `inventario`.

## 2.5. Esquemas (PostgreSQL)

Un esquema es un namespace dentro de la misma base:

```sql
CREATE SCHEMA IF NOT EXISTS bodega;

CREATE TABLE bodega.producto (
    id INTEGER PRIMARY KEY,
    nombre TEXT NOT NULL
);

SET search_path TO bodega, public;
```

Sin esquema propio, las tablas caen en `public`.
En MySQL, `CREATE SCHEMA` es un alias de `CREATE DATABASE`.

## 2.6. Ver qué hay

**PostgreSQL** (`psql`): `\l` bases, `\dn` esquemas, `\dt` tablas, `\d producto`
columnas y restricciones.

```sql
SELECT current_database(), current_user;

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema NOT IN ('pg_catalog', 'information_schema');
```

**MySQL:** `SHOW DATABASES;`, `SHOW TABLES;`, `DESCRIBE producto;`

## 2.7. JDBC (Java)

La misma conexión, en URL:

```text
jdbc:postgresql://127.0.0.1:5432/inventario
jdbc:mysql://127.0.0.1:3306/inventario
```

Usuario y clave van aparte (`DriverManager.getConnection(url, user, pass)`).
Java no “es” SQL: **envía** la cadena (`SELECT * FROM producto`) y recorre
el `ResultSet`.

← [Índice](../MANUAL-SQL.md) · [← Introducción](01-introduccion.md) · [Tipos →](03-tipos-de-datos.md)
