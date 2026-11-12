← [Índice](../MANUAL-SQL.md) · [← Usuarios](14-usuarios-y-privilegios.md) · [Respaldo →](16-respaldo.md)

# 15. Transacciones y concurrencia

Una transacción agrupa varias sentencias en **todo o nada**. Si algo falla
(o cortas la luz), el motor deshace lo que no se confirmó.

## 15.1. ACID

| | Significa | En la práctica |
|--|-----------|----------------|
| **A**tomicidad | Todo o nada | Resta 5 a A y suma 5 a B: o las dos, o ninguna |
| **C**onsistencia | Las reglas siguen valiendo | Un `CHECK` o una FK abortan y revierten |
| **I**slamiento | Las sesiones no se pisan a ciegas | El otro no ve tus `UPDATE` hasta el `COMMIT` |
| **D**urabilidad | Lo confirmado sobrevive un crash | Tras `COMMIT`, está en disco (WAL / redo log) |

Un CSV no tiene nada de esto: cada `save()` es definitivo y visible.

## 15.2. Autocommit

Por defecto **cada sentencia se confirma sola**.

| | PostgreSQL | MySQL (InnoDB) |
|--|------------|----------------|
| Autocommit | on | on |
| Abrir transacción | `BEGIN;` o `START TRANSACTION;` | `START TRANSACTION;` o `BEGIN;` |
| Confirmar | `COMMIT;` | `COMMIT;` |
| Deshacer | `ROLLBACK;` | `ROLLBACK;` |

```sql
START TRANSACTION;          -- o BEGIN;
UPDATE producto SET stock = stock - 5 WHERE id = 1;
UPDATE producto SET stock = stock + 5 WHERE id = 2;
COMMIT;
```

En `psql` / `mysql` el autocommit sigue activo hasta el `BEGIN`.
En JDBC: `connection.setAutoCommit(false)` es el equivalente.

## 15.3. `ROLLBACK` y el apagón

```sql
START TRANSACTION;
UPDATE producto SET stock = stock - 5 WHERE id = 1;   -- A queda en 20, aún no fijo
-- error a propósito:
UPDATE producto SET stock = stock + (5 / 0) WHERE id = 2;
```

PostgreSQL: `ERROR: division by zero` y la transacción queda **abortada**.
Cualquier sentencia posterior falla hasta `ROLLBACK` (o se cierra la
sesión, que también deshace).

MySQL: la división por cero puede ser advertencia o error según
`sql_mode`. Fuerza un fallo claro:

```sql
START TRANSACTION;
UPDATE producto SET stock = stock - 5 WHERE id = 1;
UPDATE producto SET stock = stock + 5 WHERE id = 99999;  -- 0 filas, no aborta
-- mejor: un error de verdad
INSERT INTO producto (nombre, precio, stock) VALUES ('x', 'GRATIS', 1);
ROLLBACK;
```

Consulta **en otra conexión** (o tras el `ROLLBACK`): A sigue en 25.
Las 5 unidades no desaparecieron. Eso es atomicidad.

Si cierras el cliente sin `COMMIT`, el motor hace `ROLLBACK`.

## 15.4. `SAVEPOINT`

Deshacer **parte** de la transacción, no toda.

```sql
START TRANSACTION;
UPDATE producto SET stock = stock - 1 WHERE id = 1;
SAVEPOINT despues_de_a;
UPDATE producto SET stock = stock - 1 WHERE id = 2;
ROLLBACK TO SAVEPOINT despues_de_a;     -- deshace solo el de id=2
COMMIT;                                 -- confirma el de id=1
```

## 15.5. Qué ve la otra sesión (aislamiento)

Niveles, del más débil al más estricto:

| Nivel | Lecturas sucias | Lecturas no repetibles | Fantasmas |
|-------|-----------------|------------------------|-----------|
| `READ UNCOMMITTED` | sí (MySQL; PG lo trata como RC) | sí | sí |
| `READ COMMITTED` | no | sí | sí |
| `REPEATABLE READ` | no | no | depende |
| `SERIALIZABLE` | no | no | no |

Por defecto:

- **PostgreSQL:** `READ COMMITTED`
- **MySQL InnoDB:** `REPEATABLE READ`

```sql
-- PostgreSQL
SHOW default_transaction_isolation;
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

-- MySQL
SELECT @@transaction_isolation;
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

**Lectura sucia:** ver el `UPDATE` ajeno **antes** del `COMMIT`. PostgreSQL
no las permite. En MySQL, `READ UNCOMMITTED` sí.

**No repetible:** dos `SELECT` de la misma fila, en tu transacción, ven
valores distintos porque el otro hizo `COMMIT` en medio.

**Fantasma:** un `SELECT ... WHERE stock < 10` devuelve 2 filas; el otro
inserta una tercera y confirma; tu segundo `SELECT` ve 3.

## 15.6. Bloqueos: dos cajeros

Stock del mouse (`id = 3`) = 1. Dos sesiones venden a la vez.

**Sesión 1**

```sql
START TRANSACTION;
UPDATE producto SET stock = stock - 1 WHERE id = 3;
-- NO hagas COMMIT todavía
SELECT id, stock FROM producto WHERE id = 3;   -- ves 0
```

**Sesión 2** (otra terminal, misma tabla)

```sql
START TRANSACTION;
UPDATE producto SET stock = stock - 1 WHERE id = 3;
-- se QUEDA ESPERANDO el lock de fila
```

Vuelves a la sesión 1:

```sql
COMMIT;
```

La sesión 2 despierta. Ahora el `UPDATE` calcula `stock = 0 - 1 = -1` y:

- con `CHECK (stock >= 0)` → **error**, no hay venta fantasma.
- sin `CHECK` → stock negativo (malo). Por eso el `CHECK` del capítulo 4
  y el lock **juntos** cierran el agujero.

En un CSV, ambos leen `1`, ambos escriben `0`: se vendieron 2 unidades
que no existían (*lost update*).

### `SELECT ... FOR UPDATE`

Reservar la fila **al leerla**, no al actualizar:

```sql
START TRANSACTION;
SELECT stock FROM producto WHERE id = 3 FOR UPDATE;
-- otra sesión que haga FOR UPDATE o UPDATE espera
UPDATE producto SET stock = stock - 1 WHERE id = 3;
COMMIT;
```

MySQL y PostgreSQL lo soportan en InnoDB / heap estándar.

### Timeout

```sql
-- PostgreSQL: abortar si el lock tarda más de 3 s
SET lock_timeout = '3s';

-- MySQL
SET innodb_lock_wait_timeout = 3;
```

## 15.7. Deadlock

Sesión 1 actualiza producto 1 y espera el 2.
Sesión 2 actualiza producto 2 y espera el 1.
El motor detecta el ciclo, **mata una** transacción y la otra sigue.

```text
PostgreSQL: deadlock detected
MySQL:      Deadlock found when trying to get lock
```

La aplicación debe reintentar. Evita: actualiza siempre las filas **en el
mismo orden** (por `id` creciente, por ejemplo).

## 15.8. Transferencia atómica (patrón)

```sql
START TRANSACTION;

UPDATE producto SET stock = stock - 5 WHERE id = 1 AND stock >= 5;
-- en PostgreSQL: GET DIAGNOSTICS / INSERT RETURNING;
-- comprobación portable:
--   si no se actualizó ninguna fila, ROLLBACK

UPDATE producto SET stock = stock + 5 WHERE id = 2;
COMMIT;
```

Si la segunda sentencia falla, `ROLLBACK` y A no pierde stock.

## 15.9. JDBC (Java)

```java
conn.setAutoCommit(false);
try {
    // UPDATE A, UPDATE B
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
} finally {
    conn.setAutoCommit(true);
}
```

Dos `Connection` = dos cajeros. El lock se ve igual que en `psql`/`mysql`.

← [Índice](../MANUAL-SQL.md) · [← Usuarios](14-usuarios-y-privilegios.md) · [Respaldo →](16-respaldo.md)
