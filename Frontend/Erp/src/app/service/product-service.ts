import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Router } from '@angular/router';
import { receivedProduct, registerProductRequest } from '../models/productModels';
import { Observable } from 'rxjs';


@Service()
export class ProductService {

    private http = inject(HttpClient)

    private router = inject(Router)

    private urlApi = "/products"


    registerProduct(productInfo: registerProductRequest): Observable<any> {

        return this.http.post<receivedProduct>(`${this.urlApi}/register`, productInfo)
    }



}
