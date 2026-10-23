export interface registerProductRequest {
    name: string,
    stock: number,
    baseSellPrice: bigint,
    costPrice: bigint,
    imageUrl: string,
    sku: string
}


export interface receivedProduct{
    publicId: string,
    sku: string,
    name: string,
    stock: number,
    baseSellPrice: bigint,
    costPrice: bigint,
    imageUrl: string,
    sales: number,
    createdAt: string,
    updatedAt: string
}

