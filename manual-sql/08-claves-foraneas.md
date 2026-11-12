← [Índice](../MANUAL-SQL.md) · [← Predicados](07-predicados-y-funciones.md) · [JOIN →](09-combinar-tablas.md)

# 8. Claves foráneas

Una clave foránea dice: “este valor **tiene que existir** en otra tabla”.
Es integridad referencial: no hay ventas de un producto que no existe.

## 8.1. Relación del catálogo

```
categoria.id  ←  producto.categoria_id
producto.id   ←  venta.producto_id
cliente.id    ←  venta.cliente_id
```

Padre = tabla referenciada (`producto`).
Hijo = tabla que apunta (`venta`).

## 8.2. Declararlas

Ya estaban en el script del [capítulo 4](04-tablas-y-restricciones.md).
Forma explícita:

```sql
ALTER TABLE venta
    ADD CONSTRAINT venta_producto_fk
        FOREIGN KEY (producto_id) REFERENCES producto (id);

ALTER TABLE venta
    ADD CONSTRAINT venta_cliente_fk
        FOREIGN KEY (cliente_id) REFERENCES cliente (id);
```

La columna referenciada debe ser `PRIMARY KEY` o `UNIQUE`.

MySQL: la tabla tiene que ser **InnoDB**. En MyISAM la FK se ignora o falla.

## 8.3. Qué pasa al insertar

```sql
INSERT INTO venta (producto_id, cliente_id, cantidad)
VALUES (999, 1, 1);
```

Falla: no hay producto `999`.

```text
PostgreSQL: insert or update on table "venta" violates foreign key constraint
MySQL:      Cannot add or update a child row: a foreign key constraint fails
```

## 8.4. Qué pasa al borrar (o actualizar) el padre

Por defecto: **NO ACTION / RESTRICT** — no puedes borrar un producto que
tiene ventas.

Otras políticas, en el `REFERENCES`:

| Acción | Efecto al borrar el padre |
|--------|---------------------------|
| `RESTRICT` / `NO ACTION` | Rechaza el `DELETE` del padre |
| `CASCADE` | Borra también los hijos |
| `SET NULL` | Deja la FK del hijo en `NULL` (la columna debe admitirlo) |
| `SET DEFAULT` | Pone el `DEFAULT` de la columna |

```sql
-- Si se borra la categoría, los productos quedan sin categoría
ALTER TABLE producto
    DROP CONSTRAINT producto_categoria_id_fkey,   -- nombre en PostgreSQL: \d producto
    ADD CONSTRAINT producto_categoria_fk
        FOREIGN KEY (categoria_id)
        REFERENCES categoria (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;
```

MySQL:

```sql
ALTER TABLE producto
    DROP FOREIGN KEY producto_categoria_fk,
    ADD CONSTRAINT producto_categoria_fk
        FOREIGN KEY (categoria_id)
        REFERENCES categoria (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;
```

`ON UPDATE CASCADE` propaga el cambio de `id` del padre. Casi nunca
cambias una PK; se declara más por costumbre que por uso.

## 8.5. Ciclo de vida

1. Inserta **padres** primero (`categoria`, `cliente`, `producto`).
2. Inserta **hijos** (`venta`).
3. Borra al revés: hijos, luego padres. O usa `ON DELETE CASCADE` a
   propósito, no por accidente.

```sql
DELETE FROM producto WHERE id = 1;
-- Falla si hay ventas de ese producto y la FK es RESTRICT.
```

← [Índice](../MANUAL-SQL.md) · [← Predicados](07-predicados-y-funciones.md) · [JOIN →](09-combinar-tablas.md)
