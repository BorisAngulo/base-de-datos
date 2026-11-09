#!/usr/bin/env bash
# Variables de conexión para el clúster local de PostgreSQL de la práctica.
PRACTICA_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export PRACTICA_ROOT
export PGDATA="${PRACTICA_ROOT}/.pgdata"
export PGHOST="${PRACTICA_ROOT}/.pgsocket"
export PGPORT="${PGPORT:-55432}"
export PGUSER="${PGUSER:-practica}"
export PGDATABASE="${PGDATABASE:-inventario}"
export PGCLIENTENCODING="UTF8"
