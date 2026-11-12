# Change.md — Padronização de UI/UX (frontend)

**Data:** 08/11/2026  
**Escopo:** padronização visual, acessibilidade e responsividade do frontend Angular.  
**Restrições respeitadas:** nenhuma regra de negócio alterada, nenhum componente
novo criado, nenhuma lógica/TypeScript modificada, nenhuma alteração de git
(stage, commit, branch ou histórico).

Este documento registra **o que mudou** e **o que foi deixado de fora** nesta
refatoração. O padrão resultante está descrito em [`Project.md`](./Project.md).

---

## 1. Resumo executivo

A refatoração concentrou a aparência do produto em **tokens globais**, removeu
duplicação de paleta nos componentes, carregou a fonte declarada, padronizou
landmarks e foco, estilizou a tela de login (que estava sem CSS) e ajustou
responsividade onde era possível sem tocar em estado ou comportamento.

**Não houve mudança de lógica.** Nenhum arquivo `.ts`, rota, guard, interceptor,
serviço, modelo ou `app.config.ts` foi alterado.

---

## 2. Arquivos alterados

| Arquivo | Tipo de mudança |
|---|---|
| `Frontend/Erp/src/styles.css` | Reescrito: tokens globais, base, foco, seleção, reduced motion |
| `Frontend/Erp/src/index.html` | `lang`, título, meta tags, fonte Inter |
| `Frontend/Erp/src/app/app.html` | Raiz passou de `<main>` para `<div class="app-root">` |
| `Frontend/Erp/src/app/app.css` | Seletor `.app-root`, `100dvh` |
| `Frontend/Erp/src/app/features/auth-login-component/auth-login-component.html` | Landmark `<main>`, remoção de texto de scaffold, `for/id`, `autocomplete`, erro acessível |
| `Frontend/Erp/src/app/features/auth-login-component/auth-login-component.css` | Estilo completo da tela de login (antes vazio) |
| `Frontend/Erp/src/app/features/header-component/header-component.css` | Tokens, `100dvh`, foco visível, z-index |
| `Frontend/Erp/src/app/features/header-component/topbar-component.css` | Aliases de tokens, foco, popovers mobile, z-index, fonte |
| `Frontend/Erp/src/app/features/products-component/products-components.css` | Tokens, padding responsivo, media queries |
| `Frontend/Erp/src/app/features/products-component/products-components-select.css` | Tokens de menu/scrollbar |
| `Frontend/Erp/src/app/features/products-component/products-components-ui.css` | Tokens, alvos de toque, skeleton, toast, z-index |
| `Frontend/Erp/src/app/features/products-component/products-components-table.css` | Token de scrollbar |
| `Frontend/Erp/src/app/features/products-component/products-components-drawer.css` | Tokens de perigo/atenção, `z-index`, placeholder |
| `Frontend/Erp/src/app/features/products-component/products-components-grid.css` | Tokens de tons, marketplace, foco |
| `Frontend/Erp/src/app/features/products-component/products-components-kpi.css` | Tokens de violeta/marketplace, foco |
| `Frontend/Erp/src/app/features/products-component/products-components.html` | `role` de toast por tom (erro = alert) |
| `Project.md` | **Novo** — guia de estilo/consistência para agentes |
| `Change.md` | **Novo** — este registro |

---

## 3. Mudanças por tema

### 3.1 Tokens globais e base

- Criado o conjunto `--gz-*` em `:root` cobrindo:
  superfícies, texto, cores semânticas, marcas de marketplace, tipografia,
  raios, sombras, transições, layout, camadas, skeleton e fundo de mídia.
- Reset global de `box-sizing`.
- `color-scheme: dark` no `html`.
- `body` passou a usar `--gz-font-sans`, `--gz-text`, `--gz-bg`, `100dvh`.
- `button/input/select/textarea` herdam tipografia e cor.
- `::selection` padronizado.
- Anel de foco único via `:where(...):focus-visible`, com especificidade zero
  para permitir override local.
- `prefers-reduced-motion` global cobre todas as animações/transições.

### 3.2 Identidade e tipografia

- `index.html`: `lang="en"` → `lang="pt-BR"`.
- Título: “Frontend de Teste” → “GzErp — ERP de E-commerce”.
- Adicionadas `description` e `theme-color`.
- Carregada a fonte **Inter** (400/500/600/700) via Google Fonts com
  `preconnect` e `display=swap`; fallback local permanece no token.
- Badge de notificação migrou de `0.58rem` para `--gz-font-size-2xs`.

### 3.3 Login

- Removido o texto de scaffold `<p>auth-login-component works!</p>`.
- Tela agora tem `<main>`, card centralizado, gradiente sutil, borda, raio 24px
  e sombra.
- Labels associados aos inputs (`for`/`id`).
- Adicionados `autocomplete="username"` e `autocomplete="current-password"`.
- Mensagem de erro com `role="alert"` e `aria-live="polite"`.
- Botão estilizado; estado `isLoading` (já existente) usa opacidade e spinner
  apenas via CSS, sem alterar lógica.
- Layout compacto em ≤480px.

> O `h2` do login virou `h1` para refletir o título primário da tela.

### 3.4 Landmarks e semântica

- A raiz da aplicação deixou de ser `<main>`; agora é `<div class="app-root">`.
- O login passa a ser o `<main>` da tela pública.
- O shell autenticado mantém seu `<main class="app-content">`.
- Resultado: nunca há dois `<main>` aninhados/ativos ao mesmo tempo.

### 3.5 Migração de paleta nos componentes

- Sidebar: cores literais substituídas por `--gz-*`; larguras e transição agora
  referenciam `--gz-sidebar-*` e `--gz-transition-slow`.
- Topbar: bloco `:host` agora faz alias dos tokens; popovers usam
  `--gz-panel-elevated`; badge usa token de fonte; `z-index` tokenizado.
- Catálogo: `:host` faz alias dos tokens (`--bg`, `--panel`, `--p2`, etc.),
  mantendo compatibilidade com as classes existentes; cores literais de botões,
  KPIs, filtros, tabela, drawer, grid e skeleton foram trocadas por tokens.
- Marca Shopee/Mercado Livre agora usa `--gz-shopee`/`--gz-mercadolivre`.
- Textos de erro/atenção/sucesso usam `--gz-danger-text`,
  `--gz-warning-text`, `--gz-success-text`.
- Não restou nenhum valor hexadecimal de cor nos CSS de componentes; os únicos
  literais restantes são `rgba(...)` de transparência contextual.

### 3.6 Responsividade

- Catálogo: gutter lateral reduzido de `clamp(64px, 5.83vw, 210px)` para
  `clamp(20px, 3.5vw, 64px)`.
- `≤900px`: padding do catálogo reduzido.
- `≤600px`: padding mínimo; cabeçalho de ações em largura total; KPIs com colunas
  menores; busca em 100%; selects diretos em 2 colunas; page-size sem `margin-left`.
- `≤520px`: popovers da topbar ocupam `calc(100vw - 32px)`.
- `100dvh` com fallback `100vh` em raiz, shell, sidebar, conteúdo e login.
- Login compacto em `≤480px`.

### 3.7 Acessibilidade

- Foco visível adicionado a itens da sidebar, pin, toggle, logout, botões da
  topbar, itens de notificação, link de ajuda e resultados de busca.
- Alvos de toque aumentados: `search-clear` 22→28px, `toast-close` 24→28px,
  `bulk-close` 26→30px.
- Toasts de erro agora usam `role="alert"`; sucesso/info usam `role="status"`
  (mudança apenas de atributo no template, baseada no tom já existente).
- Login com erro acessível e labels associados.
- Foco global padronizado; nenhum componente removeu `outline` sem substituto.
- Reduced motion global; o catálogo e a topbar mantêm suas regras específicas.

### 3.8 Camadas (`z-index`)

- Sidebar, topbar, popovers, backdrop, drawer e toasts agora usam os tokens
  `--gz-z-*`; nenhum valor acima de 1300 foi introduzido.

---

## 4. O que NÃO foi alterado (e por quê)

Estes itens são **funcionais** e ficaram fora por instrução explícita:

- **Rotas** (`app.routes.ts`), guards, interceptors e `app.config.ts`.
- **Autenticação/sessão**: login, refresh token, logout, `returnUrl`.
- **Botões e handlers** do catálogo (editar, excluir, exportar, adicionar,
  ações em massa, vincular marketplace).
- **Dados mockados**: KPIs (`SIMULATED_SUMMARY`), notificações e ajuda.
- **Permissões** (`PERM_*`) na UI.
- **Endpoints** e contratos (`/api/products/overall`, `/api/products/categories`).
- **Dashboard legado**: template e TS não foram tocados.
- **Sidebar off-canvas no mobile**, troca automática tabela→cards, navegação por
  setas em `listbox` e focus trap do drawer de revisão.
- **Unificação do conjunto de ícones** (inclui substituir `public/*.svg` por
  SVG inline/sprite).
- **Criação de componentes**, serviços, stores ou qualquer arquivo de lógica.
- **Git**: nenhuma operação de stage/commit/branch foi executada.

As pendências acima continuam listadas no `Project.md` §10 e no diagnóstico
funcional anterior.

---

## 5. Validação

Executado em `Frontend/Erp`:

```bash
npm run build
```

Resultado: **build concluído com sucesso** em 09/11/2026 01:08 UTC
(`Application bundle generation complete`).

Avisos pré-existentes de budget de CSS por componente (warning de 4kB), todos
abaixo do limite de erro:

| Arquivo | Tamanho |
|---|---|
| `products-components-grid.css` | 7,91 kB |
| `header-component.css` | 7,92 kB |
| `products-components.css` | 7,66 kB |
| `products-components-ui.css` | 7,58 kB |
| `topbar-component.css` | 7,17 kB |
| `products-components-drawer.css` | 5,08 kB |

Esses avisos já existiam antes da padronização e são consequência do tamanho dos
componentes, não de erro de sintaxe. A correção recomendada é dividir os
componentes, não aumentar o budget.

Também foi iniciado `ng serve` em `localhost:4300` para inspeção visual; a porta
4200 já estava em uso no ambiente do usuário.

---

## 6. Notas e riscos

- A fonte Inter depende de rede (Google Fonts). Sem internet, o fallback
  `Segoe UI/system-ui` mantém a interface funcional, com leve diferença visual.
- Variáveis CSS globais herdam para componentes com encapsulamento emulado; os
  aliases `--bg: var(--gz-bg)` preservam as classes existentes do catálogo.
- O login agora renderiza spinner via CSS quando o botão está `disabled`. O botão
  só é desabilitado por `isLoading` na lógica atual; se isso mudar, o spinner
  deve ser revisto.
- A troca de `<main>` na raiz não altera rotas nem layout; apenas corrige o
  landmark. O shell autenticado continua com seu próprio `<main>`.
- Nenhum novo componente foi criado; a refatoração é reversível por arquivo, mas
  **não foi feito commit** por instrução.

---

## 7. Próximos passos sugeridos (fora desta entrega)

1. Revisão visual nas larguras 360/768/1024/1366/1920px.
2. Decidir sobre self-host da fonte Inter.
3. Unificar ícones em sprite SVG.
4. Tratar as pendências funcionais do `Project.md` §10 em uma entrega separada,
   com testes e aprovação de produto.

---

## 8. Atualização do guia (08/11/2026)

Revisão do `Project.md` para cobrir o escopo completo do frontend e orientar
agentes de código:

- escopo explicitamente restrito a `Frontend/Erp`;
- mapa de arquivos, rotas, papéis, nomenclatura e fluxo de nova tela;
- convenções Angular 22 (zoneless/OnPush, `@Service`, signals, control flow,
  `input/output/model`, `@HostListener` como legado);
- separação entre classes locais do catálogo e padrões reutilizáveis;
- contrato de URL do catálogo, formulários, testes, budgets e baseline;
- correções factuais de breakpoints, `z-index`, drawer de revisão, login,
  dashboard legado e dívidas de acessibilidade;
- definição de pronto separada entre código novo/linhas tocadas e dívida legada.

Nenhum arquivo de código foi alterado nesta revisão.

---

## 9. Implementação — ações de produto, deep links e status (11/11/2026)

Esta entrega saiu da padronização visual e implementou o fluxo funcional dos
produtos no catálogo.

### Rotas novas
- `/produtos/novo` — criação de produto.
- `/produtos/:publicId` — detalhe do produto (deep link).
- `/produtos/:publicId/editar` — edição de produto.
- `HeaderComponent` passou a usar `paths: 'subset'` para manter “Produtos”
  ativo nas rotas filhas.

### Catálogo
- Nome, foto, card e botão “Abrir produto” navegam para o detalhe.
- Pencil navega para a edição.
- “Mais opções” virou menu contextual com abrir, editar, duplicar, copiar
  link, copiar SKU, ver anúncios, alterar status e excluir.
- Badge compacto de status em tabela e cards.
- Exclusão individual e em massa com confirmação em duas etapas.
- Edição em massa com todos os campos do frontend, exceto `publicId`.
- “Adicionar produto” virou link para `/produtos/novo`.
- Exportação removida do catálogo; não há CSV nem endpoint de export.

### Produto
- Detalhe com informações completas, categorias, marketplaces, status e ações.
- Área de anúncios mockada, com criação/edição em massa, edição e duplicação
  de listing apenas como ações demonstrativas.
- Formulário de criação/edição com Signal Forms, validação inline, categorias
  ativas, preservação de categorias inativas quando a seleção não é alterada
  e `categoryIds: []` apenas em reset explícito.
- Duplicação pelo endpoint de registro, com prefixo `Cópia - ` no nome, SKU
  vazio para geração automática e validação de nome/SKU únicos contra o
  produto original.
- Status separado de `active`: `ACTIVE`, `PAUSED` e `INACTIVE` não removem o
  produto do catálogo.
- Edição sempre envia `publicId` e o SKU atual quando ele não deve mudar.

### Bulk e status
- `PATCH /api/products/bulk` usado pela edição em massa, com SKU atual por
  item para evitar regeneração.
- `DELETE /api/products/bulk/delete` usado pela exclusão em massa.
- `PATCH /api/products/status` usado para status individual; a estrutura já
  aceita lote.

### Serviços, modelos e componentes
- `ProductService` ganhou `getProduct`, `registerProduct`, `updateProduct`,
  `deleteProduct`, `updateProductsStatus`, `bulkUpdateProducts` e
  `bulkDeleteProducts`; o registro passou para `/api/products/register`.
- `ProductResponse` ganhou `status` e passou a aceitar `baseSellPrice` nulo.
- Novos tipos de request/response e `ApiProblem`.
- Novos componentes:
  - `ProductDetailComponent`
  - `ProductFormComponent`
  - `ProductListingsMockComponent`
  - `ProductBulkEditDialogComponent`
  - `ProductStatusBadgeComponent`
  - `ConfirmDialogComponent`
- Novo utilitário de formatação `shared/format/product-format.ts`.
- Novo CSS `products-components-actions.css` para menu contextual e links.

### Fora do escopo
- Vínculo/desvínculo real de marketplace.
- Listagem real de anúncios com endpoint/pageable.
- Criação/edição real de anúncios.
- CRUD de categorias.
- Exportação.

### Validação
- `npm run build` passou; apenas warnings de budget de CSS por componente.
- `npm test -- --watch=false`: 9 arquivos / 22 testes passando.
- Fluxo validado no navegador: criar, editar, duplicar, status, excluir,
  edição em massa, exclusão em massa, deep links e área mockada de anúncios.
- O servidor local em 4200 servia bundle antigo; a validação foi feita em uma
  instância paralela e o arquivo temporário de proxy de teste foi removido.

---

## 10. Ajustes no formulário de produto (11/11/2026)

- Formulário de criação/edição centralizado e ampliado. O deep link recebeu um
  layout “studio” de duas colunas, com largura máxima de 1400px: prévia à
  esquerda e cards numerados de formulário à direita.
- Coluna de prévia com imagem, nome, SKU, preço, estoque, status e categorias
  atualizados em tempo real; ações do formulário em barra sticky.
- Campo **Estoque mínimo** removido do formulário de criação/edição e da
  edição em massa.
- O estoque mínimo passa a ser responsabilidade do backend: usa o default no
  cadastro e é atualizado automaticamente conforme os pedidos. Os payloads de
  criação, edição e bulk não enviam mais `minStock`.
- O detalhe do produto continua exibindo o estoque mínimo como leitura.
- O campo de URL da imagem foi substituído por **envio de arquivo** com
  dropzone, preview local, remover/restaurar e validação de JPG/PNG/WEBP até
  5 MB.
- O upload está em **modo mock local**: o arquivo é usado apenas no preview e
  não é enviado ao backend. Ao salvar, a imagem atual é mantida (edição/
  duplicação) ou o produto fica sem imagem (criação). O fluxo já está
  estruturado para receber o endpoint real de upload.

---

## 11. Higiene de responsabilidades (12/11/2026)

- Regra de estoque removida do HTML do catálogo: criado
  `shared/product/product-view.ts` com `stockState()` (`out`, `low`, `normal`).
  A tabela e o card agora usam o helper.
- Comparações de status, listagens, categorias e imagem movidas para helpers e
  computeds nos componentes:
  - `hasCategories`, `hasListings`, `isCurrentStatus`, `noCategoryFilterSelected`;
  - `hasMarketplaces`, `hasCategories`, `isCurrentStatus`, `isSavingStatus`,
    `statusBusy`;
  - `hasSelectedCategories`, `canRemoveImage`, `canRestoreImage`,
    `showMockBadge`, `canResetCategories`;
  - `isPositiveZero(card)` para o estado zero positivo dos KPIs.
- Mutações do catálogo extraídas para `ProductActionsService`:
  status, exclusão individual, exclusão em massa, atualização em massa e
  montagem dos itens do bulk. O componente ficou responsável pelo estado de
  tela, toasts e atualização local.
- Dashboard legado limpo: removidos formulário morto, `ProductService`,
  `registerProduct()`, `alert()` e `console.error`.
- Login limpo: removidos `alert()` e `console.error`, adicionado `return` na
  validação vazia, bloqueio de duplo envio e `takeUntilDestroyed`.
- Testes: `ProductActionsService` ganhou spec da montagem do bulk.
- Validação: build passou e `npm test -- --watch=false` passou com
  **10 arquivos / 24 testes**.

---

## 12. Category type — PRODUCT x STATE (12/11/2026)

- `CategoryRef` ganhou `type: 'PRODUCT' | 'STATE'`; criados `CategoryType` e
  `CategoryCreateRequest`.
- `ProductService.getCategories(type?)` aceita `?type=PRODUCT|STATE` e
  `createCategory()` envia `name + type`.
- Catálogo:
  - filtro de categorias agrupado em “Categoria de produto” e “Categoria de
    estado”;
  - chips ativos com rótulo `Produto:`/`Estado:` e cor distinta para estado;
  - tabela e cards mostram badges de produto em azul e de estado em violeta;
  - resumo de categorias no drawer separa “Produto” e “Estado”.
- Detalhe do produto:
  - seções “Categorias de produto” e “Categorias de estado”;
  - contagem separada no cabeçalho.
- Formulário de criação/edição:
  - listas e contagens separadas por tipo;
  - chips de estado em violeta;
  - inativas separadas por tipo e exibidas apenas na edição;
  - preview com chips diferenciados;
  - payload continua usando `categoryIds` único, aceitando mistura.
- Bulk edit: lista de categorias agrupada por tipo.
- Categorias inativas continuam sendo preservadas quando o campo não é tocado;
  `categoryIds: []` somente em reset explícito.
- Testes: `ProductService` cobre `?type` e criação de categoria.
- Validação: build passou e `npm test -- --watch=false` passou com
  **10 arquivos / 26 testes**.





