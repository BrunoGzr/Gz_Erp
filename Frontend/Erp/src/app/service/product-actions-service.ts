import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ProductBulkEditPayload,
  ProductBulkUpdateItem,
  ProductResponse,
  ProductStatus,
} from '../models/productModels';
import { ProductService } from './product-service';

@Service()
export class ProductActionsService {
  private readonly productService = inject(ProductService);

  updateStatus(publicId: string, status: ProductStatus): Observable<void> {
    return this.productService.updateProductsStatus([{ publicId, status }]);
  }

  delete(publicId: string): Observable<void> {
    return this.productService.deleteProduct(publicId);
  }

  deleteMany(publicIds: string[]): Observable<void> {
    return this.productService.bulkDeleteProducts(publicIds);
  }

  updateMany(
    products: ProductResponse[],
    payload: ProductBulkEditPayload,
  ): Observable<ProductResponse[]> {
    return this.productService.bulkUpdateProducts(this.buildBulkUpdateItems(products, payload));
  }

  buildBulkUpdateItems(
    products: ProductResponse[],
    payload: ProductBulkEditPayload,
  ): ProductBulkUpdateItem[] {
    return products.map((product) => {
      const item: ProductBulkUpdateItem = { publicId: product.publicId };

      if (payload.name !== undefined) {
        item.name = payload.name;
      }

      if (payload.sku !== undefined) {
        item.sku = payload.sku;
      } else if (product.sku) {
        item.sku = product.sku;
      }

      if (payload.stock !== undefined) {
        item.stock = payload.stock;
      }

      if (payload.baseSellPrice !== undefined) {
        item.baseSellPrice = payload.baseSellPrice;
      }

      if (payload.costPrice !== undefined) {
        item.costPrice = payload.costPrice;
      }

      if (payload.imageUrl !== undefined) {
        item.imageUrl = payload.imageUrl;
      }

      item.categoryIds = payload.categoryIds ?? null;

      if (payload.status !== undefined) {
        item.status = payload.status;
      }

      return item;
    });
  }
}
