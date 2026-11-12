import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ProductActionsService } from './product-actions-service';
import { ProductResponse } from '../models/productModels';

const product: ProductResponse = {
  publicId: 'p1',
  sku: 'SKU-1',
  name: 'Produto',
  stock: 4,
  minStock: 2,
  baseSellPrice: 10,
  costPrice: 5,
  imageUrl: null,
  sales: 0,
  status: 'ACTIVE',
  createdAt: '2026-01-01T00:00:00',
  updatedAt: '2026-01-01T00:00:00',
  listingCount: {},
  categories: [],
};

describe('ProductActionsService', () => {
  let service: ProductActionsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProductActionsService);
  });

  it('should preserve the current SKU and null categories when not changed', () => {
    const items = service.buildBulkUpdateItems([product], { stock: 8 });

    expect(items).toEqual([
      {
        publicId: 'p1',
        sku: 'SKU-1',
        stock: 8,
        categoryIds: null,
      },
    ]);
  });

  it('should apply explicit name, SKU, image, categories and status', () => {
    const items = service.buildBulkUpdateItems([product], {
      name: 'Novo nome',
      sku: 'SKU-2',
      imageUrl: '',
      categoryIds: ['c1'],
      status: 'PAUSED',
    });

    expect(items).toEqual([
      {
        publicId: 'p1',
        name: 'Novo nome',
        sku: 'SKU-2',
        imageUrl: '',
        categoryIds: ['c1'],
        status: 'PAUSED',
      },
    ]);
  });
});
