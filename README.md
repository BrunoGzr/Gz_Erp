# GzErp — ERP de E-commerce Multi-Tenant

> Sistema de gerenciamento de e-commerce que centraliza e sincroniza a operação de um E-commerce em diversos marketplaces em um so lugar. Cada empresa cadastra seus dados, socios e funcionarios, e o sistema sincroniza seus anuncios e centraliza as atualizações de dados em um só lugar.

**Linguagem:** Java 25 + Spring Boot 4.0.6  
**Banco de Dados Principal:** MySQL (AWS RDS)  
**Cache:** Redis (AWS ElastiCache)  
**Logs Criticos:** MongoDB (AWS DocumentDB)  
**Mensageria:** RabbitMQ (Amazon MQ)  
**Seguranca:** JWT (stateless) + Spring Security + Bcrypt 

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
2. **Gerenciar produtos internamente** — Produtos cadastrados no sistema para sincronização posterior com os marketplaces
3. **Sincronização com Marketplaces** — Sincroniza dados de produtos entre Marketplaces, tornando a operação sincrona. 
4. **Sincronização via HashMap / SKU** — O sistema pega as fotos dos anuncios nos marketplaces, transforma em Perceptual Hash (pHash) e compara com as fotos do banco interno para identificar o mesmo produto e vincular automaticamente
5. **Sincronizar bidirecionalmente** — Atualizacoes de estoque, preco, descricao e titulo feitas no ERP refletem em todos os marketplaces e vice-versa
6. **Receber notificacoes** — Email, WhatsApp e/or Telegram sobre eventos importantes (sincronizacao concluida, erros, estoque baixo, logs criticos, promoções, etc)

---

## Arquitetura

```
                         ┌───────────────────────────────────────┐
                         │         Frontend (Angular)            │
                         └──────────────────┬────────────────────┘
                                            │ REST / HTTP + JWT
                                            │
                 ┌──────────────────────────▼────────────────────────────┐
                 │              API Java (Spring Boot)                   │
                 │                                                       │
                 │   Auth · Tenants · Produtos · Sincronizacao           │
                 │   Perceptual Hash · Regras de Negocio                 │
                 │   Notificacoes · Permissoes                           │
                 │                                                       │
                 └───────┬──────────┬───────────┬────────────┬───────────┘
                         │          │           │            │
                         ▼          ▼           ▼            ▼
                   ┌──────────┐ ┌────────┐ ┌──────────┐ ┌────────────┐
                   │  MySQL   │ │ Redis  │ │  MongoD  │ │ RabbitMQ   │
                   │  (RDS)   │ │ Cache  │ │   Logs   │ │  (Filas)   │
                   │          │ │        │ │ Criticos │ │            │
                   └──────────┘ └────────┘ └──────────┘ └─────┬──────┘
                                                              │
                              ┌───────────────────────────────┘
                              │
                 ┌────────────▼──────────────────────────────┐
                 │     Microservico Python (FastAPI)         │
                 │                                           │
                 │   Integracoes de Marketplace:             │
                 │   · Mercado Livre API                     │
                 │   · Shopee Open Platform                  │
                 │   · (Futuros marketplaces)                │
                 │                                           │
                 │   Baixa fotos de anuncios                 │
                 │   Sincroniza estoque/preco/descricao      │
                 │   Recebe pedidos                          │
                 └──────────────────────┬────────────────────┘
                                        │                   
                 ┌──────────────────────▼───────────────────────┐
                 │           S3 (AWS)                           │
                 │   Armazenamento de imagens de produtos       │
                 └──────────────────────────────────────────────┘
```

### Por que microservico Python?

Devido a volatilidade de integração das APIs, todas as integracoes de marketplace sao centralizadas em um microservico Python (FastAPI) que se comunica com a API Java via RabbitMQ. Isso:

- Mantem cada marketplace isolado e extensivel (adicionar um novo marketplace = criar um novo adapter no Python)
- Permite a escalabilidade e confiabilidade padrão do Java, combinadas com a praticidade e velocidade no desenvolvimento do python. Assim desacopla a logica de negocio do ERP (Java) da logica de integracao externa (Python)
- Segue o padrao de microservico por responsabilidade

---

## Stack Tecnologica

### Backend Principal (API ERP)

| Tecnologia | Versao | Funcao |
|-----------|--------|--------|
| Java | 26     | Linguagem principal |
| Spring Boot | 4.0.6  | Framework principal |
| Spring Security | —      | Autenticacao e autorizacao |
| Spring Data JPA | —      | ORM e acesso a dados |
| Flyway | —      | Versionamento de schema (migrations) |
| jjwt | 0.13.0 | Geracao e validacao de tokens JWT |
| BCrypt | —      | Hash de senhas |
| dotenv-java | 3.2.0  | Variaveis de ambiente (.env) |
| Bean Validation | —      | Validacao de DTOs e entidades |

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

O sistema usa isolamento logico baseado em ScopedValue em VirtualThreads, cada requisição é feita com seu proprio contexto assim cada empresa (Tenant) tem seus proprios dados isolados no Id (Tenant_id).

| Entidade | Descricao                                                                                                |
|----------|----------------------------------------------------------------------------------------------------------|
| **Tenant** | Empresa/organizacao (CNPJ, email, plano, status)                                                         |
| **Partner** | Socios da empresa (CPF, participacao, contato)                                                           |
| **Employee** | Funcionarios autenticados com permissoes/roles                                                           |
| **Product** | Produto interno do catalogo do tenant com variações e URL para a imagem, e linkagem para os marketplaces |
| **MarketplaceIntegration** | Configuracao de conexao com cada marketplace                                                             |
| **Notification** | Notificacao enviada ao usuario                                                                           |


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
  "identifier": "joao@email.com", 
  "password": "senha123",
}
```
Ou via username
```json
POST /auth/login
Content-Type: application/json

{
  "identifier": "joao", 
  "password": "senha123",
}
```

### Exemplo de response — Login

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9....",
  "refreshToken": "6bd35829...",
  "userId": X,
  "tenantId": X,
  "userType": "PARTNER",
  "expirationIn": 900,
  "type": "Bearer"
}
```

---

## Fluxo de Sincronizacao de produtos baseado nas Imagens

```
1. Usuario solicita sincronização do produto no Db interno, para o marketplace XXXX.
2. Backend principal em Java solicita ao Microsservice em python todos produtos da categoria, via RabbitMQ. 
3. Python publica mensagem no RabbitMQ: { "imageUrl": "...", "marketplace": "ML", "listingId": "..." }
4. Java consome a mensagem e calcula o Perceptual Hash (pHash) da imagem
5. Java compara o pHash com os pHashes armazenados no banco (via indice)
6. Se similaridade > threshold (configuravel):
   → MATCH AUTOMATICO: vincula o anuncio ao produto interno
7. Se similaridade e ambigua:
   → PENDENTE: aguarda confirmacao manual do usuario (match manual via UI)
8. Java atualiza o status do match e notifica o usuario
```

---





