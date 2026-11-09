# Informe — Práctica 1: archivos planos vs. base de datos relacional

Implementación en Java (`practicas.practica1`) sobre PostgreSQL 18.6.
Ejecución completa: `./practicas/practica1/scripts/run.sh todo`.

Catálogo usado en CSV y en la tabla `producto`:

| id | nombre | precio | stock |
|----|--------|--------|-------|
| 1 | Auriculares Bluetooth Pulse X | 59.99 | 25 |
| 2 | Cargador USB-C GaN 65W | 19.50 | 8 |
| 3 | Mouse inalámbrico Orbit Mini | 34.90 | 1 |

---

## Etapa 1 — Creación e inserción

**CSV.** `inventario.csv` ocupa **137 bytes**. El sistema de archivos solo conoce
tamaño, fechas, dueño (`boris`) y permisos `rw-r--r--`. La cabecera `id,nombre,precio,stock`
es texto: no obliga a nada.

**SQL.** `\d producto` muestra `integer`, `text`, `numeric(10,2)`, `PRIMARY KEY (id)`
y `CHECK (stock >= 0)`. Esos metadatos viven en el catálogo del motor y se aplican
en cada `INSERT`/`UPDATE`.

Los mismos tres productos se insertaron y `SELECT * FROM producto` los devolvió
intactos.

---

## Etapa 2 — Filtrado y agregación

**Java (imperativo).** Recorrer el CSV línea a línea, convertir `precio` y `stock`
a número, y filtrar `stock < 10` devolvió el cargador (8) y el mouse (1).

**SQL (declarativo).** La misma pregunta es una sentencia:

```sql
SELECT id, nombre, precio, stock FROM producto WHERE stock < 10;
```

**Complejidad (1 millón de filas, 3 condiciones).** El programa Java recorre
siempre las *n* líneas (`O(n)`), convierte tipos y acumula `if`. Tres filtros
son más código y más puntos de fallo. SQL describe el resultado; el planificador
puede usar un índice y no leer el millón de filas. Añadir una condición es un
`AND`, no un bucle nuevo.

**Agregaciones.** `COUNT(*) = 3`, `ROUND(AVG(precio), 2) = 38.13`. En Java harían
falta un contador, un acumulador `BigDecimal`, proteger la división si el CSV
está vacío y un barrido completo. SQL devuelve los dos números de una vez.

---

## Etapa 3 — Integridad y tipos

El CSV aceptó sin protestar:

```
4,Webcam HD Studio,GRATIS,-5
```

Al volver a filtrar, Java lanzó `NumberFormatException`:

> Character G is neither a decimal digit number, decimal point, nor "e" notation exponential mark.

El archivo plano no valida. Falla (si acaso) la aplicación.

El mismo registro en PostgreSQL:

| intento | resultado |
|---------|-----------|
| precio `'GRATIS'`, stock `-5` | `SQLState 22P02` — *invalid input syntax for type numeric: "GRATIS"* |
| precio `49.90`, stock `-5` | `SQLState 23514` — *violates check constraint "chk_stock_no_negativo"* |

La regla de negocio está en el esquema, no en el cliente.

---

## Etapa 4 — Concurrencia

**CSV.** Dos cajeros leyeron `stock = 1` del mouse, confirmaron a la vez y
ambos escribieron `0`. Se vendieron dos unidades que no existían (*lost update*).
El último `Files.write` gana; no hay bloqueo de fila.

**SQL.** Cajero 1 hizo `UPDATE ... stock = stock - 1` **sin COMMIT**. Cajero 2
quedó bloqueado ~2500 ms en el lock de fila. Tras el `COMMIT`, el Cajero 2
intentó dejar `stock = -1` y el motor respondió `SQLState 23514`
(`chk_stock_no_negativo`). El stock final quedó en **0**, coherente con una
sola venta. El lock evitó la carrera; el `CHECK` evitó el negativo.

Para repetirlo a mano: dos terminales con `run.sh venta --cajero 1` y
`--cajero 2`, o dos sesiones `psql` con `sql/08_transaccion_cajero1.sql` y
`sql/09_transaccion_cajero2.sql`.

---

## Etapa 5 — Atomicidad

**CSV.** Se restaron 5 unidades al producto A y se guardó el archivo. El
“apagón” (`ApagonSimuladoException`) ocurrió **antes** de sumarle esas 5 al
producto B.

Estado posterior: A = **20**, B = **8**. Las 5 unidades **no están en ningún
lado**. Cada `save()` es definitivo.

**SQL.** `BEGIN` → restar 5 a A (visible dentro de la transacción: A = 20) →
`UPDATE ... stock + (5 / 0)` → `SQLState 22012` *division by zero* → `ROLLBACK`.

Consulta con una conexión nueva: A = **25**, B = **8**. La resta nunca se
confirmó. O ocurren las dos operaciones, o no ocurre ninguna.

---

## Etapa 6 — Permisos

**CSV.** Permisos `rw-r--r--`. Quien puede escribir puede truncar: el archivo
quedó en **0 bytes**, sin catálogo. El sistema de archivos no entiende columnas;
no existe “solo actualizar stock”.

**SQL.** Rol `auxiliar_bodega` / clave `bodega123` con `SELECT` y
`GRANT UPDATE (stock)`.

| operación | resultado |
|-----------|-----------|
| `UPDATE producto SET stock = stock + 1 WHERE id = 2` | 1 fila (stock 8 → 9) |
| `UPDATE producto SET precio = 1.00 WHERE id = 2` | `SQLState 42501` — *permission denied for table producto* |

Granularidad de columna, imposible en un archivo plano.

---

## Etapa 7 — Respaldo y consolidación

`pg_dump --inserts` generó `respaldos/inventario.dump.sql` (1888 bytes). No es
una copia opaca: recrea `CREATE TABLE`, `CHECK`, `PRIMARY KEY`, los `INSERT`
y los `GRANT` del auxiliar. Restaurar es volver a ejecutar SQL.

`inventario.csv` solo tiene cabecera + tres líneas. Copiarlo no copia reglas,
índices, roles ni transacciones abiertas.

### Tres casos donde un archivo plano es mejor

1. Catálogo pequeño de un solo escritor, versionado en Git (configuración,
   códigos, export a Excel). No hay cajeros ni reglas de stock.
2. Intercambio entre sistemas heterogéneos: el CSV es el mínimo común
   denominador; la integridad se valida al importar.
3. Script puntual o log *append-only* donde abortar a mitad de camino no deja
   el negocio inconsistente, o se puede regenerar desde la fuente.

### Tres casos donde la BD relacional es imprescindible

1. Varios cajeros venden el mismo SKU a la vez. Sin lock, el CSV vende
   existencias que no hay (Etapa 4).
2. El dominio exige tipos y reglas (`stock ≥ 0`, PK) para **todos** los
   clientes, no solo para el programa que se acordó de validar (Etapas 1 y 3).
3. Separación de deberes (el auxiliar toca stock, no precios), transferencias
   atómicas ante un apagón, y un respaldo que recrea esquema y datos juntos
   (Etapas 5, 6 y 7).
