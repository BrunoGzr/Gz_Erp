# Project.md — Guia de UI, UX e Engenharia do Frontend GzErp

> **Escopo deste guia:** exclusivamente o frontend Angular em `Frontend/Erp`.
> Backend, banco de dados, contratos internos de API, regras de negócio e
> infraestrutura **não** fazem parte deste documento e não devem ser alterados
> a partir dele.
>
> **Leia este arquivo antes de alterar qualquer tela, estilo, componente ou
> serviço do frontend.** Ele define o padrão visual, de interação e de código do
> GzErp. O histórico da padronização está em [`Change.md`](./Change.md); a visão
> de produto está em [`README.md`](./README.md).
>
> **Como usar este guia**
>
> 1. Localize a tela/componente mais próximo do que será feito (§2.2 e §5).
> 2. Reutilize tokens, classes e padrões existentes antes de criar algo novo.
> 3. Implemente todos os estados: repouso, hover, foco, ativo, desabilitado,
>    carregando, vazio e erro.
> 4. Valide acessibilidade, responsividade, build e testes (§6, §7 e §11).
>
> **Regra de ouro:** reutilizar > extrair > criar. Se um padrão novo for
> realmente necessário, documente-o aqui e justifique no `Change.md`.
>
> **Precedência documental:** para UI, `Project.md` > `Change.md` > `README.md`.
> Se o código legado divergir deste guia, o guia vale para código novo; não
> altere comportamento existente por inferência.

---

## 1. Escopo e princípios

O GzErp é um ERP multi-tenant de e-commerce com frontend Angular. A UI deve
transmitir **densidade controlada, previsibilidade e leitura rápida de dados**,
sem abandonar acessibilidade.

Princípios obrigatórios:

1. **Um único vocabulário visual.** Cores, raios, sombras, tipografia e camadas
   vêm dos tokens globais. Nenhum componente deve introduzir uma paleta própria.
2. **Consistência acima de novidade.** Um padrão existente deve ser reutilizado
   antes de se criar outro.
3. **Estado sempre visível.** Toda ação deve ter estados de repouso, hover,
   foco, ativo, desabilitado, carregando, vazio e erro.
4. **Acessibilidade por padrão.** Teclado, foco visível, landmarks, rótulos e
   contraste fazem parte da definição de pronto.
5. **Dark theme é o tema atual.** O produto opera em tema escuro; qualquer tema
   claro futuro deve ser construído sobre os mesmos tokens.
6. **Português do Brasil.** Textos, datas, moeda e mensagens seguem pt-BR.
7. **Evolução incremental.** Não reescreva uma tela inteira para aplicar um
   padrão. Extraia subcomponentes e migre o trecho tocado sem alterar
   comportamento.

> **Fora do escopo deste guia:** backend, banco, autenticação como política de
> negócio, permissões, integrações externas, contratos internos de API e dados
> mockados como fonte de verdade. O mapa de rotas aqui é **contexto de UI**;
> mudanças funcionais de rota exigem decisão de produto e testes próprios.

---

## 2. Stack, estrutura e organização

### 2.1 Stack e ambiente

- **Angular 22.2** com standalone components (padrão), signals e control flow
  `@if/@for/@switch`.
- **TypeScript 6** conforme `tsconfig`. O `tsconfig.json` atual **não habilita
  `strict: true`**; não introduza `any` e prefira tipagem explícita. Não altere
  as flags de compilação sem alinhamento e build completo.
- **CSS puro** com encapsulamento por componente. Não há Tailwind, Material ou
  biblioteca de componentes instalada.
- **Vitest 5** para testes unitários.
- **Prettier 3** com `printWidth: 100`, `singleQuote: true` e parser Angular para
  HTML (`.prettierrc`). Formate apenas os arquivos que você tocar. O CSS do
  catálogo está comprimido em algumas linhas; **não rode Prettier nele** —
  preserve o estilo local para não gerar diff gigante.
- Projeto em `Frontend/Erp`.

Comandos de trabalho, sempre a partir de `Frontend/Erp`:

```bash
npm install
npm start                  # ng serve; usa proxy.conf.json
npm run build              # validação obrigatória de UI
npm test -- --watch=false  # Vitest; veja baseline em §10.11
```

O `proxy.conf.json` é configuração de ambiente e não faz parte deste guia de UI.
Não altere o target do proxy como parte de uma mudança visual.

### 2.2 Mapa do frontend

```text
Frontend/Erp/
├── angular.json                 # builders, budgets de bundle e CSS
├── package.json                 # scripts, Angular 22, Vitest, Prettier
├── proxy.conf.json              # proxy de desenvolvimento
├── public/                      # ícones SVG legados usados por <img>
└── src/
    ├── styles.css               # tokens globais + base + foco + reduced motion
    ├── index.html               # lang, título, meta tags, fonte Inter
    └── app/
        ├── app.ts / app.html / app.css   # raiz (<div>), sem landmark
        ├── app.config.ts                 # router, HTTP e interceptor
        ├── app.routes.ts                 # rotas (contexto funcional)
        ├── core/
        │   ├── guards/auth-guard.ts      # protege o shell autenticado
        │   └── interceptors/auth-interceptor.ts
        ├── models/
        │   ├── loginModels.ts            # tipos usados pela UI de autenticação
        │   └── productModels.ts          # tipos usados pela UI de produto/categoria
        ├── service/
        │   ├── auth-login-service.ts     # AuthService
        │   └── product-service.ts        # ProductService
        ├── shared/
        │   ├── format/product-format.ts  # moeda, data, status, normalização
        │   └── ui/
        │       ├── confirm-dialog/       # confirmação destrutiva em 2 etapas
        │       └── product-status-badge/
        └── features/
            ├── auth-login-component/     # tela pública de login
            ├── header-component/         # shell, sidebar, topbar e ícones
            │   ├── header-component.*
            │   ├── topbar-component.*
            │   └── header-icon-component.ts
            ├── dashboard-component/      # legado — ver §10.8
            ├── product-detail-component/ # deep link /produtos/:publicId
            ├── product-form-component/   # /produtos/novo e /produtos/:publicId/editar
            └── products-component/       # catálogo (KPI, tabela, grid, drawer)
                ├── products-components.ts/html
                └── products-components-*.css
```

### 2.3 Rotas e navegação

| Rota | Componente | Estado | Observação |
|---|---|---|---|
| `/login` | `AuthLoginComponent` | Implementada | Tela pública; dona do `<main class="login-page">` |
| `/` | `HeaderComponent` | Implementada | Shell autenticado; redireciona para `/dashboard` |
| `/dashboard` | `DashboardComponent` | Stub/legado | Só header/nav; duplica navegação e tem links mortos (§10.8) |
| `/produtos` | `ProductsComponents` | Implementada | Catálogo; referência de tela de dados |
| `/produtos/novo` | `ProductFormComponent` | Implementada | Criação de produto; deep link |
| `/produtos/:publicId` | `ProductDetailComponent` | Implementada | Detalhe do produto; deep link |
| `/produtos/:publicId/editar` | `ProductFormComponent` | Implementada | Edição de produto; deep link |
| `/pedidos` | — | Não existe | Sidebar exibe; topbar marca “Em breve” |
| `/marketplaces` | — | Não existe | Sidebar exibe; topbar marca “Em breve” |
| `/equipe` | — | Não existe | Sidebar exibe; topbar marca “Em breve” |
| `**` | — | Não existe | Não há rota 404/wildcard |

Regras:

- Toda página autenticada é filha de `HeaderComponent` em `app.routes.ts`.
- O shell já possui o único `<main class="app-content">`. **Páginas filhas não
  devem criar outro `<main>`.**
- O login é a exceção: ele é dono do seu próprio `<main>`.
- Ao criar uma rota nova, atualize também `menuItems` em
  `header-component.ts` e `pages` em `topbar-component.ts`. A navegação está
  duplicada nessas duas listas; uma fonte única é desejável, mas não é requisito
  agora. Mantenha as duas sincronizadas até um refactor alinhado.
- Rotas filhas de Produtos (`/produtos/novo`, `/produtos/:publicId`,
  `/produtos/:publicId/editar`) mantêm o item “Produtos” ativo via
  `navLinkActiveOptions` com `paths: 'subset'`.
- Prefira `loadComponent` para features novas. Rotas atuais podem continuar
  eager até um refactor alinhado.
- O `authGuard` valida o token local; se inválido, tenta refresh e, ao falhar,
  redireciona para `/login?returnUrl=...`. O `authInterceptor` injeta o token e
  tenta refresh em `401`. Não duplique essa lógica em componentes.
- O login atual ignora `returnUrl`; ao corrigir, use o parâmetro enviado pelo
  guard (§10.3).
- Não invente rotas “Em breve”. Elas são intencionais até a feature existir.

### 2.4 Papéis e responsabilidades

| Camada | Responsabilidade | Onde fica |
|---|---|---|
| Shell | Sidebar, topbar, navegação e área de conteúdo | `features/header-component/` |
| Página | Uma tela de rota, estado e composição da feature | `features/<feature>/` |
| Apresentação | Subcomponente sem HTTP e com inputs/outputs claros | Dentro da feature ou em `shared/` se reutilizável |
| Serviço | HTTP e estado compartilhado do domínio; normalização de erro é desejável no serviço | `service/` |
| Modelos | Contratos TypeScript (`interface`/`type`) | `models/` |
| Core | Guard, interceptor e preocupações transversais | `core/` |
| Estilos globais | Tokens, base, foco e movimento reduzido | `src/styles.css` |

Ao criar uma pasta `shared/`, coloque nela apenas componentes/pipes/diretivas
realmente reutilizáveis. Não mova código específico de feature para `shared/`
sem uso comprovado.

### 2.5 Nomenclatura e organização

- Arquivos em **kebab-case**: `product-card.ts`, `products-components.css`.
- Classes em **PascalCase** com sufixo `Component` quando for componente.
  `ProductsComponents` (plural) é exceção legada; não renomeie sem necessidade.
- Seletores de componente com prefixo `app-`: `app-products-components`.
- Serviços no padrão `<dominio>-service.ts`, classe `<Dominio>Service`.
- Modelos no padrão `<dominio>Models.ts` (padrão existente). Tipos/interfaces em
  PascalCase; `registerProductRequest` e `receivedProduct` são exceções legadas.
- Testes no padrão `<arquivo>.spec.ts`, ao lado do arquivo testado.
- Uma feature por pasta. Um componente por arquivo.
- `shared/` só recebe código a partir do 2º uso real. Primitivas visuais devem
  virar componentes (`shared/ui/`) ou classes globais promovidas com
  justificativa, nunca cópias de CSS entre features.
- Componentes grandes podem dividir CSS por responsabilidade, como o catálogo
  faz (`table`, `grid`, `drawer`, `ui`, `select`, `kpi`).
- Página nova deve mirar ~250–300 linhas de TS. Se passar disso ou acumular três
  ou mais responsabilidades, extraia subcomponentes de apresentação. O catálogo
  é referência visual, não modelo de tamanho.
- Não crie arquivos “utils” genéricos sem um domínio claro. Formatação
  transversal pode viver em `shared/format/` ou usar `Intl` no componente.

### 2.6 Fluxo para criar ou alterar uma tela

Para uma página nova autenticada:

1. Crie a pasta em `features/<nome>/` com `<nome>-component.ts`, `.html`, `.css`
   e `.spec.ts`.
2. Declare o componente com `selector: 'app-<nome>'`, `templateUrl` e
   `styleUrl`; não use `standalone: true` (é o padrão).
3. Adicione a rota como filha do shell em `app.routes.ts` (prefira
   `loadComponent`) e atualize `menuItems`/`pages`.
4. Modele o estado com `signal` e derive com `computed`; mantenha HTTP no
   serviço do domínio.
5. Reutilize o padrão visual do catálogo (§5.11), mas **não copie o CSS inteiro**
   nem presuma que classes locais do catálogo existem fora dele (§5).
6. Implemente loading, vazio, erro, hover, foco, ativo e desabilitado.
7. Garanta teclado, `aria-*`, foco visível e `prefers-reduced-motion`.
8. Valide em 360, 768, 1024, 1366 e 1920px.
9. Rode `npm run build` e `npm test -- --watch=false` (§11).
10. Atualize `Change.md` com entrada datada se a mudança for estrutural ou criar
    padrão novo.

Para alterar uma tela existente: localize o padrão mais próximo (§5), limite o
diff ao escopo da tarefa e migre apenas as linhas tocadas.

---

## 3. Tokens de design

Todos os tokens vivem em `src/styles.css` no seletor `:root` e usam o prefixo
`--gz-`. **Não crie valores literais de cor, raio ou camada em componentes.**
Transparências contextuais (`rgba(255,255,255,.06)`, por exemplo) são aceitas
apenas para realces sobre superfícies; quando existir token equivalente, use o
token.

### 3.1 Superfícies

| Token | Valor | Uso |
|---|---|---|
| `--gz-bg` | `#12121e` | Fundo da aplicação, inputs, controles |
| `--gz-panel` | `#181920` | Sidebar, painéis, cards, drawer |
| `--gz-panel-elevated` | `#1a1a22` | Popovers e menus flutuantes |
| `--gz-surface` | `#1e1e24` | Botões secundários, toasts, camada intermediária |
| `--gz-surface-hover` | `#2a2a35` | Hover de superfícies interativas |
| `--gz-surface-hover-strong` | `#3a3a45` | Hover de bordas e realce forte |
| `--gz-border` | `#2a2a35` | Borda padrão |
| `--gz-border-strong` | `#3a3a45` | Borda em hover/foco |
| `--gz-line` | `rgba(42,42,53,.62)` | Divisórias internas de tabelas/listas |
| `--gz-media-bg` | `#0f0f16` | Fundo de imagem de produto |
| `--gz-row-hover` | `#1c1c24` | Linha/célula sticky em hover |
| `--gz-row-selected` | `#1c202a` | Linha/célula sticky selecionada |
| `--gz-scrollbar-thumb-hover` | `#50505f` | Thumb de scrollbar em hover |
| `--gz-avatar-start` | `#d8d8de` | Início do gradiente do avatar |
| `--gz-avatar-end` | `#9797a1` | Fim do gradiente do avatar |

### 3.2 Texto

| Token | Valor | Uso |
|---|---|---|
| `--gz-text` | `#e6e6ea` | Texto padrão |
| `--gz-text-strong` | `#f4f4f5` | Títulos e valores de destaque |
| `--gz-text-muted` | `#8f8f98` | Texto secundário |
| `--gz-text-muted-2` | `#a8b3c7` | Texto secundário mais claro (rótulos) |
| `--gz-text-faint` | `#6f6f7a` | Placeholder e ícones decorativos |
| `--gz-white` | `#ffffff` | Branco puro (não usar como texto padrão) |
| `--gz-on-strong` | `#181920` | Texto/ícone sobre fundo claro/branco |

### 3.3 Cores semânticas

| Token | Valor | Uso |
|---|---|---|
| `--gz-accent` | `#60a5fa` | Links, foco, seleção e realces (a ação primária branca usa `--gz-text-strong`) |
| `--gz-accent-soft` | `rgba(96,165,250,.6)` | Borda em foco |
| `--gz-accent-focus` | `rgba(96,165,250,.7)` | Anel de foco (`outline`) |
| `--gz-accent-bg` | `rgba(96,165,250,.1)` | Fundo do anel de foco |
| `--gz-accent-text` | `#93c5fd` | Texto sobre fundo azulado |
| `--gz-success` | `#4ade80` | Sucesso/estoque saudável |
| `--gz-success-text` | `#86efac` | Texto de sucesso |
| `--gz-warning` | `#fbbf24` | Atenção/estoque baixo |
| `--gz-warning-text` | `#fcd34d` | Texto de atenção |
| `--gz-danger` | `#f87171` | Erro/exclusão/sem estoque |
| `--gz-danger-text` | `#fca5a5` | Texto de erro |
| `--gz-violet` | `#a78bfa` | Pendências/avisos de marketplace |
| `--gz-violet-text` | `#c4b5fd` | Texto violeta |
| `--gz-severity-low` | `#94a3b8` | Severidade baixa (cinza-azulado) |
| `--gz-shopee` | `#ee4d2d` | Marca Shopee |
| `--gz-mercadolivre` | `#ffe600` | Marca Mercado Livre |

**Regras de cor:**

- Cores de marca (`--gz-shopee`, `--gz-mercadolivre`) só podem ser usadas para
  identificar o marketplace, nunca como cor de ação.
- Cores semânticas comunicam significado. Não use verde/vermelho apenas como
  decoração.
- Não introduza hex novo. Se o token não existir, proponha a adição em
  `styles.css` e documente no `Change.md`.

### 3.4 Tipografia

| Token | Valor | Uso |
|---|---|---|
| `--gz-font-sans` | `'Inter', 'Segoe UI', system-ui, -apple-system, sans-serif` | Toda a interface |
| `--gz-font-size-2xs` | `0.625rem` (10px) | Badge numérico e micro-rótulo não essencial |
| `--gz-font-size-xs` | `0.6875rem` (11px) | Metadado muito denso |
| `--gz-font-size-sm` | `0.75rem` (12px) | Texto auxiliar, labels, chips |
| `--gz-font-size-md` | `0.8125rem` (13px) | Controles e corpo denso |
| `--gz-font-size-base` | `0.875rem` (14px) | Corpo padrão em telas de dados |
| `--gz-font-size-lg` | `1rem` (16px) | Título de seção |
| `--gz-font-size-xl` | `1.25rem` (20px) | Título de drawer |
| `--gz-font-size-2xl` | `1.5rem` (24px) | Título de página |
| `--gz-line-height-tight` | `1.2` | Títulos |
| `--gz-line-height-normal` | `1.5` | Corpo |

**Piso de legibilidade:** não introduza texto abaixo de `0.6875rem` para
conteúdo de leitura. A única exceção é `--gz-font-size-2xs` (10px), permitida
para badge numérico e micro-rótulo não essencial. Nunca use `2xs` em corpo,
label de formulário ou mensagem de erro.

A fonte **Inter** é carregada via Google Fonts no `index.html` (pesos
400/500/600/700, `display=swap`). O fallback local é obrigatório e já está no
token. Se o projeto for servido em ambiente sem internet, considere self-host
da fonte — mudança de infraestrutura, não de UI.

### 3.5 Raios, sombras e movimento

| Token | Valor |
|---|---|
| `--gz-radius-sm` | `8px` |
| `--gz-radius-md` | `12px` |
| `--gz-radius-media` | `14px` |
| `--gz-radius-lg` | `16px` |
| `--gz-radius-xl` | `20px` |
| `--gz-radius-2xl` | `24px` |
| `--gz-radius-badge` | `6px` |
| `--gz-radius-pill` | `999px` |
| `--gz-shadow-sm` | `0 2px 8px rgba(0,0,0,.35)` |
| `--gz-shadow-md` | `0 8px 22px rgba(0,0,0,.22)` |
| `--gz-shadow-lg` | `0 18px 46px rgba(0,0,0,.5)` |
| `--gz-shadow-xl` | `0 32px 80px rgba(0,0,0,.55)` |
| `--gz-transition-fast` | `160ms ease` |
| `--gz-transition` | `200ms ease-in-out` |
| `--gz-transition-slow` | `300ms cubic-bezier(.22,1,.36,1)` |

Pílulas (`--gz-radius-pill`) são o padrão para **botões, inputs, chips e
filtros**. Cards usam `--gz-radius-xl`/`2xl`. Badges usam `--gz-radius-badge`;
o checkbox do catálogo é circular.

### 3.6 Layout e camadas

| Token | Valor | Uso |
|---|---|---|
| `--gz-sidebar-expanded` | `260px` | Sidebar fixada |
| `--gz-sidebar-collapsed` | `80px` | Sidebar recolhida/mobile |
| `--gz-topbar-height` | `48px` | Altura da topbar |
| `--gz-z-topbar` | `50` | Topbar |
| `--gz-z-popover` | `60` | Popovers da topbar |
| `--gz-z-sidebar` | `1000` | Sidebar |
| `--gz-z-overlay` | `1100` | Backdrop de drawer |
| `--gz-z-overlay-content` | `1101` | Drawer/dialog |
| `--gz-z-toast` | `1300` | Toasts |

**Nunca** use `z-index` arbitrário acima de 1300 sem atualizar este guia.
Menus internos de filtro usam `z-index: 40` e ficam abaixo da topbar; não os
promova a camadas globais.

### 3.7 Skeleton

| Token | Valor |
|---|---|
| `--gz-skeleton-base` | `#1f1f29` |
| `--gz-skeleton-highlight` | `#2b2b38` |

Skeletons usam shimmer horizontal de 1,3s e **só** aparecem no primeiro
carregamento; atualizações usam a barra de progresso do painel.

---

## 4. Base global (`styles.css`)

O arquivo global aplica:

- reset de `box-sizing`;
- `color-scheme: dark` no `html` e `font-family` no `body`;
- tipografia herdada por `button/input/select/textarea`;
- `::selection` azul;
- anel de foco único em `:where(a, button, input, select, textarea, [tabindex]):focus-visible`;
- redução global de movimento em `prefers-reduced-motion`.

**Não remova o `outline` sem fornecer um substituto visível.** Componentes podem
sobrescrever o anel, mas nunca suprimi-lo.

### Landmarks

- A raiz (`app.html`) é uma `<div class="app-root">` — **não** é `<main>`.
- A tela ativa é dona do único `<main>`:
  - login → `<main class="login-page">`;
  - shell autenticado → `<main class="app-content">`.
- Páginas filhas do shell **não** criam `<main>`; elas renderizam dentro do
  `<main class="app-content">`.
- Sidebar é `<aside>` + `<nav aria-label="Navegação principal">`.
- Nunca aninhe dois `<main>`.

---

## 5. Componentes e padrões

> **Importante — classes são locais até serem extraídas.** `.btn`, `.panel`,
> `.kpi`, `.toast`, `.filter-chip` e `.select-*` vivem hoje no CSS encapsulado do
> catálogo (`products-components-*.css`), não em `styles.css`. Uma nova tela
> **não** herda essas classes: recrie o padrão com os mesmos tokens/medidas ou
> extraia um componente para `shared/ui/` quando houver reuso real (a partir do
> 2º uso). Não copie o CSS inteiro do catálogo nem presuma que a classe
> funcionará fora dele.

### 5.1 Botões

| Classe | Uso | Aparência |
|---|---|---|
| `.btn` | Ação secundária | Fundo `--gz-surface`, borda, pill, 40px |
| `.btn-primary` | Ação principal | Fundo `--gz-text-strong`, texto `--gz-on-strong` |
| `.btn-danger` | Ação destrutiva | Borda/fundo translúcido de perigo, texto `--gz-danger-text` |
| `.icon-btn` | Ação só com ícone | Circular, 36–40px, `aria-label` obrigatório |
| `.round-btn` | Abrir/avançar em card | Circular branca, ícone escuro |
| `.quick-btn` | Ajustes rápidos no drawer | 38px, pill, borda |
| `.link-btn` | Ação textual discreta | Azul, sem fundo |
| `.filter-chip` | Filtro ativo removível | 36px, pill, borda |
| `.segmented button` | Alternância de modo | 36px, pill, ativo branco |
| `.pagination button` | Paginação | 34px, pill, ativo branco |
| `.bulk-actions button` | Ações em massa | 34px, compacto |

Regras:

- Altura padrão de 40px; controles principais nunca abaixo de 32px em desktop.
  Controles compactos (drawer, bulk, paginação) usam 34–38px; ícones auxiliares
  (pin, limpar busca, fechar toast, bulk-close) usam 28–30px.
- A topbar é uma exceção deliberada: seus controles têm 34px para caber nos
  48px de altura. Ações de tabela usam 36px.
- Em telas de toque, garanta alvo efetivo de 44px via padding ou
  pseudo-elemento; 34–38px é compacto de desktop, não um alvo mobile.
- Botão só com ícone **exige** `aria-label` e `title` quando o ícone não for
  óbvio.
- Desabilitado: opacidade e `cursor: not-allowed`; carregando: `cursor: progress`
  e spinner quando o botão for o gatilho.
- Não use `alert()` para feedback. Use toast (§5.8) ou mensagem inline.
- Ação destrutiva usa confirmação em duas etapas e `.btn-danger`.
- Não coloque botões interativos dentro de outro botão. KPIs clicáveis são
  `<button>` com conteúdo textual/estático, não com ações aninhadas.

### 5.2 Inputs, busca e selects

- Altura 40–46px, fundo `--gz-bg`, borda `--gz-border`, pill. Inputs do drawer
  usam 42px; login usa 46px.
- Foco: `border-color: --gz-accent-soft` + anel `0 0 0 3px --gz-accent-bg`.
- Placeholder `--gz-text-faint`, nunca como único rótulo.
- **Busca de dados (catálogo):** 40px, ícone à esquerda, botão limpar (28px),
  spinner durante request e debounce de 350ms. `Enter` aplica imediatamente;
  `Esc` limpa quando há texto.
- **Busca global da topbar:** 34px, atalho `Ctrl K`, sem botão limpar/spinner e
  com resultados de páginas. Não confunda os dois padrões.
- Selects customizados usam `.select-wrap` + `.select-trigger` + `.select-menu`,
  com `aria-expanded`, `aria-haspopup="listbox"` e `role="option"`.
- Em mobile (≤600px), filtros diretos do toolbar ocupam 2 colunas e a busca 100%.
- O menu de filtros usa `z-index: 40`; não o coloque acima da topbar.

### 5.3 Cards, painéis e KPIs

- `.panel` = superfície principal com borda e raio 24px.
- `.kpi` = card clicável de resumo, com faixa de tom no topo e ícone de ação.
- KPI com valor zero positivo mostra ícone de check e mensagem verde.
- Todo KPI clicável deve ter `aria-label` descritivo com valor e ação.
- O KPI é um `<button>`; não aninhe links/botões dentro dele.
- Use `font-variant-numeric: tabular-nums` em valores numéricos.

### 5.4 Tabela

- Cabeçalho sticky; primeira coluna (checkbox/produto) e coluna de ações sticky
  **acima de 900px**. Em ≤900px, as colunas deixam de ser sticky e a tabela
  rola horizontalmente.
- Colunas numéricas com `font-variant-numeric: tabular-nums` e alinhamento à
  direita.
- Ordenação: botão no `<th>`, `aria-sort` e indicador que só aparece na coluna
  ativa/hover. O ciclo é ascendente → descendente → sem ordenação.
- Linhas selecionadas e hover devem ser distinguíveis. **Dívida atual:** no
  corpo da tabela os dois usam o mesmo tom translúcido; refine quando tocar no
  componente. Células sticky usam `--gz-row-hover`/`--gz-row-selected`.
- Em telas estreitas, a tabela rola horizontalmente; colunas podem ser ocultadas,
  mas nunca remova a coluna de produto nem a de ações.
- Colunas ocultas hoje: categorias e atualização em ≤1250px; vendas em ≤1050px.

### 5.5 Grid de cards

- `grid-template-columns: repeat(auto-fill, minmax(210px, 1fr))`.
- Card: mídia 110px, checkbox no canto, categoria, título, marketplaces, preço e ação.
- Card selecionado recebe borda azul + anel. A linha da tabela hoje só muda o
  fundo (§10.7).
- Imagem de produto usa `loading="lazy"` e fallback visual quando falhar.

### 5.6 Drawer / dialog

- Overlay `--gz-z-overlay`, conteúdo `--gz-z-overlay-content`.
- Estrutura: `header` + `body` rolável + `footer` de ações.
- Largura `min(720px, calc(100vw - 32px))`; drawer de revisão usa
  `min(560px, calc(100vw - 32px))`.
- `max-height: min(90vh, 820px)`; em ≤600px, `calc(100vh - 24px)`.
- Fechar por botão, backdrop e `Esc`.
- Ações destrutivas usam confirmação em duas etapas e `.btn-danger`.
- O foco inicial vai para o botão de fechar; o foco deve retornar ao gatilho.
- Prenda o foco no drawer enquanto aberto e bloqueie o scroll do fundo.
- **Dívida atual:** o drawer de revisão fecha com `Esc` via listener global, mas
  ainda não tem focus trap nem restauração de foco; o drawer de edição rápida
  implementa ambos.

### 5.7 Empty, loading e erro

- **Primeiro carregamento:** skeleton correspondente ao modo (tabela/grid).
- **Atualização:** conteúdo esmaecido + barra animada no topo do painel.
- **Erro de carregamento:** ícone de atenção, título, mensagem e “Tentar novamente”.
- **Vazio sem cadastro:** ícone, título e explicação; CTA de cadastro quando existir.
- **Vazio por filtro:** mensagem específica + “Limpar filtros”.
- Use `aria-busy` no painel e `aria-live="polite"` na contagem/estado.
- Não use skeleton em atualização; skeleton é só para o primeiro load.

### 5.8 Toasts

- Empilhados no canto inferior direito, máximo 3 visíveis.
- Sucesso/erro/info com ícone e cor semântica.
- Erro usa `role="alert"`; os demais `role="status"`.
- Auto-dismiss em 3,5s; fechamento manual sempre disponível.
- Não use toast para validação de campo; use erro inline.
- Não use `alert()` nem `console.error` como feedback de usuário.

### 5.9 Sidebar e topbar

- Sidebar: perfil no topo, navegação no meio, logout na base.
- Recolhida = 80px; expandida = 260px; transição `--gz-transition-slow`.
- Expansão por hover com delay de 300ms; pin fixa o estado; botão central só
  aparece quando fixada.
- Topbar: busca global à esquerda, ajuda/notificações à direita.
- Popovers usam `--gz-panel-elevated`, raio 14px e `--gz-shadow-lg`.
- Todo item de navegação tem `aria-label` e `aria-current` quando ativo.
- A busca global (`Ctrl K`) só navega para páginas disponíveis; itens futuros
  mostram “Em breve” e ficam desabilitados.
- Notificações e ajuda são mockadas; não as apresente como dados reais.

### 5.10 Login

**Padrão implementado:**

- Tela centralizada, fundo com gradiente azul sutil sobre `--gz-bg`.
- Card de 420px, raio 24px, borda e `--gz-shadow-xl`.
- Um `<h1>`, labels associados por `for/id`, `autocomplete` correto.
- Erro inline com `role="alert"` e contraste AA.
- Botão desabilitado durante `isLoading` com spinner.
- Em ≤480px, padding reduzido e card ocupa a largura disponível.

**Dívida atual (ver §10.3):** login não respeita `returnUrl`, usa
`alert()`/`console.error`, duplica validação e não interrompe o submit quando os
campos estão vazios (falta `return`). Não replique esse padrão em telas novas.

### 5.11 Página de dados (padrão do catálogo)

O catálogo é a referência para novas telas de listagem/gestão:

1. **Cabeçalho da página:** eyebrow, `<h1>`, descrição e ações
   (`Exportar`, `Adicionar`, menu de contexto).
2. **KPIs:** cards clicáveis com resumo, estado zero positivo e `aria-label`.
3. **Painel:** título, contagem, alternância tabela/grid e toolbar de filtros.
4. **Filtros ativos:** chips removíveis + “Limpar filtros”; chips de ordenação.
5. **Barra de seleção:** contagem, ações em massa e limpar seleção.
6. **Conteúdo:** tabela ou grid com skeleton/empty/error.
7. **Rodapé:** intervalo exibido, paginação e itens por página
   (20/50/100/200).
8. **Estado na URL:** busca, categorias, marketplace, anúncios, estoque, página,
   tamanho e ordenação devem sobreviver a refresh. O catálogo usa `replaceUrl` e
   lê `route.snapshot` na entrada; se precisar de back/forward entre estados de
   filtro, re-sincronize com `queryParamMap` reativo.
9. **Atualização:** não recarregue a tela inteira; preserve filtros. No catálogo
   atual, a seleção é limpa ao mudar filtro, página ou ordenação; preserve-a
   apenas se implementar essa intenção explicitamente.

Contrato de URL do catálogo:

| Parâmetro | Valores | Default |
|---|---|---|
| `searchTerm` | texto | vazio |
| `categoryIds` | UUID repetido (`?categoryIds=a&categoryIds=b`) | vazio |
| `marketplace` | `SHOPEE`, `MERCADOLIVRE` | todos |
| `hasListings` | `true`, `false` | todos |
| `stock` | `LOW`, `OUT`, `NORMAL` | todos |
| `page` | inteiro 0-based | `0` |
| `size` | `20`, `50`, `100`, `200` | `20` (omitido) |
| `sort` | `campo,asc` ou `campo,desc` | sem ordenação |

Campos de ordenação: `name`, `baseSellPrice`, `stock`, `sales`, `updatedAt`.

**Categorias:** cada `CategoryRef` tem `type: PRODUCT | STATE`. O catálogo
agrupa e diferencia os dois tipos (produto em azul, estado em violeta); o
formulário separa os grupos e o payload continua em `categoryIds` único,
aceitando mistura. `GET /api/products/categories?type=PRODUCT|STATE` está
disponível.

### 5.12 Formulários

- Todo campo tem `<label for>`; placeholder não substitui label.
- `autocomplete` correto em login e dados pessoais.
- Validação inline junto ao campo; erro de servidor no topo do formulário com
  `role="alert"`.
- Botão de submit desabilitado durante envio e com spinner.
- Não valide a mesma regra em dois lugares. Centralize no formulário/serviço.
- Forms template-driven (`ngModel`) existem no legado. Para formulários novos,
  use **Signal Forms** (`@angular/forms/signals`); Reactive Forms é o fallback.
- Estoque mínimo não é editável no formulário de produto; o backend mantém o
  default e atualiza o valor automaticamente conforme os pedidos.
- Imagem de produto no deep link usa upload por arquivo (dropzone + preview),
  não campo de URL. Enquanto o backend não expõe o endpoint, o upload fica em
  modo mock local e não persiste a imagem.
- Não use `alert()` para sucesso/erro.
- Após sucesso, limpe o formulário ou navegue; não deixe estado ambíguo.

### 5.13 Feedback e confirmação

- **Toast:** confirmação rápida e não bloqueante.
- **Inline:** erro de campo ou de formulário.
- **Drawer feedback:** resultado de ação em massa dentro do drawer.
- **Confirmação em duas etapas:** ação destrutiva/irreversível.
- **Status:** use `role="status"` para informação; `role="alert"` para erro.
- Nunca use `console.log`/`console.error` como feedback de produto.

### 5.14 Tipos de tela e referência

| Tipo | Referência | Estado principal | Cuidados |
|---|---|---|---|
| Lista/dados | Catálogo (`products-component`) | Filtros, URL, loading/empty/error, seleção | Não clonar o CSS; extrair padrões |
| Detalhe | Drawer de edição rápida (referência); revisão pendente | Foco preso, `Esc`, retorno ao gatilho | Revisão ainda sem focus trap/retorno (§10.9) |
| Detalhe com deep link | `ProductDetailComponent` (`/produtos/:publicId`) | Loading, 404, status, ações | Listings ainda mockados |
| Formulário | Login (visual) + §5.12 | Validação inline, submit bloqueado | Signal/Reactive Forms em código novo |
| Configurações | Painel + formulário | Salvar/cancelar, feedback | Confirmação ao sair com alterações |

---

## 6. Acessibilidade

Checklist mínimo:

- [ ] Um `<main>` por tela; sem landmarks aninhados.
- [ ] Todo controle interativo é alcançável por `Tab` e ativável por `Enter`/`Espaço`.
- [ ] `:focus-visible` sempre visível; nunca `outline: none` sem substituto.
- [ ] Botões só com ícone têm `aria-label`; ícones decorativos têm `aria-hidden`.
- [ ] Campos têm `<label for>`; erros usam `role="alert"`/`aria-live`.
- [ ] Estados usam `aria-expanded`, `aria-selected`, `aria-pressed`, `aria-current` e `aria-sort`.
- [ ] Contraste mínimo AA (4.5:1 para texto normal; 3:1 para texto grande).
- [ ] Alvos de toque ≥44px quando possível; nunca <28px em controles principais.
- [ ] `prefers-reduced-motion` respeitado (global cobre animações CSS).
- [ ] Drawers/popovers gerenciam foco, `Esc` e restauração ao gatilho.
- [ ] Tabelas têm `scope="col"` nos cabeçalhos. `aria-selected` em `<tr>` só
      quando a tabela tiver semântica de grid; em tabela comum, use checkbox +
      estado visual.
- [ ] Imagens têm `alt`; imagens decorativas usam `alt=""`.
- [ ] Não use `aria-hidden="true"` em elemento que ainda pode receber foco.

**Dívida de contraste:** `--gz-text-faint` como placeholder fica em torno de
3,7:1 sobre `--gz-bg`, abaixo de AA para texto normal. Não use placeholder como
label; ao tocar no trecho, avalie `--gz-text-muted` ou um fundo mais escuro.

Pendências funcionais que exigem implementação (não apenas CSS): navegação por
setas nos menus `listbox`, foco preso e restauração no drawer de revisão, e
restauração de foco dos popovers da topbar. O drawer de edição rápida já prende
o foco e restaura ao gatilho.

---

## 7. Responsividade

Breakpoints em uso:

| Breakpoint | Efeito |
|---|---|
| `≥1400px` | Ajuste de chips de severidade nos KPIs (`min-width`) |
| `1250px` | Oculta colunas de categorias e atualização; tabela `min-width: 980px` |
| `1050px` | Oculta coluna de vendas |
| `900px` | Topbar compacta; catálogo reduz padding; tabela `min-width: 800px` e sticky desativado |
| `768px` | Sidebar fixa em 80px, labels ocultos, pin/toggle ocultos |
| `720px` | Drawer em uma coluna |
| `600px` | Catálogo em padding mínimo; filtros em 2 colunas; busca 100%; page-size sem `margin-left` |
| `520px` | Popovers da topbar ocupam a largura da tela |
| `480px` | Login compacto |

Regras:

- Use `100dvh` com fallback `100vh` (já aplicado em shell, sidebar, login e raiz).
- Evite gutters laterais acima de 64px; o catálogo usa `clamp(20px, 3.5vw, 64px)`.
- Teste sempre em 360, 768, 1024, 1366 e 1920px.
- Sidebar off-canvas no mobile e troca automática tabela→cards ainda são
  **pendências funcionais** (exigem estado/roteamento, não só CSS).
- Não invente breakpoints sem documentar nesta tabela.

---

## 8. Ícones

- Os controles próprios da topbar usam SVGs inline com `stroke: currentColor`.
- Resultados da busca da topbar reutilizam `HeaderIconComponent`, que recebe
  `name` (obrigatório) e `size` (default 24).
- A sidebar usa `HeaderIconComponent`; dois ícones vêm de `<img>` em `public/`
  (`Icone-home.svg`, `marketplace-icon.svg`) e dependem de `filter: brightness(0)`
  no estado ativo. O dashboard legado também usa `<img>` em links próprios.
- O catálogo usa um sprite SVG inline (`<symbol>` + `<use>`) no topo do template.
- **Padrão desejado:** SVG inline ou sprite único, sempre `currentColor`, com
  `aria-hidden="true"` e `focusable="false"`.
- Não adicione novos `<img>` de ícone. A unificação do conjunto é uma melhoria
  pendente (escopo funcional/visual amplo).

---

## 9. Convenções de CSS, TypeScript e código

### 9.1 CSS

- Componentes usam `styleUrl`/`styleUrls` com CSS encapsulado.
- Tokens globais só em `src/styles.css`; cores locais apenas como alias
  (`--bg: var(--gz-bg)`), como no catálogo e na topbar.
- Nomes de classe em kebab-case, semânticos (`.product-card`, `.kpi-head`).
- Ordem recomendada: layout → dimensões → tipografia → cores → estados → mídia.
- Não use `!important` exceto em regras de movimento reduzido e ocultação mobile
  já existentes.
- Não crie CSS global para um componente; se precisar, justifique e documente.
- Prefira `rem` para tipografia e `px` para bordas/ícones.
- `aria-*` e atributos de acessibilidade acompanham o markup do componente.
- Scrollbars customizadas só onde já existem (catálogo, tabela, drawer, selects).
- Todo novo CSS deve respeitar `prefers-reduced-motion`.
- Camadas locais: `z-index` 0–3 para células e cabeçalhos sticky; 10 para o
  toggle da sidebar; menus internos 40; camadas globais usam `--gz-z-*` (≥1000).
  Qualquer outro valor exige justificativa neste guia.

### 9.2 Angular e TypeScript

- Standalone é o padrão no Angular 22; **não** declare `standalone: true` em
  componentes novos.
- Não declare `changeDetection: ChangeDetectionStrategy.OnPush` explicitamente;
  `OnPush` é o padrão no Angular 22.
- O app é **zoneless** (padrão no Angular 21+) e `OnPush` é o default. Todo
  estado lido pelo template deve ser signal, `computed` ou `async` pipe; não
  atualize campos simples em callbacks assíncronos esperando re-render.
- Para HTTP, prefira `resource()`/`httpResource()` ou `toSignal`. `subscribe`
  manual fica para efeito colateral e usa `takeUntilDestroyed`.
- Use `signal()` para estado local e `computed()` para estado derivado.
- Use `set`/`update`; nunca `mutate` em signals.
- Prefira `inject()` a injeção por construtor.
- Use `@Service()` para serviços singleton novos (padrão Angular 22 e já usado
  em `AuthService`/`ProductService`).
- Para componentes novos, prefira `input()`, `output()` e `model()` a
  `@Input`/`@Output`. Código legado pode manter decorators até ser tocado.
- Use control flow nativo (`@if`, `@for`, `@switch`) e **sempre** `track` no
  `@for`. Não use `*ngIf`/`*ngFor`/`*ngSwitch`.
- Não use `ngClass`/`ngStyle`; use `[class]`/`[style]` bindings.
- Não importe `CommonModule`; importe apenas diretivas/pipes usados.
- Em componentes novos, prefira o objeto `host` a `@HostBinding`/`@HostListener`.
  O legado usa `@HostListener`; migre apenas ao tocar no componente.
- Observables devem ser consumidos com `async` pipe ou convertidos com
  `toSignal`; assinaturas manuais usam `takeUntilDestroyed`.
- Não use `any` novo. Use `unknown` + narrowing quando o tipo for incerto.
- Evite lógica complexa no template; mova para `computed` ou métodos do
  componente.
- Acesso direto ao DOM é permitido apenas para foco, scroll e medições
  controladas; limpe timers e listeners no destroy.
- Não registre diretivas/forms como `providers` em `app.config.ts`; use
  `imports` no componente.

### 9.3 Estado e dados

- Estado de tela vive no componente com signals; estado compartilhado vive em
  serviços.
- Componentes **não** chamam `HttpClient` diretamente; use o serviço do domínio.
- Um serviço por domínio, com métodos tipados. O padrão desejado é normalizar
  erro no serviço; o catálogo atual normaliza no componente (`apiErrorMessage`),
  o que é legado.
- Modelos são `interface`/`type`; não use classes de modelo sem necessidade.
- Filtros, ordenação, paginação e busca de telas de dados devem refletir na URL.
- Estado na URL: o catálogo lê `route.snapshot` na entrada e grava com
  `replaceUrl`; isso torna a URL compartilhável, mas não mantém histórico de
  filtros. Se mudar para `push`, re-sincronize com `queryParamMap` para evitar
  estado inconsistente no back/forward.
- Não coloque regra de negócio no template.
- Dados mockados (`SIMULATED_SUMMARY`, notificações, ajuda) são apenas
  apresentação; não os trate como contrato real.
- Não invente endpoints nem campos. Reutilize os serviços existentes e alinhe
  qualquer contrato novo antes de consumir.
- Formate moeda, número, data e percentual com `Intl` em pt-BR; não concatene
  símbolos manualmente. Formatação transversal pode viver em `shared/format/`.
- Estados vazios: texto ausente usa `—`; valor numérico/data inválido usa `--`;
  intervalo usa `–`. Não misture no mesmo componente sem motivo.

### 9.4 Formulários e validação

- **Default para formulários novos:** Signal Forms (`@angular/forms/signals`);
  Reactive Forms é o fallback; template-driven (`ngModel`) é apenas legado.
- Validação deve ter uma única fonte de verdade.
- Erros de campo ficam junto ao campo; erros de submit ficam no topo.
- O botão de submit não deve permitir duplo envio.
- Após sucesso, limpe ou navegue de forma explícita.

### 9.5 Testes

- Use Vitest e `TestBed`; specs ficam ao lado do arquivo.
- Teste comportamento observável (estado, texto, classes/atributos relevantes),
  não detalhes de implementação.
- Componentes que injetam router/HTTP precisam dos providers de teste
  (`provideRouter`, `provideHttpClient`, `provideHttpClientTesting`). Vários
  specs legados não os declaram e passam no ambiente atual; adicione os
  providers ao tocar no spec para não depender desse acoplamento.
- Use fake timers para debounce, hover delay e auto-dismiss.
- Ao mudar comportamento, atualize o spec correspondente.
- **Baseline atual:** `npm test -- --watch=false` tem 2 falhas pré-existentes
  (ver §10.11). Não introduza falhas novas.

### 9.6 Performance e budgets

- Mantenha `@for` com `track` para evitar recriação desnecessária.
- Prefira rotas lazy para features novas.
- Evite cálculos pesados no template; use `computed`.
- Imagens de produto usam `loading="lazy"`; imagens estáticas novas podem usar
  `NgOptimizedImage` quando fizer sentido.
- O budget de CSS por componente é 4kB (warning) e 8kB (erro). O budget inicial
  do bundle é 500kB (warning) e 1MB (erro), conforme `angular.json`. Não aumente
  budget para “resolver” aviso; divida o componente.

---

## 10. Estado atual e pendências (não “corrigir” sem alinhamento)

Estes pontos são **funcionais** e foram deliberadamente preservados:

1. Rotas `/pedidos`, `/marketplaces` e `/equipe` ainda não existem; a sidebar as
   exibe e a topbar as marca como “Em breve”. Não sincronize isso apenas com CSS.
2. Não há rota 404/wildcard.
3. Login ainda não respeita `returnUrl`. A validação com `alert()`/
   `console.error` e a falta de `return` no submit vazio foram corrigidas; a
   requisição usa `takeUntilDestroyed` e bloqueia duplo envio.
4. Sessão/refresh/logout têm inconsistências no frontend: o refresh token não é
   persistido (`storeTokens` grava só `access_token`), o logout lê uma chave que
   nunca é gravada e o refresh depende de cookie. Não altere sem alinhamento.
5. KPIs, notificações e ajuda são mockados; não os apresente como dados reais e
   não invente endpoints/campos a partir deles.
6. As ações de produto (abrir, editar, duplicar, status, excluir e bulk) foram
   implementadas. As mutações do catálogo foram extraídas para
   `ProductActionsService` e o estado visual de estoque para
   `shared/product/product-view.ts`. Continuam mockadas: vinculação de
   marketplace, edição de preço/estoque dos anúncios e a listagem real de
   anúncios no detalhe. O feedback do drawer de anúncios ainda usa
   `role="status"` mesmo no tom de erro; corrija ao implementar o contrato de
   listings.
7. A tabela usa o mesmo tom para hover e seleção no corpo; a distinção existe
   apenas nas células sticky. Refine ao tocar no componente.
8. Dashboard é stub legado: o template só tem header/nav, os links apontam para
   `/estoque`, `/Marketplaces` e `/equipe` (inexistentes ou com caixa
   divergente) e o CSS está vazio. O formulário morto e a chamada
   `registerProduct()` foram removidos. Requer decisão de produto.
9. Sidebar mobile off-canvas, tabela→cards e navegação por teclado em listbox
   continuam pendentes. O drawer de revisão não tem focus trap nem restauração
   de foco (o `Esc` é coberto por listener global).
10. Os estilos de componente ultrapassam o budget de warning de 4kB (máximo
    observado ~7,92kB, abaixo do erro de 8kB). A solução é dividir componentes,
    não aumentar o budget. Avisos atuais: `products-components-ui.css` (7,58kB),
    `header-component.css` (7,92kB), `products-components.css` (7,66kB),
    `products-components-grid.css` (7,91kB), `topbar-component.css` (7,17kB) e
    `products-components-drawer.css` (~5,1kB).
11. Baseline atual de testes: `npm test -- --watch=false` passa com 10 arquivos /
    26 testes. Os specs de `HeaderComponent` usam fake timers para o delay de
    300ms, o spec de `ProductsComponents` fornece `ActivatedRoute`, providers
    de router e HTTP, `ProductActionsService` cobre a montagem do bulk e
    `ProductService` cobre `?type` e criação de categoria.
12. `app.config.ts` registra `FormsModule`/`RouterOutlet` como providers; isso é
    legado e não deve ser replicado. O correto é importar no componente.
13. A tipografia legada ainda tem valores abaixo do piso (ex.: `0.6rem`,
    `0.62rem`, `0.64rem`) na topbar e no catálogo. Migre para
    `--gz-font-size-2xs`/`xs` ao tocar no trecho, sem refatoração em massa.
14. A tabela aplica `aria-selected` em `<tr>` de uma `<table>` comum, sem
    `role="grid"`. Remova o atributo ou adote semântica de grid ao tocar.
15. `AuthService` ainda usa `any` em `jwtDecode`; migre ao tocar nesse serviço.
    `ProductService` já está tipado com `ProductResponse` e requests específicos.
16. Alguns `subscribe`/`setTimeout` legados não têm cleanup (`auth-login`,
    `dashboard`, `topbar`); ao tocar, use `takeUntilDestroyed`/`clearTimeout`.
17. Alguns literais equivalentes a tokens permanecem (ex.: topbar `height: 48px`,
    `rgba(96,165,250,...)` onde existem `--gz-accent-soft`/`--gz-accent-bg`);
    migre ao tocar, sem varredura global.
18. Alvos de toque legados ainda são menores que 44px no mobile (ex.:
    `.check-hit` 32px e `.topbar-btn` 34px). Garanta hit area via padding ou
    pseudo-elemento ao tocar nesses controles.

---

## 11. Definição de pronto (UI)

**Obrigatório no código novo e nas linhas tocadas:**

- [ ] Usa apenas tokens globais (sem hex novo).
- [ ] Tipografia, raio e altura seguem as tabelas deste guia.
- [ ] Estados hover/foco/ativo/disabled/loading/empty/error existem.
- [ ] Funciona em 360, 768, 1024, 1366 e 1920px.
- [ ] Teclado alcança e opera todos os controles; foco visível.
- [ ] `aria-*` corretos; nenhum botão de ícone sem rótulo.
- [ ] `prefers-reduced-motion` respeitado.
- [ ] Textos em pt-BR, sem placeholders de scaffold (“works!”, “teste”).
- [ ] `npm run build` passa sem erros.
- [ ] `npm test -- --watch=false` não introduz falhas novas (baseline em §10.11).
- [ ] Nenhuma alteração de backend, contrato interno ou regra de negócio.
- [ ] Diff limitado aos arquivos da tarefa: sem `dist/`, reformatação global,
      churn em `package-lock` ou migração em massa de legado.
- [ ] `Change.md` recebeu entrada datada quando a mudança foi estrutural ou criou
      padrão novo.

**Não exigido nesta entrega:** normalizar telas legadas inteiras, dividir o
catálogo ou migrar todas as dívidas de §10. Isso exige tarefa própria e
alinhamento de produto.

---

## 12. Anti-padrões

Não faça:

- hex novo ou `rgba()` sem justificativa contextual quando existir token;
- `z-index` fora da allowlist (0–3 local, 10 toggle, 40 menus, `--gz-z-*` global);
- `<main>` em página filha do shell;
- `alert()`/`console.error` como feedback de usuário;
- botão só com ícone sem `aria-label`;
- `*ngIf`/`*ngFor`, `ngClass`/`ngStyle` ou `CommonModule` em código novo;
- `@for` sem `track`;
- `HttpClient` direto no componente;
- `mutate` em signal;
- cópia de CSS ou de classes locais do catálogo presumindo que são globais;
- CSS global para um componente sem justificativa;
- `outline: none` sem substituto visível;
- novo `<img>` de ícone;
- aumento de budget de CSS em vez de dividir componente;
- endpoint/campo inventado ou mock apresentado como dado real;
- refatoração em massa de código legado junto com uma mudança pontual.
