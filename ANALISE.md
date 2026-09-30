# Análise do Código Base — GzErp

## 1. Estrutura do Projeto

```
src/main/java/com/erpapi/gzerp/
├── GzErpApplication.java          # Main + dotenv loader
├── config/                         # Configurações
│   ├── SecurityConfig.java         # Spring Security, JWT filter chain
│   ├── JwtConfig.java              # JWT generation/validation
│   ├── JwtAuthenticationFilter.java # JWT filter (extracts & validates token)
│   ├── JwtAuthEntryPoint.java      # 401 entry point
│   ├── PasswordConfig.java         # Argon2 password encoder
│   ├── LimitConfig.java            # Rate limiting rules per endpoint
│   └── SchedulingConfig.java       # Scheduled tasks
├── dto/                            # DTOs (Request/Response)
│   ├── LoginRequestDto.java        # { identifier, password }
│   ├── LoginResponseDto.java       # { token, refreshToken, userId, tenantId, userType }
│   ├── RefreshTokenRequestDto.java # { token }
│   ├── RefreshTokenResponseDto.java # { refreshedToken, token, tokenType, expiresIn }
│   ├── LogoutRequestDto.java       # { refreshToken }
│   ├── TenantRegisterDto.java      # CNPJ, email, razaoSocial, nomeFantasia, partners[], phone
│   ├── TenantResponseDto.java      # CNPJ, email, razaoSocial, nomeFantasia, partners[]
│   ├── PartnersRegisterDto.java    # CPF, fullName, username, email, phone, ownership, salary, password
│   ├── PartnersResponseDto.java    # CPF, name, email, phone, ownership, salary
│   ├── ProductRegisterDto.java     # name, stock, baseSellPrice, costPrice, imageUrl, sku
│   ├── ProductResponseDto.java     # publicId, sku, name, stock, baseSellPrice, costPrice, imageUrl, createdAt, sales
│   ├── EmployeeRegisterDto.java    # (comentado — não usado)
│   ├── EmployeeResponseDto.java    # (comentado — não usado)
│   ├── UsersAccountRegisterDto.java # (não exposto via endpoint)
│   └── UsersAccountsResponseDto.java # (não exposto via endpoint)
├── enums/                          # Enums do domínio
│   ├── Permissions.java            # CREATE_PROD, DELETE_PROD, ALTER_*, REGISTER_PROD
│   ├── Plans.java                  # ENTERPRISE, SCALING, INICIAL, FREE
│   ├── Status.java                 # ACTIVE, INACTIVE, DELETED, PENDENT, BLOCKED
│   └── UserType.java               # PARTNER, EMPLOYEE, ADMIN
├── exceptions/                     # Exceções customizadas
│   ├── EmployeeAlreadyExistException.java
│   ├── InvalidCharactersException.java
│   ├── InvalidCredentialsException.java
│   ├── InvalidProductException.java
│   └── InvalidRefreshTokenException.java
├── exceptionHandler/
│   └── ApiExceptionHandler.java    # @ControllerAdvice — ProblemDetail responses
├── models/                         # Entidades JPA
│   ├── Tenants.java                # CNPJ, email, razaoSocial, nomeFantasia, plan, status
│   ├── Partners.java               # CPF, fullName, email, phone, ownership
│   ├── UsersAccounts.java          # email, username, password, userType, tenant, role
│   ├── Employees.java              # fullName, cpf, salary, phone (FK to users_accounts)
│   ├── Products.java               # publicId, sku, name, stock, baseSellPrice, costPrice, imageUrl
│   ├── RefreshToken.java           # token, userAccount, expiresAt, revoked, replacedBy
│   └── Roles.java                  # name, description, permissions (Set<Permissions>)
├── repositories/                   # Spring Data JPA Repositories
├── services/                       # Serviços de negócio
│   ├── AuthService.java            # CPF/CNPJ validation & formatting
│   ├── CustomUserDetailService.java # UserDetailsService — loads user + builds authorities
│   ├── RefreshTokenService.java    # Create, validate, rotate, revoke refresh tokens
│   ├── TenantsService.java         # Register tenant + partners + user account
│   ├── UsersAccountsService.java   # Register user account (partner)
│   ├── PartnersService.java        # Register partner entity
│   ├── EmployeesService.java       # (quase vazio — endpoints comentados)
│   └── ProductsService.java        # Register product (with SKU/name uniqueness check)
├── resources/                      # REST Controllers
│   ├── AuthResource.java           # POST /api/auth/login, /refresh, /logout
│   ├── TenantsResource.java        # POST /register
│   ├── EmployeesResource.java      # GET /employees, DELETE /employees/{id}
│   └── ProductsResources.java      # POST /products/register
├── security/                       # Filtros de segurança
│   ├── RateLimitFilter.java        # Per-IP rate limiting (ConcurrentHashMap in-memory)
│   ├── RateLimitingBucket.java     # Sliding window counter
│   ├── RateLimitingCounter.java    # Counter with expiry
│   ├── TenantContextFilter.java    # Sets ScopedValue<Long> for tenant isolation
│   └── TenantContext.java          # ScopedValue<Long> TENANT_ID
└── event/                          # Event-driven audit
    ├── ResourceCreatedEvent.java
    └── listener/CreatedResourceListener.java
```

## 2. Endpoints REST Completos

### Endpoints Públicos (sem autenticação)

| Método | Endpoint | Corpo Esperado | Resposta | Descrição |
|--------|----------|---------------|----------|-----------|
| POST | `/register` | `TenantRegisterDto` (cnpj, email, razaoSocial, nomeFantasia?, partners[], phone) | `TenantResponseDto` (201) ou `ProblemDetail` (409) | Cadastra empresa + sócio(s) + conta de login |
| POST | `/api/auth/login` | `LoginRequestDto` ({ identifier, password }) | `LoginResponseDto` (200) | Login por username OU email. Retorna JWT + refresh token |
| POST | `/api/auth/refresh` | `RefreshTokenRequestDto` ({ token }) | `RefreshTokenResponseDto` (200) | Rotaciona refresh token + gera novo access token |
| POST | `/api/auth/logout` | `LogoutRequestDto` ({ refreshToken }) | 204 No Content | Revoga refresh token |

### Endpoints Autenticados (JWT Bearer)

| Método | Endpoint | Headers | Resposta | Descrição |
|--------|----------|---------|----------|-----------|
| GET | `/employees` | Authorization: Bearer {token} | `List<Employees>` (200) ou 204 | Lista TODOS os employees (sem filtro por tenant!) |
| DELETE | `/employees/{id}` | Authorization: Bearer {token} | `202` ou `404` | Remove employee por ID |
| POST | `/products/register` | Authorization: Bearer {token}<br>Content-Type: application/json | `ProductResponseDto` (201) ou `ProblemDetail` (400/409) | Cadastra produto (exige `PERM_CREATE_PROD`) |

### JWT Payload (claims)
```json
{
  "sub": "username",
  "id": 1,
  "tenantId": 1,
  "userType": "PARTNER",
  "roles": ["PERM_CREATE_PROD", "PERM_DELETE_PROD", ...],
  "email": "partner@email.com",
  "iat": 1234567890,
  "exp": 1234568790
}
```

## 3. Modelo de Dados (MySQL)

### Tabelas (Flyway V001 → V009)

| Tabela | Colunas Principais | Relações |
|--------|-------------------|----------|
| `tenants` | id, cnpj, email, razao_social, nome_fantasia, phone, plan, status, demo | 1:N partners |
| `partners` | user_account_id (PK), tenant_id, cpf, full_name, email, phone, ownership | FK users_accounts, tenants |
| `users_accounts` | id, email, username, password, user_type, tenant_id, role_id | FK tenants, roles; 1:1 partners/employees |
| `employees` | user_account_id (PK), tenant_id, full_name, cpf, salary, phone, hire_date | FK users_accounts, tenants |
| `products` | id, public_id, tenant_id, user_id, sku, name, stock, base_price, cost, image, sales | FK tenants, users_accounts |
| `refresh_tokens` | id, token, user_accounts_id, expires_at, revoked, replaced_by, created_at | FK users_accounts |
| `roles` | id, tenant_id, name, is_system, description | FK tenants |
| `role_permissions` | role_id, permission (PK composta) | FK roles |

### Enums no Banco
- `plans`: ENTERPRISE, SCALING, INICIAL, FREE
- `status`: ACTIVE, INACTIVE, DELETED, PENDENT, BLOCKED
- `user_type`: PARTNER, EMPLOYEE, ADMIN

## 4. Segurança

### Autenticação
- **JWT (HS256)** com chave HMAC via BASE64
- **Access Token**: 900.000 ms (15 minutos)
- **Refresh Token**: 604.800.000 ms (7 dias) — armazenado no banco, com rotação
- **Logout**: revoga o refresh token
- **Anti-reuse**: refresh token não pode ser reutilizado (detecta `replaced_by != null`)

### Senhas
- **Argon2** via Spring Security defaults (versão 5.8)

### Autorização
- **Role-based** via `@EnableMethodSecurity`
- **PARTNER/ADMIN**: recebem TODAS as permissões automaticamente
- **EMPLOYEE**: recebe permissões do `Role` associado (padrão: `DefaultRoleEmployees` — sem permissões)
- Formato de authority: `ROLE_PARTNER`, `PERM_CREATE_PROD`, etc.
- `@PreAuthorize("hasAuthority('PERM_CREATE_PROD')")` no `/products/register`

### Rate Limiting (in-memory, por IP)
| Endpoint | Limite | Janela |
|----------|--------|--------|
| `/login` | 5 req | 60s |
| `/refresh` | 10 req | 60s |
| `/register` | 3 req | 3600s |
| Demais | 100 req | 60s |

### Outros
- **CSRF**: desabilitado
- **CORS**: sem configuração explícita (Spring Boot defaults)
- **Tenant isolation**: ScopedValue com VirtualThreads
- **Audit events**: `ResourceCreatedEvent` (listener existe, mas implementação não clara)

## 5. Regras de Negócio do MVP

### Fluxo de Cadastro de Empresa (`POST /register`)
1. Valida CNPJ (checksum), email único, razão social única
2. Cria `Tenant` com plano FREE e status ACTIVE
3. Para cada sócio:
   - Valida CPF (checksum)
   - Cria `UsersAccounts` (PARTNER, senha Argon2)
   - Cria `Partners` (dados pessoais)
   - Associa ao Tenant

### Fluxo de Login (`POST /api/auth/login`)
1. Busca `UsersAccounts` por username OU email
2. Autentica com `AuthenticationManager` (Argon2)
3. Gera JWT com claims: id, tenantId, userType, roles, email
4. Cria refresh token no banco
5. Retorna token + refreshToken + userId + tenantId + userType

### Fluxo de Refresh (`POST /api/auth/refresh`)
1. Valida refresh token (existe, não revogado, não expirado)
2. Verifica anti-reuse (não pode ter sido substituído)
3. Revoga antigo, cria novo refresh token
4. Gera novo access token

### Fluxo de Produto (`POST /products/register`)
1. Verifica permissão `PERM_CREATE_PROD`
2. Verifica SKU único por tenant
3. Verifica Nome único por tenant
4. Cria produto com publicId (UUID)

## 6. Configurações

### application.properties
- **Porta**: 8989
- **Banco**: MySQL via variáveis de ambiente (`DB_URL`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- **Flyway**: habilitado
- **Debug**: SQL, Spring Web, Spring Security
- **Erro**: inclui message e exception
- **Jackson**: não falha em propriedades desconhecidas
- **VirtualThreads**: habilitado

### .env
```
DB_URL=localhost:3306
DB_USERNAME=bruno
DB_PASSWORD=32372871Br!
DB_NAME=ERP
JWT_SECRET=1dwhJLubGeEqNWAs2LDyGa/tiGu4Hyoo2aafKNyJWow=
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000
```

## 7. Performance

### Possíveis Gargalos
1. **Rate limiting in-memory**: perde dados ao reiniciar o servidor (sem Redis)
2. **Queries sem paginação**: `employeesRepo.findAll()` carrega tudo em memória
3. **Eager fetch em refresh_tokens**: `FetchType.EAGER` no relacionamento
4. **Sem cache**: nenhuma configuração de cache (Redis mencionado no README mas não implementado)
5. **TenantContext Filter** roda em todas as requisições autenticadas (overhead mínimo)
6. **Sem connection pool configurado**: usa defaults do HikariCP

## 8. Comparação: Implementado vs. Planejado

### ✅ Implementado
- Cadastro de empresa (Tenant + Partners + User Account)
- Login com JWT (username ou email)
- Refresh token com rotação
- Logout
- Cadastro de produto (com verificação de duplicidade)
- Listagem de employees (sem filtro por tenant)
- Exclusão de employee
- Rate limiting por IP
- Multi-tenant com ScopedValue
- Sistema de permissões (roles + permissions enum)
- Auditoria via eventos (básico)

### ⚠️ Parcialmente Implementado
- Employee registration (código comentado)
- Roles table + permissions (tabelas criadas, mas sem endpoint de gestão)

### ❌ Não Implementado
- Listagem de produtos (`GET /products`)
- Atualização de produto (`PUT /products/{id}`)
- Exclusão de produto (`DELETE /products/{id}`)
- Busca/filtro de produtos
- Atualização de estoque
- Gestão de funcionários (criar, listar, editar, remover)
- Gestão de cargos/permissões (CRUD de roles)
- Integração com marketplaces (Python FastAPI não está no workspace)
- Sincronização de estoque entre marketplaces
- Perceptual hash / matching de imagens
- Notificações (email, WhatsApp, Telegram)
- Dashboard/métricas
- MongoDB (audit logs não implementados)
- Redis (cache não implementado)
- RabbitMQ (mensageria não implementada)
- S3 (armazenamento de imagens não implementado)
- Frontend Angular
- Testes automatizados

### 🐛 Bugs/Problemas Identificados
1. **`/employees` sem filtro por tenant**: expõe dados de todas as empresas
2. **Coluna `password` removida** da tabela `employees` (V006) mas entidade ainda pode referenciar
3. **`sku` NOT NULL DEFAULT 'N/A'** conflita com `unique = true` e lógica de SKU opcional
4. **`role_id NOT NULL DEFAULT 0`** mas roles começam em id=1 — pode causar FK violation
5. **`replaced_by` nullable false** mas pode ser null inicialmente
6. **Rate limiter usa path `/login`** mas endpoint é `/api/auth/login` — **rate limit de login NÃO está funcionando**
7. **`/register` rate limit** mapeia para `/register` mas endpoint real é `POST /register` — funciona por coincidência
8. **Username não pode conter `@`** (verificação em `UsersAccountsService`) mas login aceita email como identifier
