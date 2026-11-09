-- Etapa 2, Reto 2: una sola sentencia declarativa.
SELECT id, nombre, precio, stock
FROM producto
WHERE stock < 10
ORDER BY id;
