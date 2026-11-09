-- Etapa 5, Retos 3 y 4: transferencia A -> B con fallo a propósito.
-- Producto A = id 1 (se le restan 5). Producto B = id 2 (se le sumarían 5).
-- La segunda operación induce un error (división entre cero) para abortar.

BEGIN;

UPDATE producto
SET stock = stock - 5
WHERE id = 1;

SELECT 'Después de restar 5 al producto A (aún sin COMMIT)' AS estado;
SELECT id, nombre, stock FROM producto WHERE id IN (1, 2) ORDER BY id;

-- Error crítico inducido: aborta toda la transacción.
UPDATE producto
SET stock = stock + (5 / 0)
WHERE id = 2;

COMMIT;
