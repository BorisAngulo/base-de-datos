-- Etapa 6, Retos 2 y 3: usuario auxiliar con privilegios granulares.
-- Puede VER la tabla e UPDATE solo la columna stock.
-- Si el rol ya existe: DROP OWNED BY auxiliar_bodega; DROP ROLE auxiliar_bodega;

DROP ROLE IF EXISTS auxiliar_bodega;

CREATE ROLE auxiliar_bodega LOGIN PASSWORD 'bodega123';

GRANT CONNECT ON DATABASE inventario TO auxiliar_bodega;
GRANT USAGE ON SCHEMA public TO auxiliar_bodega;
GRANT SELECT ON TABLE producto TO auxiliar_bodega;
GRANT UPDATE (stock) ON TABLE producto TO auxiliar_bodega;
