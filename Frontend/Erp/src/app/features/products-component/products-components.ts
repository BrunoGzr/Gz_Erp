import { DOCUMENT } from '@angular/common';
import {
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';

type ViewMode = 'table' | 'grid';
type MarketplaceFilter = 'all' | 'shopee' | 'mercado-livre';
type AdsFilter = 'all' | 'with' | 'without';
type StockFilter = 'all' | 'low' | 'out';
type FilterKey = 'search' | 'category' | 'marketplace' | 'ads' | 'stock';
type PriceMode = 'percent' | 'fixed';
type StockMode = 'set' | 'adjust';
type DeactivateStep = 0 | 1 | 2 | 3;

interface ProductStat {
  label: string;
  value: string;
  trend: string;
  direction: 'up' | 'down';
  tone: 'good' | 'bad';
  note: string;
  progress: number;
  toneClass: string;
  points: string;
}

interface MarketplaceAd {
  key: 'shopee' | 'mercado-livre';
  name: string;
  ads: number;
}

interface ProductRow {
  id: number;
  name: string;
  sku: string;
  category: string;
  price: string;
  cost: string;
  stock: number;
  sales: string;
  updated: string;
  tone: string;
  marketplaces: MarketplaceAd[];
}

@Component({
  imports: [],
  selector: 'app-products-components',
  styleUrls: [
    './products-components.css',
    './products-components-grid.css',
    './products-components-drawer.css',
  ],
  templateUrl: './products-components.html',
})
export class ProductsComponents implements OnDestroy {
  private readonly document = inject(DOCUMENT);

  @ViewChild('drawerClose') private drawerClose?: ElementRef<HTMLButtonElement>;

  readonly viewMode = signal<ViewMode>('table');
  readonly categoryFilter = signal('all');
  readonly marketplaceFilter = signal<MarketplaceFilter>('all');
  readonly adsFilter = signal<AdsFilter>('all');
  readonly stockFilter = signal<StockFilter>('all');
  readonly search = signal('');
  readonly selectedIds = signal<ReadonlySet<number>>(new Set<number>());
  readonly adsDrawerProduct = signal<ProductRow | null>(null);
  readonly priceMode = signal<PriceMode>('percent');
  readonly priceValue = signal<number | null>(null);
  readonly stockMode = signal<StockMode>('set');
  readonly stockValue = signal<number | null>(null);
  readonly deactivateStep = signal<DeactivateStep>(0);
  readonly applyFeedback = signal<string | null>(null);
  readonly applyFeedbackTone = signal<'success' | 'error'>('success');
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;
  private focusTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly scrollTimers = new Map<HTMLElement, ReturnType<typeof setTimeout>>();
  private previousFocus: HTMLElement | null = null;

  readonly categories = ['Camisa', 'Jaqueta', 'Calça', 'Camiseta'];

  readonly marketplaceOptions: { key: MarketplaceFilter; label: string }[] = [
    { key: 'shopee', label: 'Shopee' },
    { key: 'mercado-livre', label: 'Mercado Livre' },
  ];

  readonly adsOptions: { key: AdsFilter; label: string }[] = [
    { key: 'with', label: 'Com anúncios' },
    { key: 'without', label: 'Sem vínculos' },
  ];

  readonly stockOptions: { key: StockFilter; label: string }[] = [
    { key: 'low', label: 'Estoque baixo' },
    { key: 'out', label: 'Sem estoque' },
  ];

  readonly stats: ProductStat[] = [
    {
      label: 'Total de produtos',
      value: '1.248',
      trend: '+4,2%',
      direction: 'up',
      tone: 'good',
      note: 'Todos os itens cadastrados',
      progress: 100,
      toneClass: 'blue',
      points: '0,20 12,14 24,16 36,9 48,12 60,7 72,9 84,4 96,5',
    },
    {
      label: 'Receita total',
      value: 'R$ 84.320',
      trend: '+12,5%',
      direction: 'up',
      tone: 'good',
      note: 'Receita acumulada no período',
      progress: 72,
      toneClass: 'green',
      points: '0,18 12,17 24,12 36,14 48,8 60,10 72,5 84,7 96,2',
    },
    {
      label: 'Estoque baixo',
      value: '12',
      trend: '-2',
      direction: 'down',
      tone: 'good',
      note: 'itens abaixo do mínimo',
      progress: 18,
      toneClass: 'amber',
      points: '0,8 12,12 24,7 36,14 48,10 60,16 72,9 84,13 96,11',
    },
    {
      label: 'Sem estoque',
      value: '8',
      trend: '+3',
      direction: 'up',
      tone: 'bad',
      note: 'itens indisponíveis',
      progress: 9,
      toneClass: 'red',
      points: '0,15 12,15 24,10 36,13 48,7 60,9 72,4 84,6 96,3',
    },
  ];

  readonly products: ProductRow[] = [
    {
      id: 1,
      name: 'Arunda Jackiss Shirt',
      sku: 'CAM-001',
      category: 'Camisa',
      price: 'R$ 50,99',
      cost: 'R$ 32,00',
      stock: 58,
      sales: '882',
      updated: '12/10/2026',
      tone: 'tone-1',
      marketplaces: [
        { key: 'shopee', name: 'Shopee', ads: 4 },
        { key: 'mercado-livre', name: 'Mercado Livre', ads: 2 },
      ],
    },
    {
      id: 2,
      name: 'Livora Bohemian Jacket',
      sku: 'JAQ-014',
      category: 'Jaqueta',
      price: 'R$ 67,99',
      cost: 'R$ 41,00',
      stock: 88,
      sales: '72',
      updated: '09/10/2026',
      tone: 'tone-2',
      marketplaces: [{ key: 'mercado-livre', name: 'Mercado Livre', ads: 3 }],
    },
    {
      id: 3,
      name: 'Spookie Stunt Pant',
      sku: 'CAL-023',
      category: 'Calça',
      price: 'R$ 40,99',
      cost: 'R$ 25,00',
      stock: 12,
      sales: '119',
      updated: '08/10/2026',
      tone: 'tone-3',
      marketplaces: [],
    },
    {
      id: 4,
      name: 'White Skorisi T-Shirt',
      sku: 'CAM-008',
      category: 'Camiseta',
      price: 'R$ 30,99',
      cost: 'R$ 18,00',
      stock: 0,
      sales: '50',
      updated: '07/10/2026',
      tone: 'tone-1',
      marketplaces: [{ key: 'mercado-livre', name: 'Mercado Livre', ads: 2 }],
    },
    {
      id: 5,
      name: 'Torema Imporio Jacket',
      sku: 'JAQ-009',
      category: 'Jaqueta',
      price: 'R$ 55,99',
      cost: 'R$ 34,00',
      stock: 10,
      sales: '44',
      updated: '06/10/2026',
      tone: 'tone-2',
      marketplaces: [
        { key: 'shopee', name: 'Shopee', ads: 3 },
        { key: 'mercado-livre', name: 'Mercado Livre', ads: 4 },
      ],
    },
    {
      id: 6,
      name: 'Lorenza Pant',
      sku: 'CAL-011',
      category: 'Calça',
      price: 'R$ 34,99',
      cost: 'R$ 21,00',
      stock: 22,
      sales: '32',
      updated: '05/10/2026',
      tone: 'tone-1',
      marketplaces: [{ key: 'mercado-livre', name: 'Mercado Livre', ads: 2 }],
    },
    {
      id: 7,
      name: 'Yorikozu Biama Pant',
      sku: 'CAL-017',
      category: 'Calça',
      price: 'R$ 32,99',
      cost: 'R$ 19,00',
      stock: 45,
      sales: '78',
      updated: '04/10/2026',
      tone: 'tone-2',
      marketplaces: [
        { key: 'shopee', name: 'Shopee', ads: 1 },
        { key: 'mercado-livre', name: 'Mercado Livre', ads: 6 },
      ],
    },
    {
      id: 8,
      name: 'Uriba Nanalu Jacket',
      sku: 'JAQ-021',
      category: 'Jaqueta',
      price: 'R$ 55,99',
      cost: 'R$ 34,00',
      stock: 22,
      sales: '10',
      updated: '03/10/2026',
      tone: 'tone-3',
      marketplaces: [
        { key: 'shopee', name: 'Shopee', ads: 3 },
        { key: 'mercado-livre', name: 'Mercado Livre', ads: 3 },
      ],
    },
  ];

  totalAds(product: ProductRow): number {
    return product.marketplaces.reduce((total, marketplace) => total + marketplace.ads, 0);
  }

  readonly filteredProducts = computed(() => {
    const term = this.search().trim().toLowerCase();
    const category = this.categoryFilter();
    const marketplace = this.marketplaceFilter();
    const ads = this.adsFilter();
    const stock = this.stockFilter();

    return this.products.filter((product) => {
      const searchable = `${product.name} ${product.sku} ${product.category} ${product.marketplaces
        .map((item) => item.name)
        .join(' ')}`.toLowerCase();
      const matchesSearch = !term || searchable.includes(term);
      const matchesCategory = category === 'all' || product.category === category;
      const matchesMarketplace =
        marketplace === 'all' || product.marketplaces.some((item) => item.key === marketplace);
      const matchesAds =
        ads === 'all' ||
        (ads === 'with' ? this.totalAds(product) > 0 : product.marketplaces.length === 0);
      const matchesStock =
        stock === 'all' ||
        (stock === 'low' ? product.stock > 0 && product.stock <= 20 : product.stock === 0);

      return matchesSearch && matchesCategory && matchesMarketplace && matchesAds && matchesStock;
    });
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
      const marketplace =
        this.marketplaceFilter() === 'shopee' ? 'Shopee' : 'Mercado Livre';
      filters.push({ key: 'marketplace', label: `Marketplace: ${marketplace}` });
    }

    if (this.adsFilter() !== 'all') {
      filters.push({
        key: 'ads',
        label: this.adsFilter() === 'with' ? 'Com anúncios' : 'Sem vínculos',
      });
    }

    if (this.stockFilter() !== 'all') {
      filters.push({
        key: 'stock',
        label: this.stockFilter() === 'low' ? 'Estoque baixo' : 'Sem estoque',
      });
    }

    return filters;
  });

  readonly selectedCount = computed(() => this.selectedIds().size);

  readonly allSelected = computed(() => {
    const list = this.filteredProducts();
    return list.length > 0 && list.every((product) => this.selectedIds().has(product.id));
  });

  readonly someSelected = computed(() => this.selectedIds().size > 0 && !this.allSelected());

  readonly pricePreview = computed(() => {
    const product = this.adsDrawerProduct();
    const value = this.priceValue();

    if (!product || value === null || !Number.isFinite(value)) {
      return null;
    }

    const base = Number(product.price.replace(/[^\d,]/g, '').replace(',', '.'));

    if (!Number.isFinite(base)) {
      return null;
    }

    const next = this.priceMode() === 'percent' ? base * (1 + value / 100) : value;

    return next.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
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
    this.clearSelection();
  }

  onCategoryChange(event: Event): void {
    this.categoryFilter.set((event.target as HTMLSelectElement).value);
    this.clearSelection();
  }

  onMarketplaceChange(event: Event): void {
    this.marketplaceFilter.set((event.target as HTMLSelectElement).value as MarketplaceFilter);
    this.clearSelection();
  }

  onAdsChange(event: Event): void {
    this.adsFilter.set((event.target as HTMLSelectElement).value as AdsFilter);
    this.clearSelection();
  }

  onStockChange(event: Event): void {
    this.stockFilter.set((event.target as HTMLSelectElement).value as StockFilter);
    this.clearSelection();
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

    this.clearSelection();
  }

  clearFilters(): void {
    this.search.set('');
    this.categoryFilter.set('all');
    this.marketplaceFilter.set('all');
    this.adsFilter.set('all');
    this.stockFilter.set('all');
    this.clearSelection();
  }

  openAdsDrawer(product: ProductRow): void {
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
      `Alterações aplicadas com sucesso em ${this.totalAds(product)} anúncios de ${product.name}.`,
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
      `Produto desativado com sucesso em ${this.totalAds(product)} anúncios de ${product.name}.`,
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
    this.clearScrollTimers();
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

  toggleSelection(id: number): void {
    const next = new Set(this.selectedIds());
    next.has(id) ? next.delete(id) : next.add(id);
    this.selectedIds.set(next);
  }

  toggleAll(): void {
    this.selectedIds.set(
      this.allSelected()
        ? new Set<number>()
        : new Set(this.filteredProducts().map((product) => product.id)),
    );
  }

  isSelected(id: number): boolean {
    return this.selectedIds().has(id);
  }

  clearSelection(): void {
    this.selectedIds.set(new Set<number>());
  }
}
