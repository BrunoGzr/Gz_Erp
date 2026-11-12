export type Marketplace = 'SHOPEE' | 'MERCADOLIVRE';
export type ProductStatus = 'ACTIVE' | 'PAUSED' | 'INACTIVE';
export type StockFilter = 'LOW' | 'OUT' | 'NORMAL';
export type CategoryType = 'PRODUCT' | 'STATE';

export interface CategoryRef {
    publicId: string;
    name: string;
    type: CategoryType;
}

export interface CategoryCreateRequest {
    name: string;
    type: CategoryType;
}

export interface ProductResponse {
    publicId: string;
    sku: string | null;
    name: string;
    categories: CategoryRef[];
    stock: number;
    minStock: number;
    baseSellPrice: number | null;
    costPrice: number;
    imageUrl: string | null;
    sales: number;
    status: ProductStatus | null;
    createdAt: string;
    updatedAt: string;
    listingCount: Partial<Record<Marketplace, number>>;
}

export interface ProductPageResponse {
    items: ProductResponse[];
    actualPage: number;
    totalPage: number;
    totalItems: number;
    pageSize: number;
    hasNext: boolean;
}

export interface ProductRegisterRequest {
    name: string;
    sku?: string;
    stock: number;
    minStock?: number;
    baseSellPrice: number;
    costPrice: number;
    imageUrl?: string | null;
    categoryIds: string[];
}

export interface ProductUpdateRequest {
    publicId: string;
    name?: string;
    sku?: string;
    stock?: number;
    minStock?: number;
    baseSellPrice?: number;
    costPrice?: number;
    imageUrl?: string | null;
    categoryIds?: string[] | null;
    status?: ProductStatus;
}

export interface ProductStatusUpdateItem {
    publicId: string;
    status: ProductStatus;
}

export interface ProductBulkUpdateItem extends Omit<ProductUpdateRequest, 'publicId'> {
    publicId: string;
}

export interface ProductBulkEditPayload {
    name?: string;
    sku?: string;
    stock?: number;
    baseSellPrice?: number;
    costPrice?: number;
    imageUrl?: string | null;
    categoryIds?: string[];
    status?: ProductStatus;
}

export interface ApiProblem {
    type?: string;
    title?: string;
    status?: number;
    detail?: string;
    instance?: string;
    errors?: string[];
    cause?: string;
}

// Aliases legados usados pelo dashboard até a migração completa.
export type registerProductRequest = ProductRegisterRequest;
export type receivedProduct = ProductResponse;
