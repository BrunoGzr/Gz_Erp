import { DOCUMENT } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, catchError, defer, finalize, of, switchMap, tap } from 'rxjs';
import {
  CategoryRef,
  CategoryType,
  Marketplace,
  ProductBulkEditPayload,
  ProductPageResponse,
  ProductResponse,
  ProductStatus,
} from '../../models/productModels';
import { ProductActionsService } from '../../service/product-actions-service';
import { ProductService } from '../../service/product-service';
import { categoryTypeLabel, statusLabel } from '../../shared/format/product-format';
import { stockState } from '../../shared/product/product-view';
import { ConfirmDialogComponent } from '../../shared/ui/confirm-dialog/confirm-dialog';
import { ProductStatusBadgeComponent } from '../../shared/ui/product-status-badge/product-status-badge';
import { ProductBulkEditDialogComponent } from './product-bulk-edit-dialog';

type ViewMode = 'table' | 'grid';
type MarketplaceFilter = 'all' | Marketplace;
type AdsFilter = 'all' | 'with' | 'without';
type StockFilter = 'all' | 'low' | 'out' | 'normal';
type FilterKey = 'search' | 'category' | 'marketplace' | 'ads' | 'stock';
type PriceMode = 'percent' | 'fixed';
type StockMode = 'set' | 'adjust';
type DeactivateStep = 0 | 1 | 2 | 3;
type SortKey = 'name' | 'price' | 'stock' | 'sales' | 'updated';
type SortDir = 'asc' | 'desc';
type ToastTone = 'success' | 'info' | 'error';
type FilterMenu = 'category' | 'marketplace' | 'ads' | 'stock' | 'pageSize';

interface ProductToast {
  id: number;
  message: string;
  tone: ToastTone;
}

interface ActiveFilter {
  key: string;
  label: string;
  filter: FilterKey;
  categoryId?: string;
  categoryType?: CategoryType;
}

interface RowMenuPosition {
  top: number;
  left: number;
}

interface PageNumber {
  label: string;
  value: number | null;
}

interface MarketplaceEntry {
  key: Marketplace;
  label: string;
  cssClass: string;
  ads: number;
}

const MARKETPLACES: Record<Marketplace, { label: string; cssClass: string }> = {
  SHOPEE: { label: 'Shopee', cssClass: 'mp-shopee' },
  MERCADOLIVRE: { label: 'Mercado Livre', cssClass: 'mp-mercado-livre' },
};

const SORT_LABELS: Record<SortKey, string> = {
  name: 'Produto',
  price: 'Preço',
  stock: 'Estoque',
  sales: 'Vendas',
  updated: 'Atualização',
};

const SORT_FIELDS: Record<SortKey, string> = {
  name: 'name',
  price: 'baseSellPrice',
  stock: 'stock',
  sales: 'sales',
  updated: 'updatedAt',
};

const PAGE_SIZES = [20, 50, 100, 200];

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

const TOAST_ICONS: Record<ToastTone, string> = {
  success: '#i-check',
  info: '#i-info',
  error: '#i-warning',
};

interface KpiBreakdownItem {
  label: string;
  value: number;
  cssClass: string;
}

type KpiTone = 'red' | 'amber' | 'blue' | 'violet';

interface KpiCard {
  key: 'outOfStock' | 'lowStock' | 'activeListings' | 'reviewActions';
  label: string;
  value: number;
  percent: number;
  note: string;
  tone: KpiTone;
  actionLabel: string;
  positiveZero: boolean;
  zeroMessage: string;
  rule?: { label: string; cssClass: string };
  breakdown?: KpiBreakdownItem[];
}

interface ProductSummary {
  totalProducts: number;
  outOfStock: number;
  lowStock: number;
  lowStockThreshold: number;
  activeListings: number;
  productsWithListings: number;
  listingsByMarketplace: Record<Marketplace, number>;
  listingsNeedingReview: number;
  actionsBySeverity: {
    critical: number;
    medium: number;
    low: number;
  };
}

// SIMULAÇÃO até o contrato de /api/products/summary ser definido.
const SIMULATED_SUMMARY: ProductSummary = {
  totalProducts: 1248,
  outOfStock: 8,
  lowStock: 12,
  lowStockThreshold: 20,
  activeListings: 210,
  productsWithListings: 180,
  listingsByMarketplace: { SHOPEE: 120, MERCADOLIVRE: 90 },
  listingsNeedingReview: 37,
  actionsBySeverity: { critical: 8, medium: 15, low: 14 },
};

@Component({
  imports: [
    RouterLink,
    ProductStatusBadgeComponent,
    ConfirmDialogComponent,
    ProductBulkEditDialogComponent,
  ],
  selector: 'app-products-components',
  styleUrls: [
    './products-components.css',
    './products-components-table.css',
    './products-components-grid.css',
    './products-components-drawer.css',
    './products-components-ui.css',
    './products-components-select.css',
    './products-components-kpi.css',
    './products-components-actions.css',
  ],
  templateUrl: './products-components.html',
})
export class ProductsComponents implements OnInit, OnDestroy {
  private readonly document = inject(DOCUMENT);
  private readonly productService = inject(ProductService);
  private readonly productActions = inject(ProductActionsService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild('drawerClose') private drawerClose?: ElementRef<HTMLButtonElement>;
  @ViewChild('reviewClose') private reviewClose?: ElementRef<HTMLButtonElement>;

  readonly viewMode = signal<ViewMode>('table');
  readonly selectedCategoryIds = signal<ReadonlySet<string>>(new Set<string>());
  readonly categories = signal<CategoryRef[]>([]);
  readonly categoriesLoading = signal(false);
  readonly categoriesError = signal<string | null>(null);
  readonly marketplaceFilter = signal<MarketplaceFilter>('all');
  readonly adsFilter = signal<AdsFilter>('all');
  readonly stockFilter = signal<StockFilter>('all');
  readonly search = signal('');
  readonly selectedIds = signal<ReadonlySet<string>>(new Set<string>());
  readonly adsDrawerProduct = signal<ProductResponse | null>(null);
  readonly priceMode = signal<PriceMode>('percent');
  readonly priceValue = signal<number | null>(null);
  readonly stockMode = signal<StockMode>('set');
  readonly stockValue = signal<number | null>(null);
  readonly deactivateStep = signal<DeactivateStep>(0);
  readonly applyFeedback = signal<string | null>(null);
  readonly applyFeedbackTone = signal<'success' | 'error'>('success');
  readonly sortKey = signal<SortKey | null>(null);
  readonly sortDir = signal<SortDir>('asc');
  readonly page = signal(0);
  readonly pageSize = signal(20);
  readonly pageSizeOptions = PAGE_SIZES;

  readonly items = signal<ProductResponse[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly totalPages = signal(0);
  readonly totalItems = signal(0);
  readonly hasNext = signal(false);

  readonly toasts = signal<readonly ProductToast[]>([]);
  readonly failedImages = signal<ReadonlySet<string>>(new Set<string>());
  readonly openMenu = signal<FilterMenu | null>(null);
  readonly skeletonRows = Array.from({ length: 6 }, (_, index) => index);
  readonly skeletonCards = Array.from({ length: 8 }, (_, index) => index);

  private readonly reload$ = new Subject<void>();
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;
  private focusTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly scrollTimers = new Map<HTMLElement, ReturnType<typeof setTimeout>>();
  private readonly toastTimers = new Map<number, ReturnType<typeof setTimeout>>();
  private searchTimer: ReturnType<typeof setTimeout> | null = null;
  private toastSeq = 0;
  private previousFocus: HTMLElement | null = null;

  private readonly currencyFormatter = new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  });
  private readonly numberFormatter = new Intl.NumberFormat('pt-BR');
  private readonly dateFormatter = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' });
  private readonly percentFormatter = new Intl.NumberFormat('pt-BR', {
    maximumFractionDigits: 1,
  });

  readonly marketplaceOptions = (Object.keys(MARKETPLACES) as Marketplace[]).map((key) => ({
    key,
    label: MARKETPLACES[key].label,
  }));

  readonly adsOptions: { key: AdsFilter; label: string }[] = [
    { key: 'with', label: 'Com anúncios' },
    { key: 'without', label: 'Sem vínculos' },
  ];

  readonly stockOptions: { key: StockFilter; label: string }[] = [
    { key: 'low', label: 'Estoque baixo' },
    { key: 'out', label: 'Sem estoque' },
    { key: 'normal', label: 'Estoque saudável' },
  ];

  readonly summary = signal<ProductSummary>(SIMULATED_SUMMARY);
  readonly reviewDrawerOpen = signal(false);

  readonly statusActions: { value: ProductStatus; label: string }[] = [
    { value: 'ACTIVE', label: 'Ativar' },
    { value: 'PAUSED', label: 'Pausar' },
    { value: 'INACTIVE', label: 'Inativar' },
  ];
  readonly statusLabel = statusLabel;
  readonly stockState = stockState;

  readonly rowMenuProduct = signal<ProductResponse | null>(null);
  readonly rowMenuPosition = signal<RowMenuPosition | null>(null);
  readonly statusUpdatingId = signal<string | null>(null);

  readonly deleteTarget = signal<ProductResponse | null>(null);
  readonly deleteLoading = signal(false);
  readonly deleteError = signal<string | null>(null);

  readonly bulkEditOpen = signal(false);
  readonly bulkEditLoading = signal(false);
  readonly bulkEditError = signal<string | null>(null);

  readonly bulkDeleteOpen = signal(false);
  readonly bulkDeleteLoading = signal(false);
  readonly bulkDeleteError = signal<string | null>(null);

  get currentUrl(): string {
    return this.router.url;
  }

  constructor() {
    this.reload$
      .pipe(
        switchMap(() =>
          defer(() => {
            this.loading.set(true);
            this.error.set(null);

            return this.productService.getOverallProducts(this.buildQuery()).pipe(
              tap({
                next: (response) => this.applyPage(response),
                error: (error: HttpErrorResponse) => this.error.set(this.apiErrorMessage(error)),
              }),
              catchError((error: HttpErrorResponse) => {
                this.showToast(this.apiErrorMessage(error), 'error');
                return of(null);
              }),
              finalize(() => this.loading.set(false)),
            );
          }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();

    effect(() => {
      this.document.body.style.overflow = this.locksBody() ? 'hidden' : '';
    });
  }

  ngOnInit(): void {
    this.readNavigationFeedback();
    this.readFiltersFromUrl();
    this.loadCategories();
    this.reload();
  }

  reload(): void {
    this.clearSearchTimer();
    this.syncUrl();
    this.reload$.next();
  }

  private buildQuery(): {
    page: number;
    size: number;
    sort: string | null;
    searchTerm: string | null;
    marketplace: Marketplace | null;
    hasListings: boolean | null;
    stock: 'LOW' | 'OUT' | 'NORMAL' | null;
    categoryIds: string[] | null;
  } {
    const key = this.sortKey();
    const marketplace = this.marketplaceFilter();
    const ads = this.adsFilter();
    const stock = this.stockFilter();
    const categoryIds = this.selectedCategoryIds();

    return {
      page: this.page(),
      size: this.pageSize(),
      sort: key ? `${SORT_FIELDS[key]},${this.sortDir()}` : null,
      searchTerm: this.search().trim() || null,
      marketplace: marketplace === 'all' ? null : marketplace,
      hasListings: ads === 'all' ? null : ads === 'with',
      stock: stock === 'all' ? null : stock.toUpperCase() as 'LOW' | 'OUT' | 'NORMAL',
      categoryIds: categoryIds.size > 0 ? [...categoryIds] : null,
    };
  }

  private readFiltersFromUrl(): void {
    const params = this.route.snapshot.queryParamMap;

    this.search.set(params.get('searchTerm')?.trim() ?? '');

    this.selectedCategoryIds.set(
      new Set(params.getAll('categoryIds').filter((id) => UUID_PATTERN.test(id))),
    );

    const marketplace = params.get('marketplace');
    this.marketplaceFilter.set(
      marketplace && marketplace in MARKETPLACES ? (marketplace as Marketplace) : 'all',
    );

    const hasListings = params.get('hasListings');
    if (hasListings === 'true' || hasListings === 'false') {
      this.adsFilter.set(hasListings === 'true' ? 'with' : 'without');
    }

    const stock = params.get('stock');
    const stockMap: Record<string, StockFilter> = {
      LOW: 'low',
      OUT: 'out',
      NORMAL: 'normal',
    };
    this.stockFilter.set(stock && stock in stockMap ? stockMap[stock] : 'all');

    const size = Number(params.get('size'));
    if (PAGE_SIZES.includes(size)) {
      this.pageSize.set(size);
    }

    const page = Number(params.get('page'));
    if (Number.isInteger(page) && page >= 0) {
      this.page.set(page);
    }

    const sort = params.get('sort');
    if (sort) {
      const [field, direction] = sort.split(',');
      const key = (Object.keys(SORT_FIELDS) as SortKey[]).find(
        (candidate) => SORT_FIELDS[candidate] === field,
      );

      if (key) {
        this.sortKey.set(key);
        this.sortDir.set(direction === 'desc' ? 'desc' : 'asc');
      }
    }
  }

  private syncUrl(): void {
    const sortKey = this.sortKey();
    const marketplace = this.marketplaceFilter();
    const ads = this.adsFilter();
    const stock = this.stockFilter();
    const categoryIds = [...this.selectedCategoryIds()];

    const queryParams: Record<string, string | number | boolean | string[] | null> = {
      searchTerm: this.search().trim() || null,
      categoryIds: categoryIds.length > 0 ? categoryIds : null,
      marketplace: marketplace === 'all' ? null : marketplace,
      hasListings: ads === 'all' ? null : ads === 'with',
      stock: stock === 'all' ? null : stock.toUpperCase(),
      page: this.page() > 0 ? this.page() : null,
      size: this.pageSize() === PAGE_SIZES[0] ? null : this.pageSize(),
      sort: sortKey ? `${SORT_FIELDS[sortKey]},${this.sortDir()}` : null,
    };

    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
      replaceUrl: true,
    });
  }

  private apiErrorMessage(
    error: HttpErrorResponse,
    fallback = 'Não foi possível carregar o catálogo. Tente novamente.',
  ): string {
    const body = error.error as { errors?: string[]; detail?: string } | null;
    const validationMessage = body?.errors?.[0];

    if (validationMessage) {
      return validationMessage;
    }

    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
    }

    return body?.detail ?? fallback;
  }

  loadCategories(): void {
    if (this.categoriesLoading()) {
      return;
    }

    this.categoriesLoading.set(true);
    this.categoriesError.set(null);

    this.productService
      .getCategories()
      .pipe(
        catchError((error: HttpErrorResponse) => {
          const message = this.apiErrorMessage(error, 'Não foi possível carregar as categorias.');
          this.categoriesError.set(message);
          this.showToast(message, 'error');
          return of(null);
        }),
        finalize(() => this.categoriesLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((categories) => {
        if (categories) {
          this.categories.set(
            [...categories].sort((a, b) => a.name.localeCompare(b.name, 'pt-BR')),
          );
        }
      });
  }

  retryCategories(): void {
    this.loadCategories();
  }

  private applyPage(response: ProductPageResponse): void {
    if (
      response.items.length === 0 &&
      response.totalPage > 0 &&
      this.page() >= response.totalPage
    ) {
      this.page.set(Math.max(0, response.totalPage - 1));
      this.reload();
      return;
    }

    this.items.set(response.items);
    this.page.set(response.actualPage);
    this.totalPages.set(response.totalPage);
    this.totalItems.set(response.totalItems);
    this.hasNext.set(response.hasNext);

    if (PAGE_SIZES.includes(response.pageSize)) {
      this.pageSize.set(response.pageSize);
    }
  }

  readonly hasActiveFilters = computed(
    () =>
      this.search().trim() !== '' ||
      this.selectedCategoryIds().size > 0 ||
      this.marketplaceFilter() !== 'all' ||
      this.adsFilter() !== 'all' ||
      this.stockFilter() !== 'all',
  );

  readonly categoryFilterLabel = computed(() => {
    const count = this.selectedCategoryIds().size;
    return count > 0 ? `Categorias (${count})` : 'Categorias';
  });

  readonly noCategoryFilterSelected = computed(() => this.selectedCategoryIds().size === 0);

  readonly selectedCategories = computed(() => {
    const categoriesById = new Map(
      this.categories().map((category) => [category.publicId, category]),
    );
    const loading = this.categoriesLoading();

    return [...this.selectedCategoryIds()].map((publicId) => {
      const category = categoriesById.get(publicId);

      return {
        publicId,
        name: category?.name ?? (loading ? 'Carregando…' : 'Categoria indisponível'),
        type: category?.type ?? null,
      };
    });
  });

  readonly productCategoryOptions = computed(() =>
    this.categories().filter((category) => category.type === 'PRODUCT'),
  );

  readonly stateCategoryOptions = computed(() =>
    this.categories().filter((category) => category.type === 'STATE'),
  );

  readonly marketplaceFilterLabel = computed(() =>
    this.marketplaceFilter() === 'all'
      ? 'Marketplace'
      : MARKETPLACES[this.marketplaceFilter() as Marketplace].label,
  );

  readonly adsFilterLabel = computed(() =>
    this.adsFilter() === 'all'
      ? 'Anúncios'
      : this.adsFilter() === 'with'
        ? 'Com anúncios'
        : 'Sem vínculos',
  );

  readonly stockFilterLabel = computed(() => {
    const stock = this.stockFilter();

    if (stock === 'all') {
      return 'Estoque';
    }

    if (stock === 'low') {
      return 'Estoque baixo';
    }

    return stock === 'out' ? 'Sem estoque' : 'Estoque saudável';
  });

  readonly activeFilters = computed<ActiveFilter[]>(() => {
    const filters: ActiveFilter[] = [];
    const term = this.search().trim();

    if (term) {
      filters.push({ key: 'search', label: `Busca: "${term}"`, filter: 'search' });
    }

    for (const category of this.selectedCategories()) {
      const typeLabel = category.type ? categoryTypeLabel(category.type) : 'Categoria';
      filters.push({
        key: `category:${category.publicId}`,
        label: `${typeLabel}: ${category.name}`,
        filter: 'category',
        categoryId: category.publicId,
        categoryType: category.type ?? undefined,
      });
    }

    if (this.marketplaceFilter() !== 'all') {
      const label = MARKETPLACES[this.marketplaceFilter() as Marketplace].label;
      filters.push({
        key: 'marketplace',
        label: `Marketplace: ${label}`,
        filter: 'marketplace',
      });
    }

    if (this.adsFilter() !== 'all') {
      filters.push({
        key: 'ads',
        label: this.adsFilter() === 'with' ? 'Com anúncios' : 'Sem vínculos',
        filter: 'ads',
      });
    }

    if (this.stockFilter() !== 'all') {
      filters.push({ key: 'stock', label: this.stockFilterLabel(), filter: 'stock' });
    }

    return filters;
  });

  readonly selectedCount = computed(() => this.selectedIds().size);

  readonly pageRangeLabel = computed(() => {
    const list = this.items();

    if (list.length === 0) {
      return '0–0';
    }

    const start = this.page() * this.pageSize() + 1;
    return `${start}–${start + list.length - 1}`;
  });

  readonly pageNumbers = computed<PageNumber[]>(() => {
    const total = this.totalPages();
    const current = this.page() + 1;

    if (total <= 0) {
      return [];
    }

    if (total <= 7) {
      return Array.from({ length: total }, (_, index) => ({
        label: String(index + 1),
        value: index,
      }));
    }

    const keep = new Set<number>(
      [1, 2, current - 1, current, current + 1, total - 1, total].filter(
        (pageNumber) => pageNumber >= 1 && pageNumber <= total,
      ),
    );
    const selected = Array.from({ length: total }, (_, index) => index + 1).filter((index) =>
      keep.has(index),
    );
    const output: PageNumber[] = [];
    let previous = 0;

    for (const pageNumber of selected) {
      if (pageNumber - previous > 1) {
        output.push({ label: '…', value: null });
      }

      output.push({ label: String(pageNumber), value: pageNumber - 1 });
      previous = pageNumber;
    }

    return output;
  });

  readonly allSelected = computed(() => {
    const list = this.items();
    return list.length > 0 && list.every((product) => this.selectedIds().has(product.publicId));
  });

  readonly someSelected = computed(() => this.selectedIds().size > 0 && !this.allSelected());

  readonly sortActive = computed(() => this.sortKey() !== null);

  readonly sortChipLabel = computed(() => {
    const key = this.sortKey();
    return key ? SORT_LABELS[key] : null;
  });

  readonly overlayOpen = computed(
    () =>
      this.adsDrawerProduct() !== null ||
      this.reviewDrawerOpen() ||
      this.deleteTarget() !== null ||
      this.bulkEditOpen() ||
      this.bulkDeleteOpen(),
  );

  private readonly locksBody = computed(
    () =>
      this.adsDrawerProduct() !== null ||
      this.reviewDrawerOpen() ||
      this.bulkEditOpen(),
  );

  private percentOf(part: number, total: number): number {
    return total > 0 ? (part / total) * 100 : 0;
  }

  isPositiveZero(card: KpiCard): boolean {
    return card.value === 0 && card.positiveZero;
  }

  readonly kpiCards = computed<KpiCard[]>(() => {
    const summary = this.summary();
    const totalProducts = summary.totalProducts;
    const outPercent = this.percentOf(summary.outOfStock, totalProducts);
    const lowPercent = this.percentOf(summary.lowStock, totalProducts);
    const adsPercent = this.percentOf(summary.productsWithListings, totalProducts);
    const reviewPercent = this.percentOf(
      summary.listingsNeedingReview,
      summary.activeListings,
    );

    const marketplaces = (Object.keys(MARKETPLACES) as Marketplace[])
      .map((key) => ({
        label: MARKETPLACES[key].label,
        value: summary.listingsByMarketplace[key] ?? 0,
        cssClass: MARKETPLACES[key].cssClass,
      }))
      .filter((item) => item.value > 0);

    const severities: KpiBreakdownItem[] = [
      { label: 'críticos', value: summary.actionsBySeverity.critical, cssClass: 'sev-critical' },
      { label: 'medianos', value: summary.actionsBySeverity.medium, cssClass: 'sev-medium' },
      { label: 'baixos', value: summary.actionsBySeverity.low, cssClass: 'sev-low' },
    ].filter((item) => item.value > 0);

    return [
      {
        key: 'reviewActions',
        label: 'Ações a fazer',
        value: summary.listingsNeedingReview,
        percent: reviewPercent,
        note:
          summary.listingsNeedingReview === 0
            ? 'Nenhuma pendência'
            : `${this.percentFormatter.format(reviewPercent)}% dos anúncios ativos`,
        tone: 'violet',
        actionLabel: 'Ver detalhes',
        positiveZero: true,
        zeroMessage: 'Nenhuma ação pendente',
        breakdown: severities,
      },
      {
        key: 'outOfStock',
        label: 'Sem estoque',
        value: summary.outOfStock,
        percent: outPercent,
        note: `${this.percentFormatter.format(outPercent)}% do catálogo`,
        tone: 'red',
        actionLabel: 'Ver produtos',
        positiveZero: true,
        zeroMessage: 'Nenhum produto sem estoque',
        rule: { label: '0 unidades em estoque', cssClass: 'sev-critical' },
      },
      {
        key: 'lowStock',
        label: 'Estoque baixo',
        value: summary.lowStock,
        percent: lowPercent,
        note: `${this.percentFormatter.format(lowPercent)}% do catálogo`,
        tone: 'amber',
        actionLabel: 'Ver produtos',
        positiveZero: true,
        zeroMessage: 'Estoque saudável',
        rule: { label: 'abaixo do mínimo do produto', cssClass: 'sev-medium' },
      },
      {
        key: 'activeListings',
        label: 'Anúncios ativos',
        value: summary.activeListings,
        percent: adsPercent,
        note:
          summary.activeListings === 0
            ? 'Nenhum anúncio ativo'
            : `${this.percentFormatter.format(adsPercent)}% dos produtos anunciados`,
        tone: 'blue',
        actionLabel: 'Ver anunciados',
        positiveZero: false,
        zeroMessage: 'Nenhum anúncio ativo',
        breakdown: marketplaces,
      },
    ];
  });

  readonly reviewSeveritySegments = computed(() => {
    const { critical, medium, low } = this.summary().actionsBySeverity;
    const total = critical + medium + low;

    return [
      { cssClass: 'sev-critical', percent: total > 0 ? (critical / total) * 100 : 0 },
      { cssClass: 'sev-medium', percent: total > 0 ? (medium / total) * 100 : 0 },
      { cssClass: 'sev-low', percent: total > 0 ? (low / total) * 100 : 0 },
    ];
  });

  readonly reviewBreakdown = computed<KpiBreakdownItem[]>(
    () => this.kpiCards().find((card) => card.key === 'reviewActions')?.breakdown ?? [],
  );

  readonly pricePreview = computed(() => {
    const product = this.adsDrawerProduct();
    const value = this.priceValue();

    if (!product || value === null || !Number.isFinite(value)) {
      return null;
    }

    const base = product.baseSellPrice;

    if (base === null || !Number.isFinite(base)) {
      return null;
    }

    const next = this.priceMode() === 'percent' ? base * (1 + value / 100) : value;

    return this.formatCurrency(next);
  });

  readonly stockPreview = computed(() => {
    const product = this.adsDrawerProduct();
    const value = this.stockValue();

    if (!product || value === null || !Number.isFinite(value)) {
      return null;
    }

    return Math.max(0, this.stockMode() === 'set' ? value : product.stock + value);
  });

  setView(mode: ViewMode): void {
    this.viewMode.set(mode);
  }

  onSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
    this.scheduleSearch();
  }

  onSearchKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.applySearchNow();
      return;
    }

    if (event.key === 'Escape' && this.search()) {
      event.preventDefault();
      this.clearSearch();
    }
  }

  clearSearch(): void {
    this.clearSearchTimer();

    if (this.search() === '') {
      return;
    }

    this.search.set('');
    this.page.set(0);
    this.clearSelection();
    this.reload();
  }

  private scheduleSearch(): void {
    this.clearSearchTimer();
    this.searchTimer = setTimeout(() => {
      this.searchTimer = null;
      this.applySearchNow();
    }, 350);
  }

  private applySearchNow(): void {
    this.clearSearchTimer();
    this.page.set(0);
    this.clearSelection();
    this.reload();
  }

  private clearSearchTimer(): void {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer);
      this.searchTimer = null;
    }
  }

  toggleMenu(menu: FilterMenu): void {
    this.openMenu.update((current) => (current === menu ? null : menu));
  }

  closeMenus(): void {
    this.openMenu.set(null);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;

    if (!target?.closest('.select-wrap')) {
      this.closeMenus();
    }

    if (!target?.closest('.row-menu') && !target?.closest('.row-menu-trigger')) {
      this.closeRowMenu();
    }
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    this.closeRowMenu();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.rowMenuProduct()) {
      this.closeRowMenu();
      return;
    }

    if (this.deleteTarget()) {
      this.closeDeleteConfirm();
      return;
    }

    if (this.bulkDeleteOpen()) {
      this.closeBulkDelete();
      return;
    }

    if (this.bulkEditOpen()) {
      this.closeBulkEdit();
      return;
    }

    if (this.adsDrawerProduct()) {
      this.closeAdsDrawer();
      return;
    }

    if (this.reviewDrawerOpen()) {
      this.closeReviewDrawer();
      return;
    }

    this.closeMenus();
  }

  openRowMenu(event: MouseEvent, product: ProductResponse): void {
    event.stopPropagation();
    this.clearFocusTimer();

    const trigger = event.currentTarget as HTMLElement | null;
    const view = this.document.defaultView;

    if (!trigger || !view) {
      return;
    }

    const rect = trigger.getBoundingClientRect();
    const width = 234;
    const estimatedHeight = 330;
    const left = Math.min(Math.max(8, rect.right - width), view.innerWidth - width - 8);
    const top = Math.min(rect.bottom + 6, view.innerHeight - estimatedHeight - 8);

    this.rowMenuProduct.set(product);
    this.rowMenuPosition.set({ top: Math.max(8, top), left });
    this.focusTimer = setTimeout(() => {
      this.focusTimer = null;
      this.document.querySelector<HTMLElement>('.row-menu__item')?.focus();
    }, 0);
  }

  closeRowMenu(): void {
    if (!this.rowMenuProduct()) {
      return;
    }

    this.rowMenuProduct.set(null);
    this.rowMenuPosition.set(null);
    this.clearFocusTimer();
  }

  onRowMenuKeydown(event: KeyboardEvent): void {
    const menu = event.currentTarget as HTMLElement;
    const items = Array.from(menu.querySelectorAll<HTMLElement>('.row-menu__item'));

    if (items.length === 0) {
      return;
    }

    const activeIndex = items.indexOf(this.document.activeElement as HTMLElement);

    if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();
      this.closeRowMenu();
      return;
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      items[(activeIndex + 1 + items.length) % items.length].focus();
      return;
    }

    if (event.key === 'ArrowUp') {
      event.preventDefault();
      items[(activeIndex - 1 + items.length) % items.length].focus();
      return;
    }

    if (event.key === 'Home') {
      event.preventDefault();
      items[0].focus();
      return;
    }

    if (event.key === 'End') {
      event.preventDefault();
      items[items.length - 1].focus();
    }
  }

  openProduct(product: ProductResponse): void {
    this.closeRowMenu();
    void this.router.navigate(['/produtos', product.publicId], {
      queryParams: { returnUrl: this.currentUrl },
    });
  }

  editProduct(product: ProductResponse): void {
    this.closeRowMenu();
    void this.router.navigate(['/produtos', product.publicId, 'editar'], {
      queryParams: { returnUrl: this.currentUrl },
    });
  }

  duplicateProduct(product: ProductResponse): void {
    this.closeRowMenu();
    void this.router.navigate(['/produtos/novo'], {
      queryParams: { duplicarDe: product.publicId, returnUrl: this.currentUrl },
    });
  }

  copyProductLink(product: ProductResponse): void {
    const origin = this.document.defaultView?.location.origin ?? '';
    this.copyToClipboard(`${origin}/produtos/${product.publicId}`, 'Link do produto');
  }

  copySku(product: ProductResponse): void {
    if (!product.sku) {
      this.showToast('Este produto não possui SKU.', 'error');
      return;
    }

    this.copyToClipboard(product.sku, 'SKU');
  }

  setProductStatus(product: ProductResponse, status: ProductStatus): void {
    if (this.statusUpdatingId()) {
      return;
    }

    this.statusUpdatingId.set(product.publicId);

    this.productActions
      .updateStatus(product.publicId, status)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.items.update((items) => items.filter((item) => item.publicId !== product.publicId));
            this.removeIdsFromSelection([product.publicId]);
            this.reload();
            this.showToast('O produto não existe mais ou foi desabilitado.', 'error');
          } else {
            this.showToast(
              this.apiErrorMessage(error, 'Não foi possível alterar o status do produto.'),
              'error',
            );
          }

          return of(false);
        }),
        finalize(() => this.statusUpdatingId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (result === false) {
          return;
        }

        this.items.update((items) =>
          items.map((item) =>
            item.publicId === product.publicId ? { ...item, status } : item,
          ),
        );

        if (this.adsDrawerProduct()?.publicId === product.publicId) {
          this.adsDrawerProduct.update((current) =>
            current ? { ...current, status } : current,
          );
        }

        this.showToast(
          `Status de ${product.name} atualizado para ${statusLabel(status)}.`,
          'success',
        );
        this.closeRowMenu();
      });
  }

  openDeleteConfirm(product: ProductResponse): void {
    this.closeRowMenu();
    this.deleteError.set(null);
    this.deleteTarget.set(product);
  }

  closeDeleteConfirm(): void {
    if (this.deleteLoading()) {
      return;
    }

    this.deleteTarget.set(null);
    this.deleteError.set(null);
  }

  confirmDelete(): void {
    const product = this.deleteTarget();

    if (!product || this.deleteLoading()) {
      return;
    }

    this.deleteLoading.set(true);
    this.deleteError.set(null);

    this.productActions
      .delete(product.publicId)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.finishDelete(product, 'Produto já havia sido removido.');
            return of(false);
          }

          this.deleteError.set(
            this.apiErrorMessage(error, 'Não foi possível excluir o produto.'),
          );
          return of(false);
        }),
        finalize(() => this.deleteLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (result === false) {
          return;
        }

        this.finishDelete(product, 'Produto excluído com sucesso.');
      });
  }

  openBulkEdit(): void {
    if (this.selectedCount() === 0) {
      return;
    }

    if (this.selectedCount() > 200) {
      this.showToast('Selecione no máximo 200 produtos para editar em massa.', 'error');
      return;
    }

    this.bulkEditError.set(null);
    this.bulkEditOpen.set(true);
  }

  closeBulkEdit(): void {
    if (!this.bulkEditLoading()) {
      this.bulkEditOpen.set(false);
      this.bulkEditError.set(null);
    }
  }

  onBulkEditSubmitted(payload: ProductBulkEditPayload): void {
    if (this.bulkEditLoading()) {
      return;
    }

    const selectedProducts = this.items().filter((product) =>
      this.selectedIds().has(product.publicId),
    );

    if (selectedProducts.length === 0) {
      this.bulkEditError.set('Nenhum produto selecionado.');
      return;
    }

    if (selectedProducts.length > 200) {
      this.bulkEditError.set('Selecione no máximo 200 produtos.');
      return;
    }

    this.bulkEditLoading.set(true);
    this.bulkEditError.set(null);

    this.productActions
      .updateMany(selectedProducts, payload)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.bulkEditError.set(
            this.apiErrorMessage(error, 'Não foi possível editar os produtos em massa.'),
          );
          return of(null);
        }),
        finalize(() => this.bulkEditLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((updated) => {
        if (!updated) {
          return;
        }

        this.bulkEditOpen.set(false);
        this.clearSelection();
        this.showToast(`${updated.length} produto(s) atualizado(s) em massa.`, 'success');
        this.reload();
      });
  }

  openBulkDelete(): void {
    if (this.selectedCount() === 0) {
      return;
    }

    if (this.selectedCount() > 200) {
      this.showToast('Selecione no máximo 200 produtos para excluir em massa.', 'error');
      return;
    }

    this.bulkDeleteError.set(null);
    this.bulkDeleteOpen.set(true);
  }

  closeBulkDelete(): void {
    if (!this.bulkDeleteLoading()) {
      this.bulkDeleteOpen.set(false);
      this.bulkDeleteError.set(null);
    }
  }

  confirmBulkDelete(): void {
    const ids = [...this.selectedIds()];

    if (ids.length === 0 || this.bulkDeleteLoading()) {
      return;
    }

    this.bulkDeleteLoading.set(true);
    this.bulkDeleteError.set(null);

    this.productActions
      .deleteMany(ids)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.finishBulkDelete(
              ids,
              'Alguns produtos já não existiam mais. A lista foi atualizada.',
              'info',
            );
            return of(false);
          }

          this.bulkDeleteError.set(
            this.apiErrorMessage(error, 'Não foi possível excluir os produtos selecionados.'),
          );
          return of(false);
        }),
        finalize(() => this.bulkDeleteLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (result === false) {
          return;
        }

        this.finishBulkDelete(ids, `${ids.length} produto(s) excluído(s).`, 'success');
      });
  }

  private finishDelete(product: ProductResponse, message: string): void {
    this.deleteTarget.set(null);
    this.deleteError.set(null);
    this.items.update((items) => items.filter((item) => item.publicId !== product.publicId));
    this.removeIdsFromSelection([product.publicId]);

    if (this.adsDrawerProduct()?.publicId === product.publicId) {
      this.closeAdsDrawer();
    }

    if (this.items().length === 0 && this.page() > 0) {
      this.page.update((page) => page - 1);
    }

    this.showToast(message, 'success');
    this.reload();
  }

  private finishBulkDelete(ids: string[], message: string, tone: ToastTone): void {
    this.bulkDeleteOpen.set(false);
    this.bulkDeleteError.set(null);
    this.selectedIds.set(new Set<string>());
    this.items.update((items) => items.filter((item) => !ids.includes(item.publicId)));

    if (this.items().length === 0 && this.page() > 0) {
      this.page.update((page) => page - 1);
    }

    this.showToast(message, tone);
    this.reload();
  }

  private removeIdsFromSelection(ids: string[]): void {
    if (ids.length === 0) {
      return;
    }

    const idSet = new Set(ids);
    const next = new Set(this.selectedIds());

    for (const id of idSet) {
      next.delete(id);
    }

    this.selectedIds.set(next);
  }

  private copyToClipboard(value: string, label: string): void {
    const navigatorRef = this.document.defaultView?.navigator;

    if (navigatorRef?.clipboard) {
      navigatorRef.clipboard
        .writeText(value)
        .then(() => this.showToast(`${label} copiado.`, 'success'))
        .catch(() => this.showToast('Não foi possível copiar.', 'error'));
      return;
    }

    const textarea = this.document.createElement('textarea');
    textarea.value = value;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    this.document.body.appendChild(textarea);
    textarea.select();
    const copied = this.document.execCommand('copy');
    textarea.remove();
    this.showToast(
      copied ? `${label} copiado.` : 'Não foi possível copiar.',
      copied ? 'success' : 'error',
    );
  }

  private readNavigationFeedback(): void {
    const history = this.document.defaultView?.history;
    const state = history?.state as
      | { productFeedback?: string; productFeedbackTone?: ToastTone }
      | null;

    if (state?.productFeedback) {
      this.showToast(state.productFeedback, state.productFeedbackTone ?? 'success');
      history?.replaceState(
        { ...state, productFeedback: undefined, productFeedbackTone: undefined },
        '',
      );
    }
  }


  isCategorySelected(publicId: string): boolean {
    return this.selectedCategoryIds().has(publicId);
  }

  toggleCategory(publicId: string): void {
    const next = new Set(this.selectedCategoryIds());
    next.has(publicId) ? next.delete(publicId) : next.add(publicId);
    this.selectedCategoryIds.set(next);
    this.applyCategoryFilter();
  }

  clearCategories(): void {
    if (this.selectedCategoryIds().size === 0) {
      return;
    }

    this.selectedCategoryIds.set(new Set<string>());
    this.applyCategoryFilter();
  }

  removeCategory(publicId: string): void {
    if (!this.selectedCategoryIds().has(publicId)) {
      return;
    }

    const next = new Set(this.selectedCategoryIds());
    next.delete(publicId);
    this.selectedCategoryIds.set(next);
    this.applyCategoryFilter();
  }

  clearActiveFilter(filter: ActiveFilter): void {
    if (filter.filter === 'category' && filter.categoryId) {
      this.removeCategory(filter.categoryId);
      return;
    }

    this.clearFilter(filter.filter);
  }

  private applyCategoryFilter(): void {
    this.page.set(0);
    this.clearSelection();
    this.reload();
  }

  chooseMarketplace(value: MarketplaceFilter): void {
    if (this.marketplaceFilter() === value) {
      this.closeMenus();
      return;
    }

    this.marketplaceFilter.set(value);
    this.page.set(0);
    this.clearSelection();
    this.closeMenus();
    this.reload();
  }

  chooseAds(value: AdsFilter): void {
    if (this.adsFilter() === value) {
      this.closeMenus();
      return;
    }

    this.adsFilter.set(value);
    this.page.set(0);
    this.clearSelection();
    this.closeMenus();
    this.reload();
  }

  chooseStock(value: StockFilter): void {
    if (this.stockFilter() === value) {
      this.closeMenus();
      return;
    }

    this.stockFilter.set(value);
    this.page.set(0);
    this.clearSelection();
    this.closeMenus();
    this.reload();
  }

  onScroll(event: Event): void {
    this.closeRowMenu();
    const target = event.currentTarget as HTMLElement | null;

    if (!target) {
      return;
    }

    target.classList.add('is-scrolling');

    const currentTimer = this.scrollTimers.get(target);

    if (currentTimer) {
      clearTimeout(currentTimer);
    }

    this.scrollTimers.set(
      target,
      setTimeout(() => {
        this.scrollTimers.delete(target);
        target.classList.remove('is-scrolling');
      }, 700),
    );
  }

  clearFilter(key: FilterKey): void {
    if (key === 'search') {
      this.clearSearchTimer();
      this.search.set('');
    } else if (key === 'category') {
      this.selectedCategoryIds.set(new Set<string>());
    } else if (key === 'marketplace') {
      this.marketplaceFilter.set('all');
    } else if (key === 'ads') {
      this.adsFilter.set('all');
    } else if (key === 'stock') {
      this.stockFilter.set('all');
    }

    this.page.set(0);
    this.clearSelection();
    this.reload();
  }

  clearFilters(): void {
    this.clearSearchTimer();
    this.search.set('');
    this.selectedCategoryIds.set(new Set<string>());
    this.marketplaceFilter.set('all');
    this.adsFilter.set('all');
    this.stockFilter.set('all');
    this.sortKey.set(null);
    this.sortDir.set('asc');
    this.page.set(0);
    this.clearSelection();
    this.reload();
  }

  openAdsDrawer(product: ProductResponse): void {
    this.closeRowMenu();
    this.clearFeedbackTimer();
    this.clearFocusTimer();
    this.previousFocus =
      this.document.activeElement instanceof HTMLElement ? this.document.activeElement : null;
    this.adsDrawerProduct.set(product);
    this.priceMode.set('percent');
    this.priceValue.set(null);
    this.stockMode.set('set');
    this.stockValue.set(null);
    this.deactivateStep.set(0);
    this.applyFeedback.set(null);
    this.applyFeedbackTone.set('success');

    this.focusTimer = setTimeout(() => {
      this.focusTimer = null;
      this.drawerClose?.nativeElement.focus();
    }, 0);
  }

  closeAdsDrawer(): void {
    this.clearFeedbackTimer();
    this.clearFocusTimer();
    this.adsDrawerProduct.set(null);
    this.deactivateStep.set(0);
    this.applyFeedback.set(null);

    if (this.previousFocus) {
      const focusTarget = this.previousFocus;
      this.previousFocus = null;
      this.focusTimer = setTimeout(() => {
        this.focusTimer = null;
        focusTarget.focus();
      }, 0);
    }
  }

  setPriceMode(mode: PriceMode): void {
    this.priceMode.set(mode);
  }

  setStockMode(mode: StockMode): void {
    this.stockMode.set(mode);
  }

  addPricePercent(amount: number): void {
    this.priceMode.set('percent');
    this.priceValue.update((value) => (value ?? 0) + amount);
  }

  addStock(amount: number): void {
    this.stockMode.set('adjust');
    this.stockValue.update((value) => (value ?? 0) + amount);
  }

  onPriceInput(event: Event): void {
    const raw = (event.target as HTMLInputElement).value;
    const value = Number(raw);
    this.priceValue.set(raw === '' || !Number.isFinite(value) ? null : value);
  }

  onStockInput(event: Event): void {
    const raw = (event.target as HTMLInputElement).value;
    const value = Number(raw);
    this.stockValue.set(raw === '' || !Number.isFinite(value) ? null : value);
  }

  applyBulkChanges(): void {
    const product = this.adsDrawerProduct();

    if (!product) {
      return;
    }

    if (this.priceValue() === null && this.stockValue() === null) {
      this.showFeedback('Não foi possível aplicar: informe um novo preço ou estoque.', 'error');
      return;
    }

    this.showFeedback(
      `Alterações aplicadas com sucesso em ${this.marketplaceTotal(product)} anúncios de ${product.name}.`,
      'success',
    );
  }

  requestDeactivate(): void {
    if (this.adsDrawerProduct()) {
      this.deactivateStep.set(1);
    }
  }

  cancelDeactivate(): void {
    this.deactivateStep.set(0);
  }

  continueDeactivate(): void {
    this.deactivateStep.set(2);
  }

  confirmDeactivate(): void {
    const product = this.adsDrawerProduct();

    if (!product) {
      return;
    }

    this.deactivateStep.set(3);
    this.showFeedback(
      `Produto desativado com sucesso em ${this.marketplaceTotal(product)} anúncios de ${product.name}.`,
      'success',
    );
  }

  onDrawerKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();
      this.closeAdsDrawer();
      return;
    }

    if (event.key !== 'Tab') {
      return;
    }

    const container = event.currentTarget as HTMLElement;
    const focusable = Array.from(
      container.querySelectorAll<HTMLElement>(
        'button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), a[href], [tabindex]:not([tabindex="-1"])',
      ),
    ).filter((element) => element.getClientRects().length > 0);

    if (focusable.length === 0) {
      return;
    }

    const first = focusable[0];
    const last = focusable[focusable.length - 1];

    if (event.shiftKey && this.document.activeElement === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && this.document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  }

  ngOnDestroy(): void {
    this.clearFeedbackTimer();
    this.clearFocusTimer();
    this.clearSearchTimer();
    this.clearScrollTimers();
    this.clearToastTimers();
    this.document.body.style.overflow = '';
  }

  private showFeedback(message: string, tone: 'success' | 'error'): void {
    this.clearFeedbackTimer();
    this.applyFeedbackTone.set(tone);
    this.applyFeedback.set(message);

    if (tone === 'success') {
      this.feedbackTimer = setTimeout(() => {
        this.feedbackTimer = null;
        this.closeAdsDrawer();
      }, 2500);
    }
  }

  private clearFeedbackTimer(): void {
    if (this.feedbackTimer) {
      clearTimeout(this.feedbackTimer);
      this.feedbackTimer = null;
    }
  }

  private clearFocusTimer(): void {
    if (this.focusTimer) {
      clearTimeout(this.focusTimer);
      this.focusTimer = null;
    }
  }

  private clearScrollTimers(): void {
    for (const timer of this.scrollTimers.values()) {
      clearTimeout(timer);
    }

    this.scrollTimers.clear();
  }

  toggleSelection(id: string): void {
    const next = new Set(this.selectedIds());
    next.has(id) ? next.delete(id) : next.add(id);
    this.selectedIds.set(next);
  }

  toggleAll(): void {
    const pageIds = this.items().map((product) => product.publicId);

    if (this.allSelected()) {
      const remaining = new Set(this.selectedIds());

      for (const id of pageIds) {
        remaining.delete(id);
      }

      this.selectedIds.set(remaining);
      return;
    }

    const next = new Set(this.selectedIds());

    for (const id of pageIds) {
      next.add(id);
    }

    this.selectedIds.set(next);
  }

  isSelected(id: string): boolean {
    return this.selectedIds().has(id);
  }

  clearSelection(): void {
    this.selectedIds.set(new Set<string>());
  }

  sortState(key: SortKey): 'ascending' | 'descending' | null {
    if (this.sortKey() !== key) {
      return null;
    }

    return this.sortDir() === 'asc' ? 'ascending' : 'descending';
  }

  setSort(key: SortKey): void {
    if (this.sortKey() !== key) {
      this.sortKey.set(key);
      this.sortDir.set('asc');
    } else if (this.sortDir() === 'asc') {
      this.sortDir.set('desc');
    } else {
      this.clearSort();
      return;
    }

    this.page.set(0);
    this.clearSelection();
    this.reload();
  }

  clearSort(): void {
    if (this.sortKey() === null) {
      return;
    }

    this.sortKey.set(null);
    this.sortDir.set('asc');
    this.page.set(0);
    this.clearSelection();
    this.reload();
  }

  goToPage(index: number): void {
    if (index < 0 || index >= this.totalPages() || index === this.page()) {
      return;
    }

    this.page.set(index);
    this.clearSelection();
    this.reload();
  }

  choosePageSize(size: number): void {
    if (!PAGE_SIZES.includes(size)) {
      return;
    }

    this.pageSize.set(size);
    this.page.set(0);
    this.clearSelection();
    this.closeMenus();
    this.reload();
  }

  relativeUpdated(product: ProductResponse): string {
    const date = new Date(product.updatedAt);

    if (Number.isNaN(date.getTime())) {
      return '--';
    }

    const diffDays = Math.round((Date.now() - date.getTime()) / 86_400_000);

    if (diffDays <= 1) {
      return diffDays <= 0 ? 'hoje' : 'ontem';
    }

    return `há ${diffDays} dias`;
  }

  formatDate(iso: string): string {
    const date = new Date(iso);

    if (Number.isNaN(date.getTime())) {
      return '--';
    }

    return this.dateFormatter.format(date);
  }

  formatCurrency(value: number | null | undefined): string {
    return value == null ? '--' : this.currencyFormatter.format(value);
  }

  formatNumber(value: number | null | undefined): string {
    return this.numberFormatter.format(value ?? 0);
  }

  productCategories(product: ProductResponse): CategoryRef[] {
    return product.categories ?? [];
  }

  categoriesByType(product: ProductResponse, type: CategoryType): CategoryRef[] {
    return this.productCategories(product).filter((category) => category.type === type);
  }

  hasCategories(product: ProductResponse): boolean {
    return this.productCategories(product).length > 0;
  }

  hasListings(product: ProductResponse): boolean {
    return this.marketplaceTotal(product) > 0;
  }

  isCurrentStatus(product: ProductResponse, status: ProductStatus): boolean {
    return product.status === status;
  }

  categoryNames(product: ProductResponse): string {
    const productNames = this.categoriesByType(product, 'PRODUCT').map((category) => category.name);
    const stateNames = this.categoriesByType(product, 'STATE').map((category) => category.name);
    const parts: string[] = [];

    if (productNames.length > 0) {
      parts.push(`Produto: ${productNames.join(', ')}`);
    }

    if (stateNames.length > 0) {
      parts.push(`Estado: ${stateNames.join(', ')}`);
    }

    return parts.length > 0 ? parts.join(' · ') : 'Sem categoria';
  }

  toneFor(product: ProductResponse): string {
    const tones = ['tone-1', 'tone-2', 'tone-3'];
    let hash = 0;

    for (let index = 0; index < product.publicId.length; index += 1) {
      hash = (hash * 31 + product.publicId.charCodeAt(index)) >>> 0;
    }

    return tones[hash % tones.length];
  }

  marketplaceEntries(product: ProductResponse): MarketplaceEntry[] {
    return (Object.keys(MARKETPLACES) as Marketplace[])
      .map((key) => ({
        key,
        ads: product.listingCount?.[key] ?? 0,
        label: MARKETPLACES[key].label,
        cssClass: MARKETPLACES[key].cssClass,
      }))
      .filter((entry) => entry.ads > 0);
  }

  marketplaceTotal(product: ProductResponse): number {
    return this.marketplaceEntries(product).reduce((total, entry) => total + entry.ads, 0);
  }

  hasImage(product: ProductResponse): boolean {
    return Boolean(product.imageUrl) && !this.failedImages().has(product.publicId);
  }

  onImageError(publicId: string): void {
    this.failedImages.update((current) => new Set([...current, publicId]));
  }

  onMoreOptions(): void {
    this.showToast('Mais opções ficam disponíveis em breve.', 'info');
  }

  onKpiClick(card: KpiCard): void {
    switch (card.key) {
      case 'outOfStock':
        this.chooseStock('out');
        this.scrollToCatalog();
        break;
      case 'lowStock':
        this.chooseStock('low');
        this.scrollToCatalog();
        break;
      case 'activeListings':
        this.chooseAds('with');
        this.scrollToCatalog();
        break;
      case 'reviewActions':
        this.openReviewDrawer();
        break;
    }
  }

  openReviewDrawer(): void {
    this.closeMenus();
    this.clearFocusTimer();
    this.reviewDrawerOpen.set(true);
    this.focusTimer = setTimeout(() => {
      this.focusTimer = null;
      this.reviewClose?.nativeElement.focus();
    }, 0);
  }

  closeReviewDrawer(): void {
    this.clearFocusTimer();
    this.reviewDrawerOpen.set(false);
  }

  private scrollToCatalog(): void {
    this.document.querySelector('.panel')?.scrollIntoView({
      behavior: 'smooth',
      block: 'start',
    });
  }

  onLinkMarketplace(product: ProductResponse): void {
    this.showToast(`Vinculação de marketplace para ${product.name}.`, 'info');
  }

  dismissToast(id: number): void {
    const timer = this.toastTimers.get(id);

    if (timer) {
      clearTimeout(timer);
      this.toastTimers.delete(id);
    }

    this.toasts.update((items) => items.filter((toast) => toast.id !== id));
  }

  toastIcon(tone: ToastTone): string {
    return TOAST_ICONS[tone];
  }

  private showToast(message: string, tone: ToastTone): void {
    const id = ++this.toastSeq;

    this.toasts.update((items) => [...items.slice(-2), { id, message, tone }]);

    this.toastTimers.set(
      id,
      setTimeout(() => {
        this.toastTimers.delete(id);
        this.dismissToast(id);
      }, 3500),
    );
  }

  private clearToastTimers(): void {
    for (const timer of this.toastTimers.values()) {
      clearTimeout(timer);
    }

    this.toastTimers.clear();
  }
}
