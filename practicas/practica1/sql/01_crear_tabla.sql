-- Etapa 1, Reto 2: estructura formal de producto.
-- Tipos estrictos, clave primaria y regla de negocio sobre el stock.

DROP TABLE IF EXISTS producto;

CREATE TABLE producto (
    id      INTEGER        PRIMARY KEY,
    nombre  TEXT           NOT NULL,
    precio  NUMERIC(10, 2) NOT NULL,
    stock   INTEGER        NOT NULL,
    CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0)
);
