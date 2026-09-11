#!/bin/bash

# ============================================
# Script para gerenciar o container MySQL
# Carrega variáveis do .env e concede permissões
# Uso: ./database.sh [start|stop]
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

# Função para conceder privilégios globais ao usuário
grant_permissions() {
    echo ">>> Concedendo permissões ao usuário '$MYSQL_USER'..."

    # Aguarda o MySQL estar pronto (máx. 60 segundos)
    for i in {1..30}; do
        if docker exec "$CONTAINER_NAME" mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent >/dev/null 2>&1; then
            break
        fi
        echo "Aguardando MySQL iniciar..."
        sleep 2
    done

    # Executa os comandos de concessão como root
    docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" "$CONTAINER_NAME" mysql -uroot <<-EOSQL
        CREATE USER IF NOT EXISTS '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';
        GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'%';
        FLUSH PRIVILEGES;
EOSQL

    echo ">>> Permissões concedidas."
}

# Lógica principal
case "$1" in
    start)
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
                docker start "$CONTAINER_NAME"
            else
                echo ">>> Container já em execução."
            fi
        else
            echo ">>> Criando e iniciando novo container..."
            docker run --name "$CONTAINER_NAME" \
                -e MYSQL_ROOT_PASSWORD="$MYSQL_ROOT_PASSWORD" \
                -e MYSQL_DATABASE="$MYSQL_DATABASE" \
                -e MYSQL_USER="$MYSQL_USER" \
                -e MYSQL_PASSWORD="$MYSQL_PASSWORD" \
                -p 3306:3306 \
                -d "$IMAGE_NAME:$IMAGE_TAG"
        fi

        # Concede permissões sempre que inicia
        grant_permissions
        echo ">>> Servidor MySQL pronto na porta 3306."
        ;;

    stop)
        echo "### Parando e removendo o servidor MySQL ###"
        if container_exists; then
            if container_running; then
                docker stop "$CONTAINER_NAME"
                echo ">>> Container parado."
            fi
            docker rm -f "$CONTAINER_NAME"
            echo ">>> Container removido."
        else
            echo ">>> Container não existe. Nada a fazer."
        fi

        # Remove a imagem se existir
        if image_exists; then
            docker rmi "$IMAGE_NAME:$IMAGE_TAG"
            echo ">>> Imagem '$IMAGE_NAME:$IMAGE_TAG' removida."
        else
            echo ">>> Imagem não existe. Nada a fazer."
        fi

        echo ">>> Tudo removido com sucesso."
        ;;

    *)
        echo "Uso: $0 [start|stop]"
        exit 1
        ;;
esac