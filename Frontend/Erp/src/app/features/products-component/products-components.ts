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
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, catchError, defer, finalize, of, switchMap, tap } from 'rxjs';
import { Marketplace, ProductPageResponse, ProductResponse } from '../../models/productModels';
import { ProductService } from '../../service/product-service';

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
  imports: [],
  selector: 'app-products-components',
  styleUrls: [
    './products-components.css',
    './products-components-table.css',
    './products-components-grid.css',
    './products-components-drawer.css',
    './products-components-ui.css',
    './products-components-select.css',
    './products-components-kpi.css',
  ],
  templateUrl: './products-components.html',
})
export class ProductsComponents implements OnInit, OnDestroy {
  private readonly document = inject(DOCUMENT);
  private readonly productService = inject(ProductService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  @ViewChild('drawerClose') private drawerClose?: ElementRef<HTMLButtonElement>;
  @ViewChild('reviewClose') private reviewClose?: ElementRef<HTMLButtonElement>;

  readonly viewMode = signal<ViewMode>('table');
  readonly categoryFilter = signal('all');
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
  }

  ngOnInit(): void {
    this.readFiltersFromUrl();
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
  } {
    const key = this.sortKey();
    const marketplace = this.marketplaceFilter();
    const ads = this.adsFilter();
    const stock = this.stockFilter();

    return {
      page: this.page(),
      size: this.pageSize(),
      sort: key ? `${SORT_FIELDS[key]},${this.sortDir()}` : null,
      searchTerm: this.search().trim() || null,
      marketplace: marketplace === 'all' ? null : marketplace,
      hasListings: ads === 'all' ? null : ads === 'with',
      stock: stock === 'all' ? null : stock.toUpperCase() as 'LOW' | 'OUT' | 'NORMAL',
    };
  }

  private readFiltersFromUrl(): void {
    const params = this.route.snapshot.queryParamMap;

    this.search.set(params.get('searchTerm')?.trim() ?? '');

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

    const queryParams: Record<string, string | number | boolean | null> = {
      searchTerm: this.search().trim() || null,
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

  private apiErrorMessage(error: HttpErrorResponse): string {
    const body = error.error as { errors?: string[]; detail?: string } | null;
    const validationMessage = body?.errors?.[0];

    if (validationMessage) {
      return validationMessage;
    }

    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
    }

    return body?.detail ?? 'Não foi possível carregar o catálogo. Tente novamente.';
  }

  private applyPage(response: ProductPageResponse): void {
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
      this.categoryFilter() !== 'all' ||
      this.marketplaceFilter() !== 'all' ||
      this.adsFilter() !== 'all' ||
      this.stockFilter() !== 'all',
  );

  readonly categoryFilterLabel = computed(() =>
    this.categoryFilter() === 'all' ? 'Categoria' : this.categoryFilter(),
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

  readonly activeFilters = computed<{ key: FilterKey; label: string }[]>(() => {
    const filters: { key: FilterKey; label: string }[] = [];
    const term = this.search().trim();

    if (term) {
      filters.push({ key: 'search', label: `Busca: "${term}"` });
    }

    if (this.categoryFilter() !== 'all') {
      filters.push({ key: 'category', label: `Categoria: ${this.categoryFilter()}` });
    }

    if (this.marketplaceFilter() !== 'all') {
      const label = MARKETPLACES[this.marketplaceFilter() as Marketplace].label;
      filters.push({ key: 'marketplace', label: `Marketplace: ${label}` });
    }

    if (this.adsFilter() !== 'all') {
      filters.push({
        key: 'ads',
        label: this.adsFilter() === 'with' ? 'Com anúncios' : 'Sem vínculos',
      });
    }

    if (this.stockFilter() !== 'all') {
      filters.push({ key: 'stock', label: this.stockFilterLabel() });
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
    () => this.adsDrawerProduct() !== null || this.reviewDrawerOpen(),
  );

  private percentOf(part: number, total: number): number {
    return total > 0 ? (part / total) * 100 : 0;
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

    if (!Number.isFinite(base)) {
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
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeMenus();

    if (this.reviewDrawerOpen()) {
      this.closeReviewDrawer();
    }
  }

  chooseCategory(value: string): void {
    if (this.categoryFilter() === value) {
      this.closeMenus();
      return;
    }

    this.categoryFilter.set(value);
    this.page.set(0);
    this.clearSelection();
    this.closeMenus();
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
      this.categoryFilter.set('all');
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
    this.categoryFilter.set('all');
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
    this.clearFeedbackTimer();
    this.clearFocusTimer();
    this.previousFocus =
      this.document.activeElement instanceof HTMLElement ? this.document.activeElement : null;
    this.document.body.style.overflow = 'hidden';
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
    this.document.body.style.overflow = '';
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
    return this.currencyFormatter.format(value ?? 0);
  }

  formatNumber(value: number | null | undefined): string {
    return this.numberFormatter.format(value ?? 0);
  }

  categoryLabel(product: ProductResponse): string {
    return product.category ?? '--';
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

  onExport(): void {
    this.showToast(
      `Relatório com ${this.totalItems()} produto(s) gerado. Download iniciado.`,
      'success',
    );
  }

  onAddProduct(): void {
    this.showToast('Fluxo de cadastro de produto iniciado.', 'info');
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

  onBulkExport(): void {
    this.showToast(`${this.selectedCount()} produto(s) incluído(s) na exportação.`, 'success');
  }

  onBulkEdit(): void {
    this.showToast(`Edição em massa preparada para ${this.selectedCount()} produto(s).`, 'info');
  }

  onBulkDelete(): void {
    this.showToast(`Exclusão de ${this.selectedCount()} produto(s) solicitada.`, 'error');
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
