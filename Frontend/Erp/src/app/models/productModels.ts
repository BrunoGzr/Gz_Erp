export interface registerProductRequest {
    name: string,
    stock: number,
    baseSellPrice: number,
    costPrice: number,
    imageUrl: string,
    sku: string
}


export interface receivedProduct{
    publicId: string,
    sku: string,
    name: string,
    stock: number,
    baseSellPrice: number,
    costPrice: number,
    imageUrl: string,
    sales: number,
    createdAt: string,
    updatedAt: string,
}

export type Marketplace = 'SHOPEE' | 'MERCADOLIVRE';

export interface ProductResponse {
    publicId: string;
    sku: string | null;
    name: string;
    category?: string | null;
    stock: number;
    minStock: number;
    baseSellPrice: number;
    costPrice: number;
    imageUrl: string | null;
    sales: number;
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

