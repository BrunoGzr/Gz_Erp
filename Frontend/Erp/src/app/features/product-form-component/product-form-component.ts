import { HttpErrorResponse } from '@angular/common/http';
import {
  Component,
  DestroyRef,
  ElementRef,
  OnDestroy,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form, maxLength, min, required, validate } from '@angular/forms/signals';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import {
  CategoryRef,
  CategoryType,
  ProductRegisterRequest,
  ProductResponse,
  ProductStatus,
  ProductUpdateRequest,
} from '../../models/productModels';
import { ProductService } from '../../service/product-service';
import { formatCurrency, normalizeForComparison } from '../../shared/format/product-format';
import { ConfirmDialogComponent } from '../../shared/ui/confirm-dialog/confirm-dialog';
import { ProductStatusBadgeComponent } from '../../shared/ui/product-status-badge/product-status-badge';

interface ProductFormModel {
  name: string;
  sku: string;
  stock: number | null;
  baseSellPrice: number | null;
  costPrice: number | null;
}

type FormMode = 'create' | 'edit';
type ImageMode = 'existing' | 'new' | 'removed';

function emptyModel(): ProductFormModel {
  return {
    name: '',
    sku: '',
    stock: 0,
    baseSellPrice: null,
    costPrice: 0,
  };
}

@Component({
  selector: 'app-product-form-component',
  imports: [FormField, ConfirmDialogComponent, ProductStatusBadgeComponent],
  templateUrl: './product-form-component.html',
  styleUrls: [
    './product-form-component.css',
    './product-form-component-preview.css',
    './product-form-component-states.css',
  ],
})
export class ProductFormComponent implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly destroyRef = inject(DestroyRef);

  private readonly publicId = this.route.snapshot.paramMap.get('publicId');
  private readonly duplicateFromId = this.route.snapshot.queryParamMap.get('duplicarDe');
  private readonly returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

  readonly mode = signal<FormMode>(this.publicId ? 'edit' : 'create');
  readonly formModel = signal<ProductFormModel>(emptyModel());
  readonly sourceProduct = signal<ProductResponse | null>(null);
  readonly duplicateSource = signal<ProductResponse | null>(null);

  readonly productForm = form(this.formModel, (path) => {
    required(path.name, { message: 'Informe o nome do produto.' });
    maxLength(path.name, 256, { message: 'O nome deve ter no máximo 256 caracteres.' });
    maxLength(path.sku, 50, { message: 'O SKU deve ter no máximo 50 caracteres.' });

    required(path.stock, { message: 'Informe o estoque.' });
    min(path.stock, 0, { message: 'O estoque não pode ser negativo.' });

    required(path.baseSellPrice, { message: 'Informe o preço de venda.' });
    min(path.baseSellPrice, 0.01, { message: 'O preço de venda deve ser maior que zero.' });

    required(path.costPrice, { message: 'Informe o custo.' });
    min(path.costPrice, 0, { message: 'O custo não pode ser negativo.' });

    validate(path.name, ({ value }) => {
      const source = this.duplicateSource();

      if (!source) {
        return null;
      }

      return normalizeForComparison(value()) === normalizeForComparison(source.name)
        ? {
            kind: 'duplicate-name',
            message: 'O nome precisa ser diferente do produto original.',
          }
        : null;
    });

    validate(path.sku, ({ value }) => {
      const source = this.duplicateSource();
      const candidate = value().trim();

      if (!source?.sku || !candidate) {
        return null;
      }

      return normalizeForComparison(candidate) === normalizeForComparison(source.sku)
        ? {
            kind: 'duplicate-sku',
            message: 'Informe um SKU diferente ou deixe vazio para gerar automaticamente.',
          }
        : null;
    });
  });

  readonly activeCategories = signal<CategoryRef[]>([]);
  readonly categoriesLoading = signal(false);
  readonly categoriesError = signal<string | null>(null);
  readonly selectedCategoryIds = signal<ReadonlySet<string>>(new Set<string>());
  readonly categoriesTouched = signal(false);

  @ViewChild('imageInput') private imageInput?: ElementRef<HTMLInputElement>;

  readonly existingImageUrl = signal<string | null>(null);
  readonly imageMode = signal<ImageMode>('existing');
  readonly selectedImageFile = signal<File | null>(null);
  readonly imagePreviewUrl = signal<string | null>(null);
  readonly imagePreviewFailed = signal(false);
  readonly imageDragOver = signal(false);
  readonly imageError = signal<string | null>(null);
  private imageObjectUrl: string | null = null;

  readonly formatCurrency = formatCurrency;

  readonly loading = signal(true);
  readonly notFound = signal(false);
  readonly submitting = signal(false);
  readonly formError = signal<string | null>(null);
  readonly nameServerError = signal<string | null>(null);
  readonly skuServerError = signal<string | null>(null);
  readonly categoryServerError = signal<string | null>(null);
  readonly discardOpen = signal(false);

  readonly isEdit = computed(() => this.mode() === 'edit');
  readonly isDuplicate = computed(() => this.duplicateFromId !== null);
  readonly pageTitle = computed(() => {
    if (this.isEdit()) {
      return 'Editar produto';
    }

    return this.isDuplicate() ? 'Duplicar produto' : 'Novo produto';
  });
  readonly pageDescription = computed(() => {
    if (this.isEdit()) {
      return 'Atualize os dados do produto. Campos não alterados são preservados.';
    }

    return this.isDuplicate()
      ? 'Revise os dados copiados. Nome e SKU precisam ser únicos.'
      : 'Cadastre um novo produto no catálogo.';
  });
  readonly submitLabel = computed(() => {
    if (this.isEdit()) {
      return 'Salvar alterações';
    }

    return this.isDuplicate() ? 'Criar cópia' : 'Criar produto';
  });
  readonly previewStatus = computed<ProductStatus | null>(() =>
    this.isEdit() ? (this.sourceProduct()?.status ?? 'ACTIVE') : 'ACTIVE',
  );
  readonly previewName = computed(
    () => this.formModel().name.trim() || 'Nome do produto',
  );
  readonly previewSku = computed(
    () => this.formModel().sku.trim() || 'SKU automático',
  );
  readonly previewStock = computed(() => this.formModel().stock ?? 0);
  readonly previewPrice = computed(() => this.formModel().baseSellPrice);
  readonly selectedCategoryRefs = computed(() => {
    const categoriesById = new Map<string, CategoryRef>();

    for (const category of this.activeCategories()) {
      categoriesById.set(category.publicId, category);
    }

    for (const category of this.sourceProduct()?.categories ?? []) {
      categoriesById.set(category.publicId, category);
    }

    return [...this.selectedCategoryIds()]
      .map((publicId) => categoriesById.get(publicId))
      .filter((category): category is CategoryRef => Boolean(category));
  });
  readonly activeProductCategories = computed(() =>
    this.activeCategories().filter((category) => category.type === 'PRODUCT'),
  );
  readonly activeStateCategories = computed(() =>
    this.activeCategories().filter((category) => category.type === 'STATE'),
  );
  readonly inactiveProductCategories = computed(() =>
    this.inactiveCategories().filter((category) => category.type === 'PRODUCT'),
  );
  readonly inactiveStateCategories = computed(() =>
    this.inactiveCategories().filter((category) => category.type === 'STATE'),
  );
  readonly selectedProductCategoryCount = computed(() => {
    const ids = new Set(this.activeProductCategories().map((category) => category.publicId));
    return this.selectedActiveCategoryIds().filter((id) => ids.has(id)).length;
  });
  readonly selectedStateCategoryCount = computed(() => {
    const ids = new Set(this.activeStateCategories().map((category) => category.publicId));
    return this.selectedActiveCategoryIds().filter((id) => ids.has(id)).length;
  });
  readonly hasPreviewImage = computed(
    () => Boolean(this.imagePreviewUrl()) && !this.imagePreviewFailed(),
  );
  readonly imageNotice = computed(() => {
    const mode = this.imageMode();
    const existing = this.existingImageUrl();
    const file = this.selectedImageFile();

    if (mode === 'new') {
      const prefix = file ? `Preview local de “${file.name}”. ` : 'Preview local. ';
      return existing
        ? `${prefix}A imagem atual será mantida ao salvar (upload mock).`
        : `${prefix}O produto será salvo sem imagem (upload mock).`;
    }

    if (mode === 'removed') {
      return existing
        ? 'A imagem atual será removida ao salvar.'
        : 'Nenhuma imagem será enviada.';
    }

    return existing ? 'Imagem atual do produto.' : 'Nenhuma imagem selecionada.';
  });
  readonly canRemoveImage = computed(() => Boolean(this.imagePreviewUrl()));
  readonly canRestoreImage = computed(
    () => this.imageMode() !== 'existing' && Boolean(this.existingImageUrl()),
  );
  readonly showMockBadge = computed(
    () => this.imageMode() === 'new' || !this.existingImageUrl(),
  );
  readonly hasSelectedCategories = computed(() => this.selectedCategoryRefs().length > 0);
  readonly canResetCategories = computed(
    () => this.selectedCategoryCount() > 0 || this.categoriesTouched(),
  );
  readonly selectedCategoryCount = computed(() => this.selectedActiveCategoryIds().length);
  readonly selectedActiveCategoryIds = computed(() => {
    const activeIds = new Set(this.activeCategories().map((category) => category.publicId));
    return [...this.selectedCategoryIds()].filter((id) => activeIds.has(id));
  });
  readonly inactiveCategories = computed(() => {
    if (!this.isEdit() || this.categoriesLoading() || this.categoriesError()) {
      return [];
    }

    const activeIds = new Set(this.activeCategories().map((category) => category.publicId));
    return (this.sourceProduct()?.categories ?? []).filter(
      (category) => !activeIds.has(category.publicId),
    );
  });
  readonly hasChanges = computed(
    () =>
      this.productForm().dirty() ||
      this.categoriesTouched() ||
      this.imageMode() !== 'existing',
  );
  readonly cancelTarget = computed(() => {
    if (this.returnUrl && this.returnUrl.startsWith('/produtos')) {
      return this.returnUrl;
    }

    return this.publicId ? `/produtos/${this.publicId}` : '/produtos';
  });

  ngOnInit(): void {
    this.initialize();
  }

  isCategorySelected(publicId: string): boolean {
    return this.selectedCategoryIds().has(publicId);
  }

  toggleCategory(publicId: string): void {
    const next = new Set(this.selectedCategoryIds());
    next.has(publicId) ? next.delete(publicId) : next.add(publicId);
    this.selectedCategoryIds.set(next);
    this.categoriesTouched.set(true);
    this.categoryServerError.set(null);
  }

  clearCategorySelection(): void {
    this.selectedCategoryIds.set(new Set<string>());
    this.categoriesTouched.set(true);
    this.categoryServerError.set(null);
  }

  retryCategories(): void {
    this.loadCategories();
  }

  triggerImageInput(): void {
    this.imageInput?.nativeElement.click();
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';

    if (file) {
      this.applyImageFile(file);
    }
  }

  onImageDrop(event: DragEvent): void {
    event.preventDefault();
    this.imageDragOver.set(false);

    const file = event.dataTransfer?.files?.[0];

    if (file) {
      this.applyImageFile(file);
    }
  }

  onImageDragOver(event: DragEvent): void {
    event.preventDefault();
    this.imageDragOver.set(true);
  }

  onImageDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.imageDragOver.set(false);
  }

  removeImage(): void {
    this.revokeImageObjectUrl();
    this.selectedImageFile.set(null);
    this.imagePreviewUrl.set(null);
    this.imagePreviewFailed.set(false);
    this.imageMode.set('removed');
    this.imageError.set(null);
  }

  restoreExistingImage(): void {
    this.revokeImageObjectUrl();
    this.selectedImageFile.set(null);
    this.imagePreviewUrl.set(this.existingImageUrl());
    this.imagePreviewFailed.set(false);
    this.imageMode.set('existing');
    this.imageError.set(null);
  }

  onPreviewError(): void {
    this.imagePreviewFailed.set(true);
    this.imageError.set(
      this.imageMode() === 'existing'
        ? 'Não foi possível carregar a imagem atual. Selecione outra.'
        : 'Não foi possível pré-visualizar a imagem selecionada.',
    );
  }

  ngOnDestroy(): void {
    this.revokeImageObjectUrl();
  }

  private applyImageFile(file: File): void {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];

    if (!allowedTypes.includes(file.type)) {
      this.imageError.set('Use uma imagem JPG, PNG ou WEBP.');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.imageError.set('A imagem deve ter no máximo 5 MB.');
      return;
    }

    this.revokeImageObjectUrl();
    const objectUrl = URL.createObjectURL(file);
    this.imageObjectUrl = objectUrl;
    this.selectedImageFile.set(file);
    this.imagePreviewUrl.set(objectUrl);
    this.imagePreviewFailed.set(false);
    this.imageMode.set('new');
    this.imageError.set(null);
  }

  private revokeImageObjectUrl(): void {
    if (this.imageObjectUrl) {
      URL.revokeObjectURL(this.imageObjectUrl);
      this.imageObjectUrl = null;
    }
  }

  private resolveImageUrlForSave(): string | null {
    if (this.imageMode() === 'removed') {
      return this.isEdit() ? '' : null;
    }

    // Mock de upload: o arquivo selecionado é usado apenas no preview local.
    // Quando o endpoint real existir, enviar selectedImageFile() aqui e usar
    // a URL retornada no lugar de existingImageUrl().
    return this.existingImageUrl();
  }

  onCancel(): void {
    if (this.submitting()) {
      return;
    }

    if (this.hasChanges()) {
      this.discardOpen.set(true);
      return;
    }

    this.leave();
  }

  confirmDiscard(): void {
    this.discardOpen.set(false);
    this.leave();
  }

  goBack(): void {
    this.leave();
  }

  onSubmit(event: Event): void {
    event.preventDefault();

    if (this.submitting()) {
      return;
    }

    this.formError.set(null);
    this.nameServerError.set(null);
    this.skuServerError.set(null);
    this.categoryServerError.set(null);
    this.productForm().markAsTouched();

    if (this.productForm().invalid()) {
      return;
    }

    const model = this.formModel();
    const stock = model.stock;
    const baseSellPrice = model.baseSellPrice;
    const costPrice = model.costPrice;

    if (stock === null || baseSellPrice === null || costPrice === null) {
      this.formError.set('Preencha os campos numéricos obrigatórios.');
      return;
    }

    this.submitting.set(true);

    if (this.isEdit()) {
      this.saveEdit({
        publicId: this.publicId ?? '',
        name: model.name.trim(),
        sku: model.sku.trim() || undefined,
        stock,
        baseSellPrice,
        costPrice,
        imageUrl: this.resolveImageUrlForSave(),
        categoryIds: this.categoriesTouched() ? [...this.selectedActiveCategoryIds()] : null,
      });
      return;
    }

    const payload: ProductRegisterRequest = {
      name: model.name.trim(),
      sku: model.sku.trim() || undefined,
      stock,
      baseSellPrice,
      costPrice,
      imageUrl: this.resolveImageUrlForSave(),
      categoryIds: [...this.selectedActiveCategoryIds()],
    };

    this.productService
      .registerProduct(payload)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.handleSaveError(error, 'Não foi possível criar o produto.');
          return of(null);
        }),
        finalize(() => this.submitting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((product) => {
        if (product) {
          this.navigateToProduct(
            product.publicId,
            'Produto criado com sucesso.' + this.imageMockNote(),
          );
        }
      });
  }

  private initialize(): void {
    this.loading.set(true);
    this.categoriesLoading.set(true);

    const product$ = this.publicId || this.duplicateFromId
      ? this.productService.getProduct((this.publicId ?? this.duplicateFromId) as string).pipe(
          catchError((error: HttpErrorResponse) => {
            if (error.status === 404) {
              this.notFound.set(true);
            } else {
              this.formError.set(
                this.apiErrorMessage(error, 'Não foi possível carregar o produto.'),
              );
            }

            return of(null);
          }),
        )
      : of(null);

    const categories$ = this.productService.getCategories().pipe(
      catchError((error: HttpErrorResponse) => {
        this.categoriesError.set(
          this.apiErrorMessage(error, 'Não foi possível carregar as categorias.'),
        );
        return of([]);
      }),
      finalize(() => this.categoriesLoading.set(false)),
    );

    forkJoin({ product: product$, categories: categories$ })
      .pipe(
        finalize(() => this.loading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ product, categories }) => {
        this.activeCategories.set(
          [...categories].sort((a, b) => a.name.localeCompare(b.name, 'pt-BR')),
        );

        if (this.publicId && !product) {
          return;
        }

        if (this.duplicateFromId && !product) {
          this.formError.set(
            'O produto original não foi encontrado. O formulário foi aberto vazio.',
          );
          this.categoriesTouched.set(true);
          return;
        }

        if (this.publicId && product) {
          this.fillFromProduct(product);
          return;
        }

        if (this.duplicateFromId && product) {
          this.fillFromDuplicate(product);
          return;
        }

        this.categoriesTouched.set(true);
      });
  }

  private fillFromProduct(product: ProductResponse): void {
    this.sourceProduct.set(product);
    this.formModel.set({
      name: product.name,
      sku: product.sku ?? '',
      stock: product.stock,
      baseSellPrice: product.baseSellPrice,
      costPrice: product.costPrice,
    });
    this.existingImageUrl.set(product.imageUrl);
    this.imagePreviewUrl.set(product.imageUrl);
    this.imagePreviewFailed.set(false);
    this.imageMode.set('existing');
    this.selectedCategoryIds.set(
      new Set(product.categories.map((category) => category.publicId)),
    );
    this.categoriesTouched.set(false);
  }

  private fillFromDuplicate(product: ProductResponse): void {
    this.sourceProduct.set(product);
    this.duplicateSource.set(product);

    const activeIds = new Set(this.activeCategories().map((category) => category.publicId));
    const copiedCategoryIds = product.categories
      .map((category) => category.publicId)
      .filter((id) => activeIds.has(id));

    this.formModel.set({
      name: `Cópia - ${product.name}`,
      sku: '',
      stock: product.stock,
      baseSellPrice: product.baseSellPrice,
      costPrice: product.costPrice,
    });
    this.existingImageUrl.set(product.imageUrl);
    this.imagePreviewUrl.set(product.imageUrl);
    this.imagePreviewFailed.set(false);
    this.imageMode.set('existing');
    this.selectedCategoryIds.set(new Set(copiedCategoryIds));
    this.categoriesTouched.set(true);
  }

  private loadCategories(): void {
    this.categoriesLoading.set(true);
    this.categoriesError.set(null);

    this.productService
      .getCategories()
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.categoriesError.set(
            this.apiErrorMessage(error, 'Não foi possível carregar as categorias.'),
          );
          return of([]);
        }),
        finalize(() => this.categoriesLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((categories) => {
        this.activeCategories.set(
          [...categories].sort((a, b) => a.name.localeCompare(b.name, 'pt-BR')),
        );
      });
  }

  private saveEdit(payload: ProductUpdateRequest): void {
    this.productService
      .updateProduct(payload.publicId, payload)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.handleSaveError(error, 'Não foi possível atualizar o produto.');
          return of(null);
        }),
        finalize(() => this.submitting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((product) => {
        if (product) {
          this.navigateToProduct(
            product.publicId,
            'Produto atualizado com sucesso.' + this.imageMockNote(),
          );
        }
      });
  }

  private navigateToProduct(publicId: string, message: string): void {
    void this.router.navigate(['/produtos', publicId], {
      state: { productFeedback: message, productFeedbackTone: 'success' },
    });
  }

  private imageMockNote(): string {
    return this.imageMode() === 'new'
      ? ' A imagem selecionada ficou apenas como preview (upload mock).'
      : '';
  }

  private leave(): void {
    void this.router.navigateByUrl(this.cancelTarget());
  }

  private handleSaveError(error: HttpErrorResponse, fallback: string): void {
    const body = error.error as
      | { detail?: string; errors?: string[]; title?: string; cause?: string }
      | null;
    const detail = body?.detail ?? '';
    const normalized = detail.toLowerCase();

    if (error.status === 404) {
      this.notFound.set(true);
      return;
    }

    if (error.status === 409) {
      if (normalized.includes('category')) {
        this.categoryServerError.set(detail || 'Categorias inválidas ou inativas.');
      } else if (normalized.includes('name or sku')) {
        this.formError.set(detail || 'Nome ou SKU já estão em uso.');
      } else if (normalized.includes('name')) {
        this.nameServerError.set(detail);
      } else if (normalized.includes('sku')) {
        this.skuServerError.set(detail);
      } else {
        this.formError.set(detail || 'Conflito ao salvar o produto.');
      }

      return;
    }

    if (error.status === 400) {
      this.formError.set(
        body?.errors?.[0] ??
          detail ??
          'Revise os campos destacados e tente novamente.',
      );
      return;
    }

    if (error.status === 429) {
      this.formError.set('Muitas requisições. Aguarde alguns instantes e tente novamente.');
      return;
    }

    if (error.status === 415) {
      this.formError.set('Falha de configuração da requisição. Contate o suporte.');
      return;
    }

    this.formError.set(this.apiErrorMessage(error, fallback));
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
