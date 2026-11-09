--
-- PostgreSQL database dump
--

\restrict IDdzCYOkLpyekfXe2Uz5XPsF6gtmoozqnBcvyqYeH2YrLzQ6DkaHYDiNr9yfhbH

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: producto; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.producto (
    id integer NOT NULL,
    nombre text NOT NULL,
    precio numeric(10,2) NOT NULL,
    stock integer NOT NULL,
    CONSTRAINT chk_stock_no_negativo CHECK ((stock >= 0))
);


--
-- Data for Name: producto; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.producto VALUES (1, 'Auriculares Bluetooth Pulse X', 59.99, 25);
INSERT INTO public.producto VALUES (2, 'Cargador USB-C GaN 65W', 19.50, 8);
INSERT INTO public.producto VALUES (3, 'Mouse inalámbrico Orbit Mini', 34.90, 1);


--
-- Name: producto producto_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producto
    ADD CONSTRAINT producto_pkey PRIMARY KEY (id);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: -
--

GRANT USAGE ON SCHEMA public TO auxiliar_bodega;


--
-- Name: TABLE producto; Type: ACL; Schema: public; Owner: -
--

GRANT SELECT ON TABLE public.producto TO auxiliar_bodega;


--
-- Name: COLUMN producto.stock; Type: ACL; Schema: public; Owner: -
--

GRANT UPDATE(stock) ON TABLE public.producto TO auxiliar_bodega;


--
-- PostgreSQL database dump complete
--

\unrestrict IDdzCYOkLpyekfXe2Uz5XPsF6gtmoozqnBcvyqYeH2YrLzQ6DkaHYDiNr9yfhbH

