-- Etapa 3, Reto 4: precio válido, stock negativo.
-- Debe fallar únicamente por la restricción chk_stock_no_negativo.

INSERT INTO producto (id, nombre, precio, stock)
VALUES (4, 'Webcam HD Studio', 49.90, -5);
