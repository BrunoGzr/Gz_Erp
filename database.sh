#!/bin/bash

# ============================================
# Script para gerenciar o container MySQL
# Carrega variáveis do .env e concede permissões
# Uso: ./database.sh [start|stop|restart]
#   start   -> sobe o servidor (cria imagem/container se necessário)
#   stop    -> apenas para o servidor, preservando os dados
#   restart -> apaga container, volumes e imagem e recria o banco do zero
# ============================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

# Carrega o .env, se existir
if [ -f "$ENV_FILE" ]; then
    set -a            # exporta todas as variáveis definidas
    source "$ENV_FILE"
    set +a
else
    echo "Arquivo .env não encontrado em $SCRIPT_DIR"
    exit 1
fi

# Valores padrão para variáveis opcionais
IMAGE_NAME="${IMAGE_NAME:-mysql-custom}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
CONTAINER_NAME="${CONTAINER_NAME:-mysql-container}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:?Variável MYSQL_ROOT_PASSWORD não definida}"
MYSQL_DATABASE="${MYSQL_DATABASE:-ERP}"
MYSQL_USER="${MYSQL_USER:-bruno}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:?Variável MYSQL_PASSWORD não definida}"
# Porta usada no host (pode ser sobrescrita: HOST_PORT=3307 ./database.sh start)
HOST_PORT="${HOST_PORT:-3306}"

# Função para verificar se a imagem existe
image_exists() {
    docker image inspect "$IMAGE_NAME:$IMAGE_TAG" > /dev/null 2>&1
}

# Função para verificar se o container existe (mesmo parado)
container_exists() {
    docker ps -a --filter "name=^${CONTAINER_NAME}$" --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"
}

# Função para verificar se o container está em execução
container_running() {
    docker ps --filter "name=^${CONTAINER_NAME}$" --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"
}

# Função para verificar se a porta já está ocupada no host
port_in_use() {
    local port="$1"
    if command -v ss >/dev/null 2>&1; then
        ss -ltn "sport = :$port" 2>/dev/null | grep -q LISTEN
    else
        return 1
    fi
}

# Função para conceder privilégios globais ao usuário
grant_permissions() {
    echo ">>> Concedendo permissões ao usuário '$MYSQL_USER'..."

    # Conexões usam TCP (-h 127.0.0.1) de propósito: durante a inicialização o
    # container sobe um servidor temporário, acessível só por socket
    # (--skip-networking), e apenas o servidor definitivo aceita TCP. Assim
    # nenhum comando cai no servidor temporário, onde um CREATE USER adiantado
    # quebra o próprio entrypoint ("ERROR 1396: User account already exists").
    # Os comandos são idempotentes, então repetir é seguro. Máx. 120 segundos.
    local sql
    sql="CREATE USER IF NOT EXISTS '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'%';
FLUSH PRIVILEGES;"

    local attempt ok=false
    for attempt in {1..60}; do
        # O -i é obrigatório: sem ele o stdin não é repassado e o mysql
        # encerra sem executar nada, retornando sucesso.
        if printf '%s\n' "$sql" | docker exec -i -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" \
            "$CONTAINER_NAME" mysql -h 127.0.0.1 -uroot >/dev/null 2>&1; then
            ok=true
            break
        fi
        if ! container_running; then
            echo "ERRO: o container '$CONTAINER_NAME' parou durante a inicialização." >&2
            docker logs "$CONTAINER_NAME" --tail 20 >&2
            exit 1
        fi
        echo "Aguardando MySQL iniciar... (tentativa $attempt)"
        sleep 2
    done

    if [ "$ok" != true ]; then
        echo "ERRO: não foi possível conceder permissões em 120s." >&2
        echo ">>> Últimos logs do container:" >&2
        docker logs "$CONTAINER_NAME" --tail 20 >&2
        exit 1
    fi

    # Verificação final: o usuário da aplicação consegue autenticar?
    for attempt in {1..10}; do
        if docker exec -e MYSQL_PWD="$MYSQL_PASSWORD" "$CONTAINER_NAME" \
            mysql -h 127.0.0.1 -u"$MYSQL_USER" -e 'SELECT 1' >/dev/null 2>&1; then
            echo ">>> Permissões concedidas (login de '$MYSQL_USER' validado)."
            return 0
        fi
        sleep 2
    done

    echo "ERRO: usuário '$MYSQL_USER' não consegue autenticar no banco." >&2
    echo ">>> Últimos logs do container:" >&2
    docker logs "$CONTAINER_NAME" --tail 20 >&2
    exit 1
}

# Sobe o servidor (cria a imagem e o container se necessário)
start_db() {
    echo "### Iniciando o servidor MySQL ###"

    # Constrói a imagem se não existir
    if ! image_exists; then
        echo ">>> Imagem não encontrada. Construindo..."
        docker build -t "$IMAGE_NAME:$IMAGE_TAG" .
    else
        echo ">>> Imagem já existe."
    fi

    # Cria ou inicia o container
    if container_exists; then
        if ! container_running; then
            echo ">>> Container parado. Iniciando..."
            docker start "$CONTAINER_NAME" >/dev/null
        else
            echo ">>> Container já em execução."
        fi
    else
        echo ">>> Criando e iniciando novo container..."
        if port_in_use "$HOST_PORT"; then
            echo "ERRO: a porta $HOST_PORT já está em uso no host." >&2
            echo ">>> Escolha outra porta, por exemplo: HOST_PORT=3307 $0 start" >&2
            exit 1
        fi
        if ! docker run --name "$CONTAINER_NAME" \
            -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
            -e MYSQL_DATABASE="$MYSQL_DATABASE" \
            -e MYSQL_USER="$MYSQL_USER" \
            -e MYSQL_PASSWORD="$MYSQL_PASSWORD" \
            -p "$HOST_PORT:3306" \
            -d "$IMAGE_NAME:$IMAGE_TAG" >/dev/null; then
            echo "ERRO ao criar o container. Últimos logs:" >&2
            docker logs "$CONTAINER_NAME" --tail 20 >&2 || true
            exit 1
        fi
    fi

    # Concede permissões sempre que inicia
    grant_permissions
    echo ">>> Servidor MySQL pronto na porta $HOST_PORT."
}

# Remove container, volumes e imagem (reset completo do banco)
destroy_db() {
    echo "### Removendo o servidor MySQL (reset) ###"

    if container_exists; then
        # Coleta os volumes antes de remover o container
        local volumes
        volumes=$(docker inspect --format='{{range .Mounts}}{{if .Name}}{{.Name}}{{"\n"}}{{end}}{{end}}' "$CONTAINER_NAME" 2>/dev/null | grep -v '^$' || true)

        if container_running; then
            docker stop "$CONTAINER_NAME" >/dev/null
            echo ">>> Container parado."
        fi
        docker rm -f "$CONTAINER_NAME" >/dev/null
        echo ">>> Container removido."

        # Remove os volumes coletados
        if [ -n "$volumes" ]; then
            while IFS= read -r vol; do
                echo ">>> Removendo volume '$vol'..."
                docker volume rm "$vol" >/dev/null 2>&1 || echo ">>> Aviso: volume '$vol' não pôde ser removido."
            done <<< "$volumes"
            echo ">>> Volumes removidos."
        fi
    else
        echo ">>> Container não existe. Nada a fazer."
    fi

    # Remove a imagem se existir
    if image_exists; then
        if docker rmi "$IMAGE_NAME:$IMAGE_TAG" >/dev/null 2>&1; then
            echo ">>> Imagem '$IMAGE_NAME:$IMAGE_TAG' removida."
        else
            echo ">>> Aviso: imagem '$IMAGE_NAME:$IMAGE_TAG' está em uso por outro container e não foi removida."
        fi
    else
        echo ">>> Imagem não existe. Nada a fazer."
    fi

    echo ">>> Banco removido."
}

# Lógica principal
case "${1:-}" in
    start)
        start_db
        ;;

    stop)
        echo "### Parando o servidor MySQL ###"
        if ! container_exists; then
            echo ">>> Container não existe. Nada a fazer."
        elif ! container_running; then
            echo ">>> Container já está parado."
        else
            docker stop "$CONTAINER_NAME" >/dev/null
            echo ">>> Container parado. Os dados foram preservados."
        fi
        ;;

    restart)
        echo "### Reiniciando o banco do zero ###"
        destroy_db
        start_db
        ;;

    *)
        echo "Uso: $0 [start|stop|restart]"
        exit 1
        ;;
esac