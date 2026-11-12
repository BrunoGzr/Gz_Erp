import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProductService } from './product-service';

const product = {
  publicId: '00000000-0000-4000-8000-000000000000',
  sku: 'SKU-1',
  name: 'Produto',
  stock: 10,
  minStock: 2,
  baseSellPrice: 10,
  costPrice: 5,
  imageUrl: null,
  sales: 0,
  status: 'ACTIVE' as const,
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  listingCount: {},
  categories: [],
};

describe('ProductService', () => {
  let service: ProductService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get categories filtered by type', () => {
    service.getCategories('PRODUCT').subscribe();

    const request = http.expectOne('/api/products/categories?type=PRODUCT');
    expect(request.request.method).toBe('GET');
    request.flush([{ publicId: 'c1', name: 'Eletrônicos', type: 'PRODUCT' }]);
  });

  it('should create a category with type', () => {
    const payload = { name: 'Nova', type: 'STATE' as const };

    service.createCategory(payload).subscribe();

    const request = http.expectOne('/api/products/categories');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ publicId: 'c2', name: 'Nova', type: 'STATE' });
  });

  it('should get a product by publicId', () => {
    service.getProduct(product.publicId).subscribe((response) => {
      expect(response.publicId).toBe(product.publicId);
    });

    const request = http.expectOne(`/api/products/${product.publicId}`);
    expect(request.request.method).toBe('GET');
    request.flush(product);
  });

  it('should register a product', () => {
    const payload = {
      name: 'Produto',
      stock: 1,
      baseSellPrice: 10,
      costPrice: 5,
      categoryIds: [],
    };

    service.registerProduct(payload).subscribe();

    const request = http.expectOne('/api/products/register');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush(product);
  });

  it('should update a product with publicId in the body', () => {
    const payload = { publicId: product.publicId, stock: 3 };

    service.updateProduct(product.publicId, payload).subscribe();

    const request = http.expectOne(`/api/products/${product.publicId}`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(payload);
    request.flush(product);
  });

  it('should delete a product', () => {
    service.deleteProduct(product.publicId).subscribe();

    const request = http.expectOne(`/api/products/${product.publicId}`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });

  it('should update statuses in batch', () => {
    const items = [{ publicId: product.publicId, status: 'PAUSED' as const }];

    service.updateProductsStatus(items).subscribe();

    const request = http.expectOne('/api/products/status');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(items);
    request.flush(null);
  });

  it('should bulk update products', () => {
    const items = [{ publicId: product.publicId, sku: 'SKU-1', stock: 2 }];

    service.bulkUpdateProducts(items).subscribe();

    const request = http.expectOne('/api/products/bulk');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(items);
    request.flush([product]);
  });

  it('should bulk delete products with a body', () => {
    const ids = [product.publicId];

    service.bulkDeleteProducts(ids).subscribe();

    const request = http.expectOne('/api/products/bulk/delete');
    expect(request.request.method).toBe('DELETE');
    expect(request.request.body).toEqual(ids);
    request.flush(null);
  });
});
