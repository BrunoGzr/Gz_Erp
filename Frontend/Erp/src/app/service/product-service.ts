import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import {
    CategoryCreateRequest,
    CategoryRef,
    CategoryType,
    Marketplace,
    ProductBulkUpdateItem,
    ProductPageResponse,
    ProductRegisterRequest,
    ProductResponse,
    ProductStatusUpdateItem,
    ProductUpdateRequest,
} from '../models/productModels';

@Service()
export class ProductService {

    private http = inject(HttpClient)

    private apiUrl = '/api/products'

    registerProduct(payload: ProductRegisterRequest): Observable<ProductResponse> {
        return this.http.post<ProductResponse>(`${this.apiUrl}/register`, payload)
    }

    getProduct(publicId: string): Observable<ProductResponse> {
        return this.http.get<ProductResponse>(`${this.apiUrl}/${encodeURIComponent(publicId)}`)
    }

    updateProduct(publicId: string, payload: ProductUpdateRequest): Observable<ProductResponse> {
        return this.http.patch<ProductResponse>(
            `${this.apiUrl}/${encodeURIComponent(publicId)}`,
            payload,
        )
    }

    deleteProduct(publicId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${encodeURIComponent(publicId)}`)
    }

    updateProductsStatus(items: ProductStatusUpdateItem[]): Observable<void> {
        return this.http.patch<void>(`${this.apiUrl}/status`, items)
    }

    bulkUpdateProducts(items: ProductBulkUpdateItem[]): Observable<ProductResponse[]> {
        return this.http.patch<ProductResponse[]>(`${this.apiUrl}/bulk`, items)
    }

    bulkDeleteProducts(publicIds: string[]): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/bulk/delete`, { body: publicIds })
    }

    getCategories(type?: CategoryType): Observable<CategoryRef[]> {
        let params = new HttpParams();

        if (type) {
            params = params.set('type', type);
        }

        return this.http.get<CategoryRef[]>(`${this.apiUrl}/categories`, { params })
    }

    createCategory(payload: CategoryCreateRequest): Observable<CategoryRef> {
        return this.http.post<CategoryRef>(`${this.apiUrl}/categories`, payload)
    }

    getOverallProducts(params: {
        page: number;
        size: number;
        sort?: string | null;
        searchTerm?: string | null;
        marketplace?: Marketplace | null;
        hasListings?: boolean | null;
        stock?: 'LOW' | 'OUT' | 'NORMAL' | null;
        categoryIds?: string[] | null;
    }): Observable<ProductPageResponse> {
        let httpParams = new HttpParams()
            .set('page', params.page)
            .set('size', params.size);

        if (params.sort) {
            httpParams = httpParams.set('sort', params.sort);
        }

        if (params.searchTerm) {
            httpParams = httpParams.set('searchTerm', params.searchTerm);
        }

        if (params.marketplace) {
            httpParams = httpParams.set('marketplace', params.marketplace);
        }

        if (typeof params.hasListings === 'boolean') {
            httpParams = httpParams.set('hasListings', params.hasListings);
        }

        if (params.stock) {
            httpParams = httpParams.set('stock', params.stock);
        }

        for (const categoryId of params.categoryIds ?? []) {
            httpParams = httpParams.append('categoryIds', categoryId);
        }

        return this.http.get<ProductPageResponse>(`${this.apiUrl}/overall`, { params: httpParams })
    }

}
