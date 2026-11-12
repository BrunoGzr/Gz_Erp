import { CategoryType, Marketplace, ProductStatus } from '../../models/productModels';

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

const numberFormatter = new Intl.NumberFormat('pt-BR');
const dateFormatter = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' });

const MARKETPLACE_LABELS: Record<Marketplace, string> = {
  SHOPEE: 'Shopee',
  MERCADOLIVRE: 'Mercado Livre',
};

export function formatCurrency(value: number | null | undefined): string {
  return value == null ? '--' : currencyFormatter.format(value);
}

export function formatNumber(value: number | null | undefined): string {
  return value == null ? '--' : numberFormatter.format(value);
}

export function formatDate(iso: string | null | undefined): string {
  if (!iso) {
    return '--';
  }

  const date = new Date(iso);

  return Number.isNaN(date.getTime()) ? '--' : dateFormatter.format(date);
}

export function relativeUpdated(iso: string | null | undefined): string {
  if (!iso) {
    return '--';
  }

  const date = new Date(iso);

  if (Number.isNaN(date.getTime())) {
    return '--';
  }

  const diffDays = Math.round((Date.now() - date.getTime()) / 86_400_000);

  if (diffDays <= 1) {
    return diffDays <= 0 ? 'hoje' : 'ontem';
  }

  return `há ${diffDays} dias`;
}

export function marketplaceLabel(marketplace: Marketplace): string {
  return MARKETPLACE_LABELS[marketplace];
}

export function statusLabel(status: ProductStatus | null): string {
  if (status === 'ACTIVE') {
    return 'Ativo';
  }

  if (status === 'PAUSED') {
    return 'Pausado';
  }

  if (status === 'INACTIVE') {
    return 'Inativo';
  }

  return 'Sem status';
}

export function categoryTypeLabel(type: CategoryType): string {
  return type === 'STATE' ? 'Estado' : 'Produto';
}

export function statusTone(status: ProductStatus | null): 'active' | 'paused' | 'inactive' | 'unknown' {
  if (status === 'ACTIVE') {
    return 'active';
  }

  if (status === 'PAUSED') {
    return 'paused';
  }

  if (status === 'INACTIVE') {
    return 'inactive';
  }

  return 'unknown';
}

export function normalizeForComparison(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase();
}
