-- Etapa 6, Reto 4: este script debe ejecutarse CONECTADO como auxiliar_bodega.
-- 1) Actualizar stock: permitido.
-- 2) Actualizar precio: denegado.

UPDATE producto
SET stock = stock + 1
WHERE id = 2;

SELECT 'UPDATE de stock ejecutado' AS resultado;
SELECT id, nombre, precio, stock FROM producto WHERE id = 2;

UPDATE producto
SET precio = 1.00
WHERE id = 2;
