#!/usr/bin/env bash
# Gestión del clúster local de PostgreSQL (sin sudo ni servicio de sistema).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "${SCRIPT_DIR}/env.sh"

mkdir -p "${PGHOST}"

esta_en_marcha() {
    pg_ctl -D "${PGDATA}" status >/dev/null 2>&1
}

cmd_init() {
    if [[ -d "${PGDATA}" && -f "${PGDATA}/PG_VERSION" ]]; then
        echo "El clúster ya existe en ${PGDATA}"
        return 0
    fi
    echo "Inicializando clúster en ${PGDATA} ..."
    initdb -D "${PGDATA}" \
        --locale=C.UTF-8 \
        --encoding=UTF8 \
        --auth-local=trust \
        --auth-host=trust \
        --username="${PGUSER}"
    cat >> "${PGDATA}/postgresql.conf" <<EOF

# Práctica 1 — clúster local
listen_addresses = 'localhost'
port = ${PGPORT}
unix_socket_directories = '${PGHOST}'
logging_collector = on
log_statement = 'none'
EOF
    echo "Clúster inicializado."
}

cmd_start() {
    cmd_init
    if esta_en_marcha; then
        echo "PostgreSQL ya está en marcha (puerto ${PGPORT})."
        return 0
    fi
    mkdir -p "${PGHOST}"
    pg_ctl -D "${PGDATA}" -l "${PGDATA}/practica.log" start
    for _ in {1..30}; do
        if pg_isready -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" >/dev/null 2>&1; then
            break
        fi
        sleep 0.2
    done
    if ! psql -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" -d postgres -Atqc \
        "SELECT 1 FROM pg_database WHERE datname = '${PGDATABASE}'" | grep -q 1; then
        createdb -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" "${PGDATABASE}"
        echo "Base de datos '${PGDATABASE}' creada."
    fi
    echo "PostgreSQL listo: psql -h ${PGHOST} -p ${PGPORT} -U ${PGUSER} -d ${PGDATABASE}"
}

cmd_stop() {
    if esta_en_marcha; then
        pg_ctl -D "${PGDATA}" stop -m fast
    else
        echo "PostgreSQL no está en marcha."
    fi
}

cmd_status() {
    pg_ctl -D "${PGDATA}" status || true
    pg_isready -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" || true
}

cmd_psql() {
    exec psql -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" -d "${PGDATABASE}" "$@"
}

uso() {
    echo "Uso: $0 {init|start|stop|status|psql [args...]}"
    exit 1
}

case "${1:-}" in
    init)   cmd_init ;;
    start)  cmd_start ;;
    stop)   cmd_stop ;;
    status) cmd_status ;;
    psql)   shift; cmd_psql "$@" ;;
    *)      uso ;;
esac
