-- Etapa 2, Reto 4: total de artículos y precio promedio.
SELECT
    COUNT(*)                    AS total_articulos,
    ROUND(AVG(precio), 2)       AS precio_promedio
FROM producto;
