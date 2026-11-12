import { ProductResponse } from '../../models/productModels';

export type StockState = 'out' | 'low' | 'normal';

export function stockState(
  product: Pick<ProductResponse, 'stock' | 'minStock'>,
): StockState {
  if (product.stock <= 0) {
    return 'out';
  }

  if (product.stock <= product.minStock) {
    return 'low';
  }

  return 'normal';
}
