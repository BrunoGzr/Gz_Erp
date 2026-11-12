import { DOCUMENT } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, of } from 'rxjs';
import {
  CategoryRef,
  CategoryType,
  ProductResponse,
  Marketplace,
  ProductStatus,
} from '../../models/productModels';
import { ProductService } from '../../service/product-service';
import {
  formatCurrency,
  formatDate,
  formatNumber,
  marketplaceLabel,
  relativeUpdated,
  statusLabel,
} from '../../shared/format/product-format';
import { ConfirmDialogComponent } from '../../shared/ui/confirm-dialog/confirm-dialog';
import { ProductStatusBadgeComponent } from '../../shared/ui/product-status-badge/product-status-badge';
import { ProductListingsMockComponent } from './product-listings-mock-component';

interface FeedbackState {
  message: string;
  tone: 'success' | 'info' | 'error';
}

@Component({
  selector: 'app-product-detail-component',
  imports: [
    RouterLink,
    ProductStatusBadgeComponent,
    ProductListingsMockComponent,
    ConfirmDialogComponent,
  ],
  templateUrl: './product-detail-component.html',
  styleUrl: './product-detail-component.css',
})
export class ProductDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly document = inject(DOCUMENT);

  readonly formatCurrency = formatCurrency;
  readonly formatNumber = formatNumber;
  readonly formatDate = formatDate;
  readonly relativeUpdated = relativeUpdated;
  readonly statusLabel = statusLabel;
  readonly marketplaceLabel = marketplaceLabel;

  readonly statuses: { value: ProductStatus; label: string }[] = [
    { value: 'ACTIVE', label: 'Ativar' },
    { value: 'PAUSED', label: 'Pausar' },
    { value: 'INACTIVE', label: 'Inativar' },
  ];

  readonly product = signal<ProductResponse | null>(null);
  readonly loading = signal(true);
  readonly notFound = signal(false);
  readonly error = signal<string | null>(null);
  readonly feedback = signal<FeedbackState | null>(null);

  readonly deleteOpen = signal(false);
  readonly deleting = signal(false);
  readonly deleteError = signal<string | null>(null);
  readonly statusSaving = signal<ProductStatus | null>(null);
  readonly statusBusy = computed(() => this.statusSaving() !== null);
  readonly imageFailed = signal(false);

  readonly hasImage = computed(() => {
    const product = this.product();
    return Boolean(product?.imageUrl) && !this.imageFailed();
  });

  marketplaceEntries(product: ProductResponse): { key: Marketplace; label: string; count: number }[] {
    return (Object.keys(product.listingCount) as Marketplace[])
      .map((key) => ({
        key,
        label: marketplaceLabel(key),
        count: product.listingCount[key] ?? 0,
      }))
      .filter((entry) => entry.count > 0);
  }

  listingTotal(product: ProductResponse): number {
    return this.marketplaceEntries(product).reduce((total, entry) => total + entry.count, 0);
  }

  hasMarketplaces(product: ProductResponse): boolean {
    return this.marketplaceEntries(product).length > 0;
  }

  hasCategories(product: ProductResponse): boolean {
    return product.categories.length > 0;
  }

  categoriesByType(product: ProductResponse, type: CategoryType): CategoryRef[] {
    return product.categories.filter((category) => category.type === type);
  }

  hasCategoriesOfType(product: ProductResponse, type: CategoryType): boolean {
    return this.categoriesByType(product, type).length > 0;
  }

  isCurrentStatus(product: ProductResponse, status: ProductStatus): boolean {
    return product.status === status;
  }

  isSavingStatus(status: ProductStatus): boolean {
    return this.statusSaving() === status;
  }

  private readonly publicId = this.route.snapshot.paramMap.get('publicId') ?? '';
  private readonly returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

  get currentUrl(): string {
    return this.router.url;
  }

  ngOnInit(): void {
    this.readNavigationFeedback();
    this.loadProduct();
  }

  goBack(): void {
    const target =
      this.returnUrl && this.returnUrl.startsWith('/produtos') ? this.returnUrl : '/produtos';
    void this.router.navigateByUrl(target);
  }

  loadProduct(): void {
    this.loading.set(true);
    this.notFound.set(false);
    this.error.set(null);

    if (!this.publicId) {
      this.loading.set(false);
      this.notFound.set(true);
      return;
    }

    this.productService
      .getProduct(this.publicId)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.notFound.set(true);
          } else {
            this.error.set(this.apiErrorMessage(error, 'Não foi possível carregar o produto.'));
          }

          return of(null);
        }),
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((product) => {
        if (product) {
          this.product.set(product);
        }
      });
  }

  retry(): void {
    this.loadProduct();
  }

  onListingAction(message: string): void {
    this.feedback.set({ message, tone: 'info' });
  }

  setStatus(status: ProductStatus): void {
    const product = this.product();

    if (!product || product.status === status || this.statusSaving()) {
      return;
    }

    this.statusSaving.set(status);

    this.productService
      .updateProductsStatus([{ publicId: product.publicId, status }])
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.feedback.set({
            message: this.apiErrorMessage(error, 'Não foi possível alterar o status do produto.'),
            tone: 'error',
          });
          return of(false);
        }),
        finalize(() => this.statusSaving.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (result === false) {
          return;
        }

        this.product.set({ ...product, status });
        this.feedback.set({
          message: `Status atualizado para ${statusLabel(status)}.`,
          tone: 'success',
        });
      });
  }

  openDelete(): void {
    this.deleteError.set(null);
    this.deleteOpen.set(true);
  }

  closeDelete(): void {
    if (!this.deleting()) {
      this.deleteOpen.set(false);
      this.deleteError.set(null);
    }
  }

  confirmDelete(): void {
    const product = this.product();

    if (!product || this.deleting()) {
      return;
    }

    this.deleting.set(true);
    this.deleteError.set(null);

    this.productService
      .deleteProduct(product.publicId)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.navigateAfterDelete('Produto já havia sido removido.');
            return of(false);
          }

          this.deleteError.set(
            this.apiErrorMessage(error, 'Não foi possível excluir o produto.'),
          );
          return of(false);
        }),
        finalize(() => this.deleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (result === false) {
          return;
        }

        if (!this.deleteError()) {
          this.navigateAfterDelete('Produto excluído com sucesso.');
        }
      });
  }

  private navigateAfterDelete(message: string): void {
    this.deleteOpen.set(false);
    this.feedback.set({ message, tone: 'success' });

    const target =
      this.returnUrl && this.returnUrl.startsWith('/produtos') ? this.returnUrl : '/produtos';
    void this.router.navigateByUrl(target, {
      state: { productFeedback: message, productFeedbackTone: 'success' },
    });
  }

  private readNavigationFeedback(): void {
    const history = this.document.defaultView?.history;
    const state = history?.state as
      | { productFeedback?: string; productFeedbackTone?: 'success' | 'info' | 'error' }
      | null;

    if (state?.productFeedback) {
      this.feedback.set({
        message: state.productFeedback,
        tone: state.productFeedbackTone ?? 'success',
      });
      history?.replaceState({ ...state, productFeedback: undefined, productFeedbackTone: undefined }, '');
    }
  }

  private apiErrorMessage(error: HttpErrorResponse, fallback: string): string {
    const body = error.error as { errors?: string[]; detail?: string } | null;

    if (body?.errors?.[0]) {
      return body.errors[0];
    }

    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
    }

    return body?.detail ?? fallback;
  }
}
