# MySQL Server para desenvolvimento local.
#
# A versão é FIXA de propósito: com "mysql:latest" cada rebuild baixava a
# imagem mais recente do Docker Hub, que muda de trilha (8.x LTS -> 26.x
# innovation) e de tamanho sem aviso. Para atualizar, troque a tag abaixo
# (veja as disponíveis em https://hub.docker.com/_/mysql/tags) e rode
# ./database.sh restart
FROM mysql:26.10.0

LABEL org.opencontainers.image.title="mysql-custom" \
      org.opencontainers.image.description="MySQL Server local para desenvolvimento do GzErp" \
      org.opencontainers.image.version="26.10.0" \
      org.opencontainers.image.base.name="docker.io/library/mysql:26.10.0"

EXPOSE 3306

# Healthcheck com login real via TCP (e não "mysqladmin ping"): no socket,
# quem responde durante a inicialização é o servidor temporário, que ainda
# recusa autenticação. O servidor definitivo é o único que escuta em TCP.
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=6 \
    CMD MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -h 127.0.0.1 -uroot -e 'SELECT 1' >/dev/null 2>&1 || exit 1
