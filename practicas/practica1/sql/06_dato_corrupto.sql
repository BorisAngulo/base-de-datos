-- Etapa 3, Reto 3: texto en precio y stock negativo.
-- Debe fallar por tipo de dato (precio) y/o por CHECK (stock).

INSERT INTO producto (id, nombre, precio, stock)
VALUES (4, 'Webcam HD Studio', 'GRATIS', -5);
