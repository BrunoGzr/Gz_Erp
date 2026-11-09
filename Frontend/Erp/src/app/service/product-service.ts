import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import {
    CategoryRef,
    Marketplace,
    ProductPageResponse,
    receivedProduct,
    registerProductRequest,
} from '../models/productModels';


@Service()
export class ProductService {

    private http = inject(HttpClient)

    private router = inject(Router)

    private urlApi = "/products"
    private apiUrl = "/api"


    registerProduct(productInfo: registerProductRequest): Observable<any> {

        return this.http.post<receivedProduct>(`${this.urlApi}/register`, productInfo)
    }

    getCategories(): Observable<CategoryRef[]> {
        return this.http.get<CategoryRef[]>(`${this.apiUrl}/products/categories`)
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

        return this.http.get<ProductPageResponse>(`${this.apiUrl}/products/overall`, { params: httpParams })
    }

}
