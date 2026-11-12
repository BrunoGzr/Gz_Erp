import { Component, computed, input, output } from '@angular/core';
import { Marketplace, ProductResponse, ProductStatus } from '../../models/productModels';
import { formatCurrency, formatNumber, marketplaceLabel } from '../../shared/format/product-format';
import { ProductStatusBadgeComponent } from '../../shared/ui/product-status-badge/product-status-badge';

interface MockListing {
  id: string;
  title: string;
  marketplace: Marketplace;
  status: ProductStatus;
  price: number | null;
  stock: number;
  sales: number;
}

@Component({
  selector: 'app-product-listings-mock',
  imports: [ProductStatusBadgeComponent],
  templateUrl: './product-listings-mock-component.html',
  styleUrl: './product-listings-mock-component.css',
})
export class ProductListingsMockComponent {
  readonly product = input.required<ProductResponse>();
  readonly action = output<string>();

  readonly marketplaceLabel = marketplaceLabel;
  readonly formatCurrency = formatCurrency;
  readonly formatNumber = formatNumber;

  readonly listings = computed<MockListing[]>(() => {
    const product = this.product();
    const entries = (Object.keys(product.listingCount) as Marketplace[])
      .map((marketplace) => ({
        marketplace,
        count: product.listingCount[marketplace] ?? 0,
      }))
      .filter((entry) => entry.count > 0);

    if (entries.length === 0) {
      return [
        {
          id: 'mock-shopee',
          title: `${product.name} — demonstração`,
          marketplace: 'SHOPEE',
          status: 'ACTIVE',
          price: product.baseSellPrice,
          stock: product.stock,
          sales: 0,
        },
        {
          id: 'mock-mercado-livre',
          title: `${product.name} — demonstração`,
          marketplace: 'MERCADOLIVRE',
          status: 'PAUSED',
          price: product.baseSellPrice,
          stock: product.stock,
          sales: 0,
        },
      ];
    }

    return entries.flatMap((entry) =>
      Array.from({ length: Math.min(entry.count, 3) }, (_, index) => ({
        id: `${entry.marketplace}-${index}`,
        title: `${product.name} — ${marketplaceLabel(entry.marketplace)} ${index + 1}`,
        marketplace: entry.marketplace,
        status: 'ACTIVE' as ProductStatus,
        price: product.baseSellPrice,
        stock: product.stock,
        sales: 0,
      })),
    );
  });

  readonly isMock = computed(() => {
    const product = this.product();
    return Object.values(product.listingCount).every((count) => (count ?? 0) === 0);
  });

  notify(message: string): void {
    this.action.emit(message);
  }
}
