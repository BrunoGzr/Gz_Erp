import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Router } from '@angular/router';
import { receivedProduct, registerProductRequest } from '../models/productModels';
import { Observable } from 'rxjs';


@Service()
export class ProductService {

    private http = inject(HttpClient)

    private router = inject(Router)

    private urlApi = "http://192.168.15.23:8989/products"


    registerProduct(productInfo: registerProductRequest): Observable<any> {
         
        return this.http.post<receivedProduct>(`${this.urlApi}/register`, productInfo)
    }



}
