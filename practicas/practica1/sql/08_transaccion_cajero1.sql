-- Etapa 4, Reto 3: Cajero 1.
-- Inicia transacción, descuenta 1 unidad y NO confirma todavía.
-- En la práctica interactiva se deja abierta hasta hacer COMMIT a mano.
-- Este archivo incluye una pausa para poder ejecutarlo en paralelo.

BEGIN;

UPDATE producto
SET stock = stock - 1
WHERE id = 3;

SELECT 'Cajero 1: stock descontado, transacción ABIERTA' AS estado;
SELECT id, nombre, stock FROM producto WHERE id = 3;

-- Mantiene el bloqueo de fila mientras el Cajero 2 intenta actualizar.
SELECT pg_sleep(8);

COMMIT;

SELECT 'Cajero 1: COMMIT realizado' AS estado;
SELECT id, nombre, stock FROM producto WHERE id = 3;
