#!/usr/bin/env pwsh
# ============================================
# Script para gerenciar o container MySQL
# Carrega variáveis do .env e concede permissões
# Uso: .\database.ps1 [start|stop]
# ============================================

$ErrorActionPreference = "Continue"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$EnvFile = Join-Path $ScriptDir ".env"

# Carrega o .env
if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            if ($value -match '^"(.+)"$' -or $value -match "^'(.+)'$") {
                $value = $matches[1]
            }
            Set-Item -Path "env:$key" -Value $value
        }
    }
} else {
    Write-Host "Arquivo .env não encontrado em $ScriptDir" -ForegroundColor Red
    exit 1
}

# Variáveis com fallback
$IMAGE_NAME = if ($env:IMAGE_NAME) { $env:IMAGE_NAME } else { "mysql-custom" }
$IMAGE_TAG = if ($env:IMAGE_TAG) { $env:IMAGE_TAG } else { "latest" }
$CONTAINER_NAME = if ($env:CONTAINER_NAME) { $env:CONTAINER_NAME } else { "mysql-container" }

if (-not $env:MYSQL_ROOT_PASSWORD) { Write-Host "Falta MYSQL_ROOT_PASSWORD no .env" -ForegroundColor Red; exit 1 }
if (-not $env:MYSQL_PASSWORD) { Write-Host "Falta MYSQL_PASSWORD no .env" -ForegroundColor Red; exit 1 }

$MYSQL_ROOT_PASSWORD = $env:MYSQL_ROOT_PASSWORD
$MYSQL_DATABASE = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { "ERP" }
$MYSQL_USER = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "bruno" }
$MYSQL_PASSWORD = $env:MYSQL_PASSWORD

# Funções auxiliares
function ImageExists {
    $id = docker images -q "${IMAGE_NAME}:${IMAGE_TAG}" 2>$null
    return ($id -ne "")
}

function ContainerExists {
    $existing = docker ps -a --filter "name=^${CONTAINER_NAME}$" --format "{{.Names}}" 2>$null
    return ($existing -eq $CONTAINER_NAME)
}

function ContainerRunning {
    $running = docker ps --filter "name=^${CONTAINER_NAME}$" --format "{{.Names}}" 2>$null
    return ($running -eq $CONTAINER_NAME)
}

# ============================================================
# FUNÇÃO CORRIGIDA – usa MYSQL_PWD para evitar caracteres especiais
# ============================================================
function GrantPermissions {
    Write-Host ">>> Concedendo permissões ao usuário '$MYSQL_USER'..."

    # Aguarda o MySQL ficar pronto (até 60s)
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        # Usa MYSQL_PWD em vez de -p"senha" – mais seguro
        docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" $CONTAINER_NAME mysql -uroot -e "SELECT 1" 2>$null
        if ($LASTEXITCODE -eq 0) {
            $ready = $true
            break
        }
        Write-Host "Aguardando MySQL iniciar... (tentativa $i)"
        Start-Sleep -Seconds 2
    }

    if (-not $ready) {
        Write-Host "ERRO: MySQL não respondeu a tempo." -ForegroundColor Red
        Write-Host "Últimos logs do container:" -ForegroundColor Yellow
        docker logs $CONTAINER_NAME --tail 20
        exit 1
    }

    # Concede privilégios
    $sql = @"
CREATE USER IF NOT EXISTS '$MYSQL_USER'@'%' IDENTIFIED BY '$MYSQL_PASSWORD';
GRANT ALL PRIVILEGES ON *.* TO '$MYSQL_USER'@'%';
FLUSH PRIVILEGES;
"@

    # O -i mantém o STDIN aberto para o pipe
    $sql | docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" -i $CONTAINER_NAME mysql -uroot

    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERRO ao conceder permissões." -ForegroundColor Red
        exit 1
    }
    Write-Host ">>> Permissões concedidas com sucesso."
}

# ---- Lógica principal ----
$action = $args[0]
switch ($action) {
    "start" {
        Write-Host "### Iniciando o servidor MySQL ###"

        # Verifica porta 3306
        $portInUse = netstat -ano | findstr ":3306.*LISTENING" 2>$null
        if ($portInUse) {
            Write-Host "AVISO: A porta 3306 já está em uso." -ForegroundColor Yellow
            $resp = Read-Host "Digite uma nova porta (ex: 3307) ou Enter para tentar 3306"
            if ($resp -ne "") { $HOST_PORT = $resp } else { $HOST_PORT = 3306 }
        } else {
            $HOST_PORT = 3306
        }

        # Constrói a imagem se não existir
        if (-not (ImageExists)) {
            Write-Host ">>> Imagem não encontrada. Construindo..."
            if (-not (Test-Path "Dockerfile")) {
                Write-Host "ERRO: Dockerfile não encontrado." -ForegroundColor Red
                exit 1
            }
            docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" .
            if ($LASTEXITCODE -ne 0) {
                Write-Host "ERRO ao construir a imagem." -ForegroundColor Red
                exit 1
            }
            Write-Host ">>> Imagem construída."
        } else {
            Write-Host ">>> Imagem já existe."
        }

        # Cria ou inicia o container
        if (ContainerExists) {
            if (-not (ContainerRunning)) {
                Write-Host ">>> Container parado. Iniciando..."
                docker start $CONTAINER_NAME
                if ($LASTEXITCODE -ne 0) {
                    Write-Host "ERRO ao iniciar o container." -ForegroundColor Red
                    exit 1
                }
            } else {
                Write-Host ">>> Container já em execução."
            }
        } else {
            Write-Host ">>> Criando e iniciando novo container na porta $HOST_PORT..."
            docker run --name $CONTAINER_NAME `
                -e "MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD" `
                -e "MYSQL_DATABASE=$MYSQL_DATABASE" `
                -e "MYSQL_USER=$MYSQL_USER" `
                -e "MYSQL_PASSWORD=$MYSQL_PASSWORD" `
                -p "${HOST_PORT}:3306" `
                -d "${IMAGE_NAME}:${IMAGE_TAG}"

            if ($LASTEXITCODE -ne 0) {
                Write-Host "ERRO ao criar o container." -ForegroundColor Red
                exit 1
            }
            Write-Host ">>> Container criado com sucesso."
        }

        # Aguarda o container estar realmente em execução
        $timeout = 30
        $elapsed = 0
        while (-not (ContainerRunning) -and $elapsed -lt $timeout) {
            Write-Host "Aguardando o container iniciar..."
            Start-Sleep -Seconds 2
            $elapsed += 2
        }
        if (-not (ContainerRunning)) {
            Write-Host "ERRO: Container não está em execução." -ForegroundColor Red
            docker logs $CONTAINER_NAME --tail 20
            exit 1
        }

        # Concede permissões
        GrantPermissions

        Write-Host ">>> Servidor MySQL pronto na porta $HOST_PORT." -ForegroundColor Green
    }

    "stop" {
        Write-Host "### Parando o servidor MySQL ###"
        if (ContainerExists) {
            docker stop $CONTAINER_NAME
            Write-Host ">>> Container parado."
        } else {
            Write-Host ">>> Container não existe. Nada a fazer."
        }
    }

    default {
        Write-Host "Uso: $($MyInvocation.MyCommand.Name) [start|stop]"
        exit 1
    }
}