import {
  AfterViewInit,
  Component,
  ElementRef,
  ViewChild,
  computed,
  input,
  output,
  signal,
} from '@angular/core';
import { FormField, form } from '@angular/forms/signals';
import {
  CategoryRef,
  ProductBulkEditPayload,
  ProductStatus,
} from '../../models/productModels';

interface BulkEditModel {
  name: string;
  sku: string;
  stock: number | null;
  baseSellPrice: number | null;
  costPrice: number | null;
  imageUrl: string;
  status: ProductStatus | '';
}

@Component({
  selector: 'app-product-bulk-edit-dialog',
  imports: [FormField],
  templateUrl: './product-bulk-edit-dialog.html',
  styleUrl: './product-bulk-edit-dialog.css',
})
export class ProductBulkEditDialogComponent implements AfterViewInit {
  readonly categories = input<CategoryRef[]>([]);
  readonly categoriesLoading = input(false);
  readonly categoriesError = input<string | null>(null);
  readonly count = input(0);
  readonly loading = input(false);
  readonly error = input<string | null>(null);

  readonly submitted = output<ProductBulkEditPayload>();
  readonly cancelled = output<void>();
  readonly retryCategories = output<void>();

  readonly model = signal<BulkEditModel>({
    name: '',
    sku: '',
    stock: null,
    baseSellPrice: null,
    costPrice: null,
    imageUrl: '',
    status: '',
  });
  readonly bulkForm = form(this.model);

  readonly removeImage = signal(false);
  readonly changeCategories = signal(false);
  readonly selectedCategoryIds = signal<ReadonlySet<string>>(new Set<string>());
  readonly localError = signal<string | null>(null);

  readonly selectedCategoryCount = computed(() => this.selectedCategoryIds().size);
  readonly hasSelectedCategories = computed(() => this.selectedCategoryCount() > 0);
  readonly productCategories = computed(() =>
    this.categories().filter((category) => category.type === 'PRODUCT'),
  );
  readonly stateCategories = computed(() =>
    this.categories().filter((category) => category.type === 'STATE'),
  );
  readonly hasAnyChange = computed(() => {
    const model = this.model();
    return (
      model.name.trim() !== '' ||
      model.sku.trim() !== '' ||
      model.stock !== null ||
      model.baseSellPrice !== null ||
      model.costPrice !== null ||
      model.imageUrl.trim() !== '' ||
      this.removeImage() ||
      this.changeCategories() ||
      model.status !== ''
    );
  });

  @ViewChild('firstInput') private firstInput?: ElementRef<HTMLInputElement>;
  @ViewChild('dialog') private dialog?: ElementRef<HTMLElement>;

  ngAfterViewInit(): void {
    setTimeout(() => this.firstInput?.nativeElement.focus(), 0);
  }

  isCategorySelected(publicId: string): boolean {
    return this.selectedCategoryIds().has(publicId);
  }

  toggleCategory(publicId: string): void {
    const next = new Set(this.selectedCategoryIds());
    next.has(publicId) ? next.delete(publicId) : next.add(publicId);
    this.selectedCategoryIds.set(next);
    this.localError.set(null);
  }

  onBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget && !this.loading()) {
      this.cancelled.emit();
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();

      if (!this.loading()) {
        this.cancelled.emit();
      }

      return;
    }

    if (event.key !== 'Tab') {
      return;
    }

    const container = this.dialog?.nativeElement;

    if (!container) {
      return;
    }

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
    const active = document.activeElement;

    if (event.shiftKey && active === first) {
      event.preventDefault();
      last.focus();
    } else if (!event.shiftKey && active === last) {
      event.preventDefault();
      first.focus();
    }
  }

  apply(): void {
    const model = this.model();
    this.localError.set(null);

    if (!this.hasAnyChange()) {
      this.localError.set('Informe ao menos um campo para alterar.');
      return;
    }

    const stock = model.stock;
    const baseSellPrice = model.baseSellPrice;
    const costPrice = model.costPrice;

    if (stock !== null && (!Number.isInteger(stock) || stock < 0)) {
      this.localError.set('O estoque deve ser um inteiro maior ou igual a zero.');
      return;
    }

    if (baseSellPrice !== null && baseSellPrice <= 0) {
      this.localError.set('O preço de venda deve ser maior que zero.');
      return;
    }

    if (costPrice !== null && costPrice < 0) {
      this.localError.set('O custo não pode ser negativo.');
      return;
    }

    if (model.name.trim().length > 256) {
      this.localError.set('O nome deve ter no máximo 256 caracteres.');
      return;
    }

    if (model.sku.trim().length > 50) {
      this.localError.set('O SKU deve ter no máximo 50 caracteres.');
      return;
    }

    const payload: ProductBulkEditPayload = {
      ...(model.name.trim() ? { name: model.name.trim() } : {}),
      ...(model.sku.trim() ? { sku: model.sku.trim() } : {}),
      ...(stock !== null ? { stock } : {}),
      ...(baseSellPrice !== null ? { baseSellPrice } : {}),
      ...(costPrice !== null ? { costPrice } : {}),
      ...(this.removeImage()
        ? { imageUrl: '' }
        : model.imageUrl.trim()
          ? { imageUrl: model.imageUrl.trim() }
          : {}),
      ...(this.changeCategories()
        ? { categoryIds: [...this.selectedCategoryIds()] }
        : {}),
      ...(model.status ? { status: model.status } : {}),
    };

    this.submitted.emit(payload);
  }
}
