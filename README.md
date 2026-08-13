# GzErp — ERP de E-commerce Multi-Marketplace

> Sistema de gerenciamento de e-commerce que centraliza e sincroniza produtos, estoque, precos e anuncios em diversos marketplaces (Mercado Livre, Shopee e futuros) em um so lugar. Cada empresa cadastra seus dados, socios e funcionarios, e o sistema sincroniza automaticamente seus anuncios atraves de processamento de imagens com Perceptual Hash.

**Linguagem:** Java 21 + Spring Boot 4.0.6  
**Banco de Dados Principal:** MySQL (AWS RDS)  
**Cache:** Redis (AWS ElastiCache)  
**Logs Criticos:** MongoDB (AWS DocumentDB)  
**Mensageria:** RabbitMQ (Amazon MQ)  
**Seguranca:** JWT (stateless) + Spring Security + BCrypt  
**Frontend:** Angular  
**Microservico Marketplaces:** Python + FastAPI  
**Infraestrutura:** AWS (EC2 + RDS + S3 + ElastiCache + DocumentDB + Amazon MQ)  
**Licenca:** Proprietaria (projeto privado)

---

## Indice

- [Visao Geral](#visao-geral)
- [Arquitetura](#arquitetura)
- [Stack Tecnologica](#stack-tecnologica)
- [Estrutura Multi-Tenant](#estrutura-multi-tenant)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Endpoints da API](#endpoints-da-api)
- [Roadmap de Desenvolvimento](#roadmap-de-desenvolvimento)
  - [FASE 0 — Estabilizacao](#fase-0--estabilizacao)
  - [FASE 1 — Finalizacao do Modulo Auth e Tenant](#fase-1--finalizacao-do-modulo-auth-e-tenant)
  - [FASE 2 — Gestao de Produtos](#fase-2--gestao-de-produtos-core-do-erp)
  - [FASE 3 — Microservico Python + Integracao Marketplaces](#fase-3--microservico-python--integracao-marketplaces)
  - [FASE 4 — Processamento de Imagens e Matching](#fase-4--processamento-de-imagens-e-matching)
  - [FASE 5 — Sincronizacao Bidirecional](#fase-5--sincronizacao-bidirecional)
  - [FASE 6 — Notificacoes](#fase-6--notificacoes)
  - [FASE 7 — Frontend Angular](#fase-7--frontend-angular)
  - [FASE 8 — Deploy e Infraestrutura AWS](#fase-8--deploy-e-infraestrutura-aws)
- [Fluxo de Sincronizacao de Imagens](#fluxo-de-sincronizacao-de-imagens)
- [Modelo de Dados](#modelo-de-dados)

---

## Visao Geral

O GzErp e um SaaS de ERP para e-commerce que permite ao usuario:

1. **Cadastrar sua empresa** (Tenant) com CNPJ, socios (Partners) e funcionarios (Employees) com permissoes granulares
2. **Gerenciar produtos internamente** — CRUD completo com categorias, marcas, variantes (SKU, tamanho, cor) e imagens
3. **Conectar marketplaces** — Inicialmente Mercado Livre e Shopee, com arquitetura extensivel para futuros marketplaces
4. **Sincronizar produtos automaticamente** — O sistema pega as fotos dos anuncios nos marketplaces, transforma em Perceptual Hash (pHash) e compara com as fotos do banco interno para identificar o mesmo produto e vincular automaticamente
5. **Sincronizar bidirecionalmente** — Atualizacoes de estoque, preco, descricao e titulo feitas no ERP refletem em todos os marketplaces e vice-versa
6. **Receber notificacoes** — Email, WhatsApp e/or Telegram sobre eventos importantes (sincronizacao concluida, erros, estoque baixo)

---

## Arquitetura

```
                         ┌───────────────────────────────────────┐
                         │         Frontend (Angular)            │
                         └──────────────────┬────────────────────┘
                                            │ REST / HTTP + JWT
                                            │
                 ┌──────────────────────────▼──────────────────────────┐
                 │              API Java (Spring Boot)                  │
                 │                                                       │
                 │   Auth · Tenants · Produtos · Sincronizacao           │
                 │   Perceptual Hash · Regras de Negocio                 │
                 │   Notificacoes · Permissoes                           │
                 │                                                       │
                 └───────┬──────────┬───────────┬────────────┬──────────┘
                         │          │           │            │
                         ▼          ▼           ▼            ▼
                   ┌──────────┐ ┌────────┐ ┌──────────┐ ┌───────────┐
                   │  MySQL    │ │ Redis  │ │ MongoDB  │ │ RabbitMQ  │
                   │  (RDS)    │ │ Cache  │ │  Logs    │ │  (Filas)  │
                   │           │ │        │ │Criticos  │ │           │
                   └──────────┘ └────────┘ └──────────┘ └─────┬─────┘
                                                           │
                              ┌────────────────────────────┘
                              │
                 ┌────────────▼──────────────────────────┐
                 │     Microservico Python (FastAPI)       │
                 │                                          │
                 │   Integracoes de Marketplace:             │
                 │   · Mercado Livre API                     │
                 │   · Shopee Open Platform                  │
                 │   · (Futuros marketplaces)                │
                 │                                          │
                 │   Baixa fotos de anuncios                 │
                 │   Sincroniza estoque/preco/descricao      │
                 │   Recebe pedidos                          │
                 └──────────────────────────────────────────┘
                                                           │
                 ┌──────────────────────────────────────────┘
                 │           S3 (AWS)
                 │   Armazenamento de imagens de produtos
                 └──────────────────────────────────────────────
```

### Por que microservico Python?

A API da Shopee (Shopee Open Platform) e documentada exclusivamente em Python. Em vez de forcar uma integracao em Java que seria dificil de manter, todas as integracoes de marketplace sao centralizadas em um microservico Python (FastAPI) que se comunica com a API Java via RabbitMQ. Isso:

- Mantem cada marketplace isolado e extensivel (adicionar um novo marketplace = criar um novo adapter no Python)
- Desacopla a logica de negocio do ERP (Java) da logica de integracao externa (Python)
- Permite escalar o microservico de marketplaces independentemente da API principal
- Segue o padrao de microservico por responsabilidade

---

## Stack Tecnologica

### Backend Principal (API ERP)

| Tecnologia | Versao | Funcao |
|-----------|--------|--------|
| Java | 21 | Linguagem principal |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Security | — | Autenticacao e autorizacao |
| Spring Data JPA | — | ORM e acesso a dados |
| Flyway | — | Versionamento de schema (migrations) |
| jjwt | 0.12.6 | Geracao e validacao de tokens JWT |
| BCrypt | — | Hash de senhas |
| dotenv-java | 3.2.0 | Variaveis de ambiente (.env) |
| Bean Validation | — | Validacao de DTOs e entidades |

### Microservico Marketplaces

| Tecnologia | Funcao |
|-----------|--------|
| Python 3.12+ | Linguagem do microservico |
| FastAPI | Framework web |
| httpx | Cliente HTTP assincrono para APIs de marketplace |
| pika | Cliente RabbitMQ |
| Pillow | Manipulacao/download de imagens |

### Banco de Dados e Cache

| Tecnologia | Funcao | Deploy |
|-----------|--------|--------|
| MySQL | Dados transacionais (tenants, employees, partners, produtos, anuncios, etc.) | AWS RDS |
| Redis | Cache de hashmaps de imagem, cache de tokens de marketplace, blacklist de JWT | AWS ElastiCache |
| MongoDB | Logs administrativos e criticos do ERP — falhas de sistema, erros de sincronizacao, auditoria | AWS DocumentDB |

### Mensageria

| Tecnologia | Funcao |
|-----------|--------|
| RabbitMQ | Desacoplar sincronizacao de produtos, atualizacao de estoque, processamento de imagens, comunicacao Java ↔ Python |

### Processamento de Imagens

| Tecnologia | Funcao |
|-----------|--------|
| JImageHash (ou imgscalr) | Perceptual Hash (pHash, dHash, aHash) — transforma foto em hashmap e compara com fotos internas |

### Frontend

| Tecnologia | Funcao |
|-----------|--------|
| Angular | SPA do SaaS |
| TypeScript | Linguagem do frontend |
| Tailwind CSS | Estilizacao |
| ngx-charts | Graficos do dashboard |

### Infraestrutura (AWS)

| Servico | Funcao |
|---------|--------|
| EC2 | Hospedagem da API Java e do microservico Python |
| RDS (MySQL) | Banco de dados principal gerenciado |
| S3 | Armazenamento de imagens de produtos |
| ElastiCache (Redis) | Cache gerenciado |
| DocumentDB (MongoDB) | Logs criticos gerenciados |
| Amazon MQ (RabbitMQ) | Mensageria gerenciada |
| ACM | Certificados SSL/TLS |
| CloudWatch | Monitoramento e logs |

### DevOps

| Tecnologia | Funcao |
|-----------|--------|
| Docker | Containerizacao (API Java, Python, MySQL, Redis, MongoDB, RabbitMQ) |
| Docker Compose | Ambiente de desenvolvimento local |
| GitHub Actions | CI/CD (build → test → deploy) |
| Nginx | Reverse proxy e SSL termination |

---

## Estrutura Multi-Tenant

O sistema usa isolamento logico baseado em `tenant_id` — cada empresa (Tenant) tem seus proprios dados isolados.

| Entidade | Descricao |
|----------|-----------|
| **Tenant** | Empresa/organizacao (CNPJ, email, plano, status) |
| **Partner** | Socios da empresa (CPF, participacao, contato) |
| **Employee** | Funcionarios autenticados com permissoes/roles |
| **Product** | Produto interno do catalogo do tenant |
| **ProductVariant** | Variante do produto (SKU, tamanho, cor, preco) |
| **ProductImage** | Imagem do produto com perceptual hash |
| **MarketplaceIntegration** | Configuracao de conexao com cada marketplace |
| **Listing** | Anuncio de um produto em um marketplace especifico |
| **Notification** | Notificacao enviada ao usuario |

## Estrutura do Projeto

```
GzErp/
├── src/
│   ├── main/
│   │   ├── java/com/erpapi/gzerp/
│   │   │   ├── GzErpApplication.java          # Entry point + dotenv loading
│   │   │   ├── config/                        # SecurityConfig, JwtFilter, SecurityUser
│   │   │   ├── dto/                           # DTOs de request/response
│   │   │   ├── enums/                         # Plans, Status
│   │   │   ├── event/                         # Eventos Spring (criacao de recursos)
│   │   │   ├── exceptionHandler/              # @ControllerAdvice global (RFC 7807)
│   │   │   ├── exceptions/                    # Excecoes de dominio
│   │   │   ├── models/                        # Entidades JPA
│   │   │   ├── repositories/                  # Spring Data JPA
│   │   │   ├── resources/                     # Controllers REST
│   │   │   └── services/                      # Regras de negocio
│   │   └── resources/
│   │       ├── application.properties         # Configuracao Spring + DB + JWT
│   │       ├── messages.properties            # Mensagens i18n (EN)
│   │       ├── messages_pt_BR.properties      # Mensagens i18n (PT-BR)
│   │       └── db/migration/                   # Migrations Flyway (V001-V003)
│   └── test/                                   # Testes (JUnit 5)
├── .env                                        # Variaveis de ambiente (NAO commitar)
├── .env_example                                # Template do .env
├── pom.xml                                     # Configuracao Maven
└── docker-compose.yml                          # (Futuro) ambiente de desenvolvimento
```

---

## Endpoints da API

### Endpoints publicos (sem autenticacao)

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/register` | Cadastra uma nova empresa (Tenant) com socios (Partners) |
| POST | `/auth/login` | Autentica um funcionario e retorna token JWT |
| POST | `/employees/register` | Cadastra um novo funcionario (Employee) |

### Endpoints autenticados (requerem JWT)

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| GET | `/employees` | Lista funcionarios (a ser filtrado por tenant) |
| DELETE | `/employees/{id}` | Remove um funcionario |

> Endpoints de produtos, marketplaces, sincronizacao e notificacoes serao implementados nas fases seguintes.

### Exemplo de request — Cadastro de Empresa

```json
POST /register
Content-Type: application/json

{
  "cnpj": "12345678000190",
  "email": "empresa@email.com",
  "razaoSocial": "Minha Empresa LTDA",
  "nomeFantasia": "Minha Empresa",
  "phone": "11999999999",
  "partners": [
    {
      "cpf": "12345678901",
      "name": "Joao Silva",
      "email": "joao@email.com",
      "phone": "11988888888",
      "ownership": 100.00,
      "password": "senha123"
    }
  ]
}
```

### Exemplo de request — Login

```json
POST /auth/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "password": "senha123",
  "tenantId": 1
}
```

### Exemplo de response — Login

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "joao@email.com",
  "tenantId": 1,
  "isAdmin": false
}
```

---

## Roadmap de Desenvolvimento

### FASE 0 — Estabilizacao

> **Objetivo:** Fazer o projeto compilar, limpar bugs, alinhar entidades com migrations e remover codigo morto. Antes de qualquer feature nova.

| # | Tarefa | Arquivos Afetados | Tecnologia |
|---|--------|-------------------|-----------|
| 0.1 | Corrigir bug de compilacao — `TenantsService.java:68` tem `dto.get` que nao e codigo valido. Implementar `getPassword()` no `PartnersRegisterDto` e usar `passwordEncoder.encode(partnerDto.getPassword())` | `TenantsService.java`, `PartnersRegisterDto.java` | Java |
| 0.2 | Corrigir `ApiExceptionHandler` — o construtor recebe `ArrayList<String> errors` como parametro, o que o Spring nao consegue injetar. Remover esse parametro | `ApiExceptionHandler.java` | Java |
| 0.3 | Limpar mensagens de teste — `"Teste, deu o Argument Not Valid aqui."` e `"Teste, deu o MessageNotReadable"` devem ser substituidas por mensagens profissionais via `messages_pt_BR.properties` | `ApiExceptionHandler.java`, `messages*.properties` | Java + i18n |
| 0.4 | Dessincronizacao entidade x migration — Migration V003 adiciona `cpf`, `hire_date`, `roles` na tabela `employees`, mas `Employees.java` nao mapeia esses campos. Decidir: adicionar na entidade ou criar migration V004 que remove as colunas | `Employees.java`, nova migration | Java + Flyway |
| 0.5 | Remover `tenantsRepo` nao usado no `TenantsResource` — injetado mas nunca referenciado | `TenantsResource.java` | Java |
| 0.6 | Corrigir `GET /employees` que vaza senhas — retornar `List<EmployeeResponseDto>` ao inves de `List<Employees>`. Aplicar filtro por `tenantId` extraido do JWT | `EmployeesResource.java`, `EmployeesService.java` | Java |
| 0.7 | Remover codigo morto — `ResourceCreatedEvent` e `CreatedResourceListener` nao sao usados. Integrar (publicar evento nos controllers) ou remover | `event/` package | Java |
| 0.8 | Verificar `.env` no `.gitignore` — o arquivo contem credenciais reais de BD | `.gitignore` | Config |
| 0.9 | Revisar `Employees.java` — getters/setters duplicados (`getTenantid()` e `getTenantId()`), formatacao inconsistente. Padronizar | `Employees.java` | Java |
| 0.10 | Padronizar injecao de dependencia — alguns services usam `@Autowired` por campo, outros por construtor. Padronizar para **construtor** em toda a codebase | Todos os services e resources | Java |
| 0.11 | Adicionar `getPassword()` no `PartnersRegisterDto` — campo `password` existe com `setPassword()` mas sem getter | `PartnersRegisterDto.java` | Java |
| 0.12 | Criar migration de correcao (V004) — alinhar schema com as entidades Java | Nova migration V004 | Flyway/SQL |

---

### FASE 1 — Finalizacao do Modulo Auth e Tenant

> **Objetivo:** Sistema de autenticacao multi-tenant completo e robusto.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 1.1 | Sistema de permissoes/funcoes granular — substituir `boolean isAdmin` por entidade `Role` e `Permission` relacionadas com `Employees`. Migration V005 | Java + JPA + Flyway |
| 1.2 | CRUD completo de Tenants — `GET /tenants/{id}`, `PUT /tenants/{id}` (atualizar), `DELETE /tenants/{id}` (soft delete = status `INACTIVE`) | Java REST |
| 1.3 | CRUD completo de Partners — `GET /tenants/{id}/partners`, `POST /tenants/{id}/partners`, `PUT /partners/{id}`, `DELETE /partners/{id}` | Java REST |
| 1.4 | CRUD completo de Employees — `GET /employees` com filtro por tenant (do JWT), `PUT /employees/{id}`, `DELETE /employees/{id}` com verificacao de permissao | Java REST |
| 1.5 | Refresh Token JWT — `POST /auth/refresh` que gera novo token a partir de um refresh token | Java + jjwt |
| 1.6 | Logout/Revogacao de token — blacklist de tokens no Redis | Java + Redis |
| 1.7 | Validacao de CNPJ — validacao de digito verificador (nao so tamanho) | Java |
| 1.8 | Validacao de CPF — validacao de digito verificador para socios | Java |
| 1.9 | Rate limiting nos endpoints de auth — proteger contra brute force (5 tentativas/min por IP) | Spring Security + Bucket4j ou Redis |
| 1.10 | Testes automatizados do modulo auth — unitarios de services + integracao de endpoints (MockMvc) | JUnit 5 + Mockito + TestContainers |
| 1.11 | Docker Compose para desenvolvimento — MySQL + Redis + MongoDB + RabbitMQ local | Docker |

---

### FASE 2 — Gestao de Produtos (Core do ERP)

> **Objetivo:** CRUD de produtos interno com upload de imagens e armazenamento no S3.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 2.1 | Modelagem de dominio de Produto — entidades `Product`, `ProductVariant` (SKU, tamanho, cor), `ProductImage`, `Category`, `Brand`. Migration V006 | Java + JPA + Flyway |
| 2.2 | CRUD de Produtos — `POST/GET/PUT/DELETE /products` (filtrados por `tenantId` do JWT). DTOs de request/response | Java REST |
| 2.3 | CRUD de Categorias — categorias hierarquicas (categoria pai → subcategorias) | Java REST + JPA |
| 2.4 | CRUD de Marcas — entidade `Brand` | Java REST |
| 2.5 | Upload de imagens para S3 — `POST /products/{id}/images` faz upload para S3 e salva URL no MySQL | Java + AWS S3 SDK |
| 2.6 | Geracao de Perceptual Hash das imagens — ao fazer upload, gerar pHash e armazenar no MySQL/Redis para futura comparacao | Java + JImageHash |
| 2.7 | Cache de queries de produtos no Redis — caching de listagens mais acessadas | Java + Spring Cache + Redis |
| 2.8 | Testes do modulo de produtos | JUnit 5 + TestContainers |

---

### FASE 3 — Microservico Python + Integracao Marketplaces

> **Objetivo:** Microservico Python que integra com APIs de marketplaces, consumindo mensagens do RabbitMQ e reportando a API Java.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 3.1 | Criar projeto do microservico Python — estrutura FastAPI com um adapter por marketplace | Python + FastAPI |
| 3.2 | Comunicacao Java ↔ Python — contrato de mensagens JSON via RabbitMQ. Java publica, Python consome | RabbitMQ |
| 3.3 | Integracao Mercado Livre — OAuth2, endpoints de produtos/anuncios, sincronizacao de estoque/preco | Python + ML API |
| 3.4 | Integracao Shopee — autenticacao e API de produtos da Shopee Open Platform | Python + Shopee API |
| 3.5 | Entidade `MarketplaceIntegration` no Java — registra quais marketplaces cada tenant ativou + tokens OAuth | Java + JPA |
| 3.6 | Endpoint de conexao com marketplace — `POST /marketplaces/{name}/connect` inicia fluxo OAuth | Java REST |
| 3.7 | Sincronizacao de produto → anuncio — produto criado/atualizado no ERP → mensagem no RabbitMQ → Python cria/atualiza anuncio no marketplace | Java → RabbitMQ → Python |
| 3.8 | Webhook receiver — endpoint na API Java para receber callbacks dos marketplaces (pedidos, status de anuncios) | Java REST |
| 3.9 | Logs de sincronizacao no MongoDB — registrar operacoes com detalhes (timestamp, marketplace, produto, status, erro) | Python/Java → MongoDB |
| 3.10 | Docker Compose atualizado — adicionar microservico Python ao compose de desenvolvimento | Docker |

---

### FASE 4 — Processamento de Imagens e Matching

> **Objetivo:** O diferencial do produto — comparar fotos de anuncios de marketplaces com fotos internas para matching automatico.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 4.1 | Pipeline de processamento de imagem — Python detecta anuncio no marketplace, baixa foto, publica no RabbitMQ para Java processar | Python → RabbitMQ → Java |
| 4.2 | Calculo de Perceptual Hash — Java calcula pHash da imagem baixada do anuncio | Java + JImageHash |
| 4.3 | Comparacao de hashes — comparar pHash do anuncio com os pHashes armazenados (com threshold de similaridade) | Java + Redis (indice de hashes) |
| 4.4 | Match automatico — se o hash bater acima do threshold, vincular o anuncio ao produto interno automaticamente | Java |
| 4.5 | Match manual (fallback) — UI para usuario confirmar/rejeitar matches ambiguos e associar manualmente | Java REST + Angular |
| 4.6 | Reavaliacao periodica — job agendado que re-processa anuncios nao vinculados | Spring Scheduling |

---

### FASE 5 — Sincronizacao Bidirecional

> **Objetivo:** Alteracoes no ERP refletem nos marketplaces e vice-versa.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 5.1 | Sincronizacao de estoque — atualizacao no ERP → RabbitMQ → Python atualiza marketplaces | Java → RabbitMQ → Python |
| 5.2 | Sincronizacao de preco — mesmo fluxo para mudanca de preco | Java → RabbitMQ → Python |
| 5.3 | Sincronizacao de descricao/titulo — atualizar no ERP → refletir nos anuncios | Java → RabbitMQ → Python |
| 5.4 | Recebimento de pedidos — Python recebe pedidos dos marketplaces → RabbitMQ → Java processa | Python → RabbitMQ → Java |
| 5.5 | Atualizacao de estoque reversa — pedido no marketplace → descontar estoque no ERP → propagar para outros marketplaces | Python → Java → RabbitMQ → Python |
| 5.6 | Conflict resolution — tratar conflitos (preco alterado diretamente no marketplace vs no ERP). Definir regra de precedencia | Java (business logic) |
| 5.7 | Dashboard de sincronizacao — `GET /sync/status` retorna status de cada marketplace/integracao | Java REST + MongoDB |

---

### FASE 6 — Notificacoes

> **Objetivo:** Manter o usuario informado de eventos importantes.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 6.1 | Sistema de notificacoes (core) — entidade `Notification`, tabela no MySQL, endpoint `GET /notifications` | Java + JPA |
| 6.2 | Integracao de Email — AWS SES (recomendado por ja estar na AWS), template engine Thymeleaf, emails transacionais | Java + AWS SES + Thymeleaf |
| 6.3 | Integracao WhatsApp — Twilio (SDK Java) ou Z-API (APIs BR) | Java + Twilio SDK |
| 6.4 | Integracao Telegram (opcional) — bot do Telegram para alertas | Java + Telegram Bot API |
| 6.5 | Preferencias de notificacao — usuario escolhe quais notificacoes recebe e por qual canal | Java + JPA |
| 6.6 | Notificacoes de eventos — "Sincronizacao concluida", "Erro ao atualizar estoque no ML", "Estoque baixo" | Java → Sistema de notifs |

---

### FASE 7 — Frontend Angular

> **Objetivo:** Interface completa do SaaS.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 7.1 | Setup do projeto Angular — CLI, estrutura modular, routing, HTTP interceptors (JWT) | Angular + TypeScript |
| 7.2 | Paginas de auth — login, registro de empresa, registro de funcionario | Angular |
| 7.3 | Dashboard — visao geral com cards de status (marketplaces conectados, produtos sincronizados, alertas) | Angular + ngx-charts |
| 7.4 | Gestao de produtos — listagem, criacao, edicao, upload de imagens | Angular |
| 7.5 | Gestao de anuncios — listar anuncios por marketplace, status de sincronizacao, matches de imagem | Angular |
| 7.6 | Configuracoes — perfil da empresa, socios, funcionarios, permissoes, conexoes de marketplace | Angular |
| 7.7 | Notificacoes in-app — bell icon com lista, marcacao de lidas | Angular |
| 7.8 | Logs/Auditoria — visualizacao de logs de sincronizacao (do MongoDB) | Angular |

---

### FASE 8 — Deploy e Infraestrutura AWS

> **Objetivo:** Colocar o SaaS em producao.

| # | Tarefa | Tecnologia |
|---|--------|------------|
| 8.1 | Dockerizar API Java — Dockerfile multi-stage, image leve (JRE 21) | Docker |
| 8.2 | Dockerizar microservico Python — Dockerfile Python slim | Docker |
| 8.3 | Provisionar RDS MySQL — instancia, security groups, backups automaticos | AWS RDS |
| 8.4 | Provisionar EC2 para API Java — instancia EC2, deploy via Docker, Nginx reverse proxy | AWS EC2 + Docker + Nginx |
| 8.5 | Provisionar EC2 para Python — instancia separada ou mesmo EC2 com container separado | AWS EC2 + Docker |
| 8.6 | Configurar S3 — buckets para imagens, policies de acesso, lifecycle rules | AWS S3 |
| 8.7 | Configurar ElastiCache Redis — instancia gerenciada | AWS ElastiCache |
| 8.8 | Provisionar MongoDB — DocumentDB (gerenciado) ou EC2 com MongoDB | AWS DocumentDB |
| 8.9 | Provisionar RabbitMQ — Amazon MQ (gerenciado) ou EC2 | AWS Amazon MQ |
| 8.10 | HTTPS/SSL — certificados via ACM ou Let's Encrypt no Nginx/ALB | AWS ACM + Nginx |
| 8.11 | CI/CD — GitHub Actions: build → test → deploy (staging e producao) | GitHub Actions |
| 8.12 | Monitoramento — CloudWatch para logs e metricas | AWS CloudWatch |

---

### Visao Geral das Fases

```
FASE 0 (Estabilizacao)     ← CORRIGIR AGORA
    │
    ▼
FASE 1 (Auth e Tenant)    ← FINALIZAR BASE
    │
    ▼
FASE 2 (Produtos)          ← CORE DO ERP
    │
    ├──→ FASE 3 (Marketplaces Python)  ← INTEGRACOES
    │        │
    │        ▼
    │    FASE 4 (Imagens/Matching)     ← DIFERENCIAL
    │        │
    │        ▼
    │    FASE 5 (Sincronizacao)        ← BIDIRECIONAL
    │
    ▼
FASE 6 (Notificacoes)
    │
    ▼
FASE 7 (Frontend Angular)  ← PODE COMECAR EM PARALELO A PARTIR DA FASE 2
    │
    ▼
FASE 8 (Deploy AWS)
```

---

## Fluxo de Sincronizacao de Imagens

```
1. Microservico Python detecta novo anuncio no marketplace
2. Python baixa a foto do anuncio
3. Python publica mensagem no RabbitMQ: { "imageUrl": "...", "marketplace": "ML", "listingId": "..." }
4. Java consome a mensagem e calcula o Perceptual Hash (pHash) da imagem
5. Java compara o pHash com os pHashes armazenados no banco (via indice no Redis)
6. Se similaridade > threshold (configuravel por tenant):
   → MATCH AUTOMATICO: vincula o anuncio ao produto interno
7. Se similaridade e ambigua:
   → PENDENTE: aguarda confirmacao manual do usuario (match manual via UI)
8. Java atualiza o status do match e notifica o usuario
```

---

## Modelo de Dados

### Entidades atuais (implementadas)

```
Tenants (Empresa)
├── id (PK)
├── cnpj (unique)
├── email (unique)
├── razaoSocial (unique)
├── nomeFantasia
├── phone
├── plan (enum: FREE, INICIAL, SCALING, ENTERPRISE)
├── status (enum: ACTIVE, INACTIVE, DELETED, PENDENT, BLOCKED)
├── demo
├── isAdmin
├── registerAt
└── updatedAt
    │
    ├── 1:N → Partners (Socios)
    │         ├── id (PK)
    │         ├── tenant_id (FK)
    │         ├── cpf
    │         ├── fullName
    │         ├── email
    │         ├── phone
    │         ├── ownership (decimal)
    │         ├── salary (decimal)
    │         └── password
    │
    └── 1:N → Employees (Funcionarios)
              ├── id (PK)
              ├── tenant_id (FK)
              ├── userName
              ├── name
              ├── email
              ├── password
              ├── isAdmin (→ sera substituido por Role/Permission)
              ├── salary
              ├── cpf (na migration, nao mapeado)
              ├── hire_date (na migration, nao mapeado)
              └── roles (na migration, nao mapeado)
```

### Entidades futuras (planejadas)

```
Product (Produto)
├── ProductVariant (SKU, tamanho, cor, preco)
├── ProductImage (url S3, perceptualHash)
├── Category (hierquica)
Brand (Marca)

MarketplaceIntegration (conexao tenant ↔ marketplace)
├── tenant_id
├── marketplace (enum: ML, SHOPEE, ...)
├── oauthTokens
└── status

Listing (anuncio em marketplace)
├── product_id (FK)
├── marketplace (FK)
├── externalId
├── status
└── imageHash

Notification (notificacao ao usuario)
├── tenant_id
├── type
├── message
├── channel (EMAIL, WHATSAPP, TELEGRAM)
└── readAt

Role (papel)
├── name
└── permissions (N:N → Permission)

Permission (permissao granular)
├── name
└── description
```

---

## Migrations Flyway

| Versao | Arquivo | Descricao |
|--------|---------|-----------|
| V001 | `V001__initial_users_table.sql` | Tabela `users` inicial (antes do multi-tenant) |
| V002 | `V002__tenants_table_created.sql` | Tabelas `tenants` e `partners`, FK de users → tenants |
| V003 | `V003__partner_permissions_in_Employees.sql` | Rename `users` → `employees`, add salary/cpf/hire_date/roles, add password em partners |
| V004 | (a ser criada) | Correcao e alinhamento do schema com as entidades Java |

---

## Variaveis de Ambiente

| Variavel | Descricao | Exemplo |
|----------|-----------|---------|
| `DB_URL` | Host e porta do MySQL | `localhost:3306/ERP` |
| `DB_USERNAME` | Usuario do MySQL | `root` |
| `DB_PASSWORD` | Senha do MySQL | `suasenha` |
| `JWT_SECRET` | Chave secreta do JWT (min 32 caracteres) | `sua_chave_secreta_...` |

---

## Status do Projeto

O projeto esta em **fase inicial de desenvolvimento**. O modulo de autenticacao/cadastro esta parcialmente implementado e precisa de estabilizacao (FASE 0) antes de avancar para as features de negocio.

| Modulo | Progresso |
|--------|-----------|
| Autenticacao e Seguranca (JWT) | ~70% |
| Gestao de Tenants/Empresas | ~40% |
| Gestao de Employees/Funcionarios | ~25% |
| Gestao de Socios (Partners) | ~10% |
| Gestao de Produtos | 0% |
| Integracao Marketplaces (ML/Shopee) | 0% |
| Processamento de Imagens | 0% |
| Sincronizacao de Estoque/Precos | 0% |
| Notificacoes | 0% |
| Frontend | 0% |
| Deploy | 0% |
