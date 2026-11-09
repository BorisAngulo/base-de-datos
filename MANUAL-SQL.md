# Manual de SQL

Guía práctica de SQL para el proyecto **base-de-datos**.
Los ejemplos usan el catálogo `inventario` y la tabla `producto`.
Cuando PostgreSQL y MySQL se separan, se indica el motor.

> Se irá ampliando por partes. Este índice crece a medida que se agreguen secciones.

---

## Índice

1. [Creación de base de datos, conexión y entorno](#1-creación-de-base-de-datos-conexión-y-entorno)
   - [1.1. Piezas del entorno](#11-piezas-del-entorno)
   - [1.2. Cómo se abre una conexión](#12-cómo-se-abre-una-conexión)
   - [1.3. Crear y borrar una base de datos](#13-crear-y-borrar-una-base-de-datos)
   - [1.4. Conectarse a una base concreta](#14-conectarse-a-una-base-concreta)
   - [1.5. Esquemas](#15-esquemas)
   - [1.6. Ver qué hay creado](#16-ver-qué-hay-creado)
   - [1.7. Relación con el código Java](#17-relación-con-el-código-java)
2. [Creación de usuarios y privilegios](#2-creación-de-usuarios-y-privilegios)
   - [2.1. Usuario, rol y login](#21-usuario-rol-y-login)
   - [2.2. Crear y eliminar usuarios](#22-crear-y-eliminar-usuarios)
   - [2.3. Privilegio de conexión](#23-privilegio-de-conexión)
   - [2.4. Privilegios sobre tablas](#24-privilegios-sobre-tablas)
   - [2.5. Privilegios por columna](#25-privilegios-por-columna)
   - [2.6. Revocar privilegios](#26-revocar-privilegios)
   - [2.7. Comprobar con qué usuario se está](#27-comprobar-con-qué-usuario-se-está)
   - [2.8. Cambiar de usuario para corroborar los permisos](#28-cambiar-de-usuario-para-corroborar-los-permisos)

---

## 1. Creación de base de datos, conexión y entorno

SQL no “abre el archivo”: habla con un **motor** (PostgreSQL, MySQL, …) que
guarda las bases, aplica tipos y controla quién puede hacer qué.

### 1.1. Piezas del entorno

| Pieza | Qué es | Ejemplo en este proyecto |
|-------|--------|--------------------------|
| Motor (SGBD) | El programa que ejecuta SQL | PostgreSQL o MySQL |
| Host | Dónde está el servidor | `127.0.0.1` (esta misma máquina) |
| Puerto | La puerta TCP del motor | PostgreSQL **5432**, MySQL **3306** |
| Base de datos | El contenedor de tablas, vistas, etc. | `inventario` |
| Usuario | Con qué identidad te conectas | `practica` |
| Contraseña | Autenticación de esa identidad | la que hayas definido |

Jerarquía típica:

```
motor  →  base de datos  →  esquema (sobre todo en PostgreSQL)  →  tabla  →  filas
```

En MySQL, “base de datos” y “esquema” se usan casi como sinónimos.
En PostgreSQL hay **varias bases**; dentro de cada una, el esquema por defecto
es `public`.

### 1.2. Cómo se abre una conexión

Desde la **consola** del motor (hay que estar autenticado como un usuario que
pueda crear bases; al instalar, suele ser `postgres` o `root`):

```bash
# PostgreSQL (puerto por defecto 5432)
psql -h 127.0.0.1 -p 5432 -U postgres

# MySQL (puerto por defecto 3306)
mysql -h 127.0.0.1 -P 3306 -u root -p
```

Una conexión queda definida por **host + puerto + usuario + base**.
Si omites la base, el cliente entra a una base por defecto
(`postgres` en PostgreSQL, ninguna o `mysql` según el cliente).

### 1.3. Crear y borrar una base de datos

Esto se ejecuta **conectado a otra base** (no puedes borrar aquella a la que
estás conectado).

**PostgreSQL**

```sql
CREATE DATABASE inventario
    WITH ENCODING = 'UTF8'
         TEMPLATE = template0;

-- Listar bases
SELECT datname FROM pg_database ORDER BY datname;

DROP DATABASE inventario;                 -- falla si alguien está conectado
DROP DATABASE IF EXISTS inventario;
```

**MySQL**

```sql
CREATE DATABASE inventario
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

SHOW DATABASES;

DROP DATABASE inventario;
DROP DATABASE IF EXISTS inventario;
```

`IF EXISTS` / `IF NOT EXISTS` evitan el error si el objeto ya está (o no está).

### 1.4. Conectarse a una base concreta

Crear la base no te “mete” dentro. Hay que **abrir sesión contra ella**.

**PostgreSQL** (en `psql`):

```text
\c inventario
```

O al arrancar:

```bash
psql -h 127.0.0.1 -p 5432 -U practica -d inventario
```

**MySQL:**

```sql
USE inventario;
```

O al arrancar:

```bash
mysql -h 127.0.0.1 -P 3306 -u practica -p inventario
```

A partir de ahí, `CREATE TABLE`, `INSERT` y `SELECT` ocurren **dentro** de
`inventario`.

### 1.5. Esquemas

En PostgreSQL un esquema agrupa objetos dentro de la misma base
(como un namespace):

```sql
CREATE SCHEMA IF NOT EXISTS bodega;

CREATE TABLE bodega.producto (
    id      INTEGER PRIMARY KEY,
    nombre  TEXT NOT NULL,
    precio  NUMERIC(10, 2) NOT NULL,
    stock   INTEGER NOT NULL
);

SET search_path TO bodega, public;
```

Si no creas esquema, las tablas caen en `public`.

En MySQL, `CREATE SCHEMA` es un alias de `CREATE DATABASE`.

### 1.6. Ver qué hay creado

**PostgreSQL** (`psql`):

```text
\l          -- bases de datos
\c inventario
\dn         -- esquemas
\dt         -- tablas
\d producto -- columnas, tipos, PK y CHECK de una tabla
```

Equivalente en SQL:

```sql
SELECT current_database();
SELECT current_user;

SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
ORDER BY 1, 2;
```

**MySQL:**

```sql
SHOW DATABASES;
SHOW TABLES FROM inventario;
DESCRIBE producto;
```

### 1.7. Relación con el código Java

JDBC arma la misma conexión con una URL. En
`ListarTabla` queda así:

```text
jdbc:postgresql://127.0.0.1:5432/inventario
jdbc:mysql://127.0.0.1:3306/inventario
```

Esa URL es host + puerto + **nombre de la base**. El usuario y la clave van
aparte (`DriverManager.getConnection(url, usuario, clave)`).

La sentencia SQL que viaja por esa conexión es independiente del lenguaje:

```sql
SELECT * FROM producto;
```

Java no “es” SQL: solo **envía** esa cadena al motor y recorre el resultado.

---

## 2. Creación de usuarios y privilegios

Crear la base no da permiso a cualquiera. El motor distingue **quién** se
conecta y **qué** puede hacer: ver, insertar, cambiar el stock, borrar la
tabla, etc.

### 2.1. Usuario, rol y login

| Idea | PostgreSQL | MySQL |
|------|------------|--------|
| Identidad | `ROLE` (un usuario es un rol con login) | `USER` |
| Poder entrar | atributo `LOGIN` | el propio `CREATE USER` |
| Contraseña | `PASSWORD '...'` | `IDENTIFIED BY '...'` |
| Alcance | roles globales al clúster; privilegios por base | usuario `'nombre'@'host'` |

En PostgreSQL, `CREATE USER x` es equivalente a `CREATE ROLE x LOGIN`.

### 2.2. Crear y eliminar usuarios

**PostgreSQL** (conectado como superusuario, p. ej. `postgres`):

```sql
CREATE ROLE auxiliar_bodega
    LOGIN
    PASSWORD 'bodega123';

-- Variante equivalente
CREATE USER auxiliar_bodega PASSWORD 'bodega123';

ALTER ROLE auxiliar_bodega PASSWORD 'nueva_clave';

-- Antes de borrar: quitar privilegios u objetos que dependan del rol
DROP OWNED BY auxiliar_bodega;
DROP ROLE auxiliar_bodega;
```

**MySQL:**

```sql
CREATE USER 'auxiliar_bodega'@'localhost' IDENTIFIED BY 'bodega123';

-- El mismo usuario desde cualquier host (menos restrictivo)
CREATE USER 'auxiliar_bodega'@'%' IDENTIFIED BY 'bodega123';

ALTER USER 'auxiliar_bodega'@'localhost' IDENTIFIED BY 'nueva_clave';

DROP USER 'auxiliar_bodega'@'localhost';
```

`'usuario'@'localhost'` solo puede entrar desde la misma máquina.
`'usuario'@'%'` acepta conexiones remotas (si el servidor las escucha).

### 2.3. Privilegio de conexión

Tener el usuario creado **no** implica poder usar la base `inventario`.

**PostgreSQL**

```sql
GRANT CONNECT ON DATABASE inventario TO auxiliar_bodega;

-- En PostgreSQL 15+ el esquema public no se comparte por defecto:
GRANT USAGE ON SCHEMA public TO auxiliar_bodega;
```

**MySQL**

```sql
GRANT USAGE ON inventario.* TO 'auxiliar_bodega'@'localhost';
FLUSH PRIVILEGES;
```

`USAGE` aquí significa “existe y puede llegar”, no “puede leer tablas”.

### 2.4. Privilegios sobre tablas

Privilegios habituales sobre una tabla:

| Privilegio | Permite |
|------------|---------|
| `SELECT` | leer filas |
| `INSERT` | agregar filas |
| `UPDATE` | modificar filas |
| `DELETE` | borrar filas |
| `TRUNCATE` | vaciar la tabla (PostgreSQL lo trata aparte) |
| `ALL PRIVILEGES` | todo lo anterior (no convierte en dueño) |

**PostgreSQL**

```sql
GRANT SELECT ON TABLE producto TO auxiliar_bodega;
GRANT INSERT, UPDATE ON TABLE producto TO auxiliar_bodega;

-- Todas las tablas actuales del esquema
GRANT SELECT ON ALL TABLES IN SCHEMA public TO auxiliar_bodega;
```

**MySQL**

```sql
GRANT SELECT, INSERT, UPDATE ON inventario.producto
    TO 'auxiliar_bodega'@'localhost';

GRANT SELECT ON inventario.*
    TO 'auxiliar_bodega'@'localhost';

FLUSH PRIVILEGES;
```

Ejemplo de lo que **sí** debería funcionar como `auxiliar_bodega`:

```sql
SELECT * FROM producto;
UPDATE producto SET stock = stock + 1 WHERE id = 2;
```

### 2.5. Privilegios por columna

Se puede permitir actualizar **solo** `stock` y no el `precio`.
Eso no existe en un archivo CSV: quien escribe el archivo puede cambiarlo todo.

**PostgreSQL**

```sql
GRANT SELECT ON TABLE producto TO auxiliar_bodega;
GRANT UPDATE (stock) ON TABLE producto TO auxiliar_bodega;
```

Con esa configuración:

```sql
-- Permitido
UPDATE producto SET stock = 10 WHERE id = 3;

-- Denegado (permission denied)
UPDATE producto SET precio = 1.00 WHERE id = 3;
```

**MySQL** (misma idea):

```sql
GRANT SELECT, UPDATE (stock) ON inventario.producto
    TO 'auxiliar_bodega'@'localhost';
FLUSH PRIVILEGES;
```

### 2.6. Revocar privilegios

`REVOKE` es el inverso de `GRANT`.

**PostgreSQL**

```sql
REVOKE UPDATE ON TABLE producto FROM auxiliar_bodega;
REVOKE CONNECT ON DATABASE inventario FROM auxiliar_bodega;
```

**MySQL**

```sql
REVOKE UPDATE ON inventario.producto FROM 'auxiliar_bodega'@'localhost';
FLUSH PRIVILEGES;
```

Después de revocar `UPDATE`, un `UPDATE` debe fallar aunque el `SELECT` siga
funcionando.

### 2.7. Comprobar con qué usuario se está

Un `GRANT` hecho como administrador **no se prueba en esa misma sesión**.
El superusuario ignora casi todos los denegados. Hay que mirar `current_user`
y, si sigue diciendo `postgres` / `root`, todavía no se ha cambiado de identidad.

**PostgreSQL**

```sql
SELECT current_user, session_user;

SELECT grantee, privilege_type, is_grantable
FROM information_schema.role_table_grants
WHERE table_name = 'producto'
ORDER BY grantee, privilege_type;
```

**MySQL**

```sql
SELECT USER(), CURRENT_USER();
SHOW GRANTS FOR 'auxiliar_bodega'@'localhost';
SHOW GRANTS;   -- los de la sesión actual
```

### 2.8. Cambiar de usuario para corroborar los permisos

Los privilegios se evalúan **en la conexión**. Para corroborarlos hay que
entrar **como** `auxiliar_bodega`, no solo crear el rol.

#### Opción A — Reconectar en la misma consola (lo más directo)

**PostgreSQL** (`psql`). `\c` cambia de base **y** de usuario sin salir:

```text
-- Estás como postgres (administrador)
SELECT current_user;                    -- postgres

\c inventario auxiliar_bodega
-- pide la clave: bodega123

SELECT current_user;                    -- debe decir auxiliar_bodega
```

Volver al administrador:

```text
\c inventario postgres
```

**MySQL.** No hay un `\c` de usuario: se sale y se entra de nuevo.

```sql
SELECT CURRENT_USER();                  -- root@localhost
EXIT;
```

```bash
mysql -h 127.0.0.1 -P 3306 -u auxiliar_bodega -p inventario
# clave: bodega123
```

```sql
SELECT CURRENT_USER();                  -- auxiliar_bodega@localhost
```

Volver a root:

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p inventario
```

#### Opción B — Dos terminales (recomendado para la práctica)

Dejas al administrador en una ventana y pruebas en otra. Así no pierdes
los `GRANT` a media prueba.

| Terminal 1 (admin) | Terminal 2 (auxiliar) |
|--------------------|------------------------|
| `psql -U postgres -d inventario` | `psql -U auxiliar_bodega -d inventario` |
| `mysql -u root -p inventario` | `mysql -u auxiliar_bodega -p inventario` |

#### Opción C — `SET ROLE` (PostgreSQL, no es un login)

```sql
SET ROLE auxiliar_bodega;
SELECT current_user;                    -- auxiliar_bodega
RESET ROLE;
```

Solo funciona si **tu usuario actual es miembro** de ese rol
(`GRANT auxiliar_bodega TO postgres`). No sustituye a `\c` ni a una
sesión nueva: el auxiliar real, por sí solo, no puede hacer `SET ROLE`
a `postgres`.

#### Prueba concreta (después de los GRANT de 2.5)

Conectado **ya** como `auxiliar_bodega`:

```sql
SELECT current_user;                    -- confirmar identidad

SELECT id, nombre, precio, stock FROM producto;     -- debe funcionar

UPDATE producto SET stock = stock + 1 WHERE id = 2; -- debe funcionar

UPDATE producto SET precio = 1.00 WHERE id = 2;     -- debe FALLAR
```

Errores esperados al denegar:

```text
PostgreSQL:  ERROR:  permission denied for table producto
MySQL:       ERROR 1142 (42000): UPDATE command denied to user
             'auxiliar_bodega'@'localhost' for table 'producto'
```

Si esas tres sentencias “funcionan todas”, no cambiaste de usuario: sigues
como administrador.

#### Desde Java

En `ListarTabla` cambia `USUARIO` y `CLAVE` y vuelve a ejecutar. Es la misma
idea: otra conexión, otra identidad.

```java
static final String USUARIO = "auxiliar_bodega";
static final String CLAVE = "bodega123";
```

Un `SELECT * FROM producto` debe listar filas. Un programa que haga
`UPDATE producto SET precio = 1` debe recibir el mismo *permission denied*.
