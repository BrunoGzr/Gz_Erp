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
    updatedAt: string
}

