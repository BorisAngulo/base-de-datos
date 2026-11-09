#!/usr/bin/env bash
# Compila y lanza la práctica 1 (paquete Java practicas.practica1).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "${SCRIPT_DIR}/env.sh"

PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

if [[ -d "${HOME}/.jdks/openjdk-26.0.1" ]]; then
    export JAVA_HOME="${JAVA_HOME:-${HOME}/.jdks/openjdk-26.0.1}"
fi
export PATH="${JAVA_HOME:+$JAVA_HOME/bin:}${PATH}"

MVN="${MVN:-}"
if [[ -z "${MVN}" ]]; then
    if command -v mvn >/dev/null 2>&1; then
        MVN="$(command -v mvn)"
    elif [[ -x /usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn ]]; then
        MVN="/usr/share/idea/plugins/maven-plugin/lib/maven3/bin/mvn"
    else
        echo "No se encontró Maven. Instálalo o exporta MVN=/ruta/a/mvn" >&2
        exit 1
    fi
fi

chmod +x "${SCRIPT_DIR}/db.sh" 2>/dev/null || true
"${SCRIPT_DIR}/db.sh" start

cd "${PROJECT_ROOT}"
CMD="${1:-todo}"
shift || true
ARGS="${CMD}"
if [[ $# -gt 0 ]]; then
    ARGS="${CMD} $*"
fi

mkdir -p "${PRACTICA_ROOT}/evidencias"

if [[ "${CMD}" == "venta" ]]; then
    exec "${MVN}" -q compile exec:java \
        -Dexec.mainClass=practicas.practica1.Practica1 \
        -Dexec.args="${ARGS}"
fi

set -o pipefail
"${MVN}" -q compile exec:java \
    -Dexec.mainClass=practicas.practica1.Practica1 \
    -Dexec.args="${ARGS}" \
    2>&1 | tee "${PRACTICA_ROOT}/evidencias/salida_completa.txt"
