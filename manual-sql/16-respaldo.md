← [Índice](../MANUAL-SQL.md) · [← Transacciones](15-transacciones.md)

# 16. Respaldo y restauración

Un CSV es una copia de filas. Un dump de SQL es un **script** que recrea
tipos, PK, `CHECK`, FK, datos y a veces privilegios.

## 16.1. Dump lógico

**PostgreSQL**

```bash
pg_dump -h 127.0.0.1 -p 5432 -U postgres \
    --no-owner --inserts \
    -d inventario \
    -f inventario.dump.sql
```

`--inserts` escribe `INSERT` (más legible). Por defecto usa `COPY`, más
rápido para restaurar.

Solo esquema: `--schema-only`. Solo datos: `--data-only`.

**MySQL**

```bash
mysqldump -h 127.0.0.1 -P 3306 -u root -p \
    --routines --single-transaction \
    inventario > inventario.dump.sql
```

`--single-transaction` (InnoDB) saca un snapshot consistente **sin**
bloquear lecturas largas. Equivale a leer todo bajo una transacción
`REPEATABLE READ`.

## 16.2. Qué hay dentro del archivo

Abre el dump. Debes reconocer:

```sql
CREATE TABLE ... producto ...
CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0)
PRIMARY KEY
INSERT INTO producto VALUES (...);
GRANT ...          -- si pediste privilegios
```

Comparado con `inventario.csv`: el CSV no trae reglas, índices, roles ni
transacciones a medio camino.

## 16.3. Restaurar

**PostgreSQL** — la base destino tiene que existir:

```bash
createdb -h 127.0.0.1 -p 5432 -U postgres inventario_copia
psql      -h 127.0.0.1 -p 5432 -U postgres -d inventario_copia \
          -f inventario.dump.sql
```

**MySQL**

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p -e "CREATE DATABASE inventario_copia"
mysql -h 127.0.0.1 -P 3306 -u root -p inventario_copia < inventario.dump.sql
```

## 16.4. Consistencia

Un dump en caliente **sin** transacción puede mezclar ventas de las 10:00
con stock de las 10:01. Por eso:

- PostgreSQL: `pg_dump` ya corre en una transacción de snapshot.
- MySQL InnoDB: `--single-transaction`.
- MyISAM: hay que bloquear tablas (`--lock-tables`); otro motivo para InnoDB.

Copiar los archivos de datos a mano (`datadir`, `PGDATA`) es un respaldo
**físico**. En Base de Datos I basta el dump lógico.

## 16.5. Cierre del curso

Con los 16 capítulos puedes:

1. levantar una base y conectarte,
2. definir tipos y tablas con reglas,
3. cargar y consultar datos (incluidos `JOIN` y agregaciones),
4. separar quién toca el stock y quién el precio,
5. vender sin *lost update* y deshacer un fallo a mitad de camino,
6. llevarte un archivo que **recrea** el sistema, no solo las filas.

← [Índice](../MANUAL-SQL.md) · [← Transacciones](15-transacciones.md)
