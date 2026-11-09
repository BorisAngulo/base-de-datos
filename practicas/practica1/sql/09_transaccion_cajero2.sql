-- Etapa 4, Reto 4: Cajero 2.
-- Intenta descontar la misma unidad. Quedará en espera (lock)
-- hasta que el Cajero 1 haga COMMIT. Después, el CHECK de stock
-- debería rechazar el descuento si ya no hay existencias.

BEGIN;

SELECT 'Cajero 2: intentando descontar (puede quedar en espera)' AS estado;

UPDATE producto
SET stock = stock - 1
WHERE id = 3;

COMMIT;

SELECT 'Cajero 2: operación finalizada' AS estado;
SELECT id, nombre, stock FROM producto WHERE id = 3;
