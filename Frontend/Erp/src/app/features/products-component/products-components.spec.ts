import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { ProductsComponents } from './products-components';

describe('ProductsComponents', () => {
  let component: ProductsComponents;
  let fixture: ComponentFixture<ProductsComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductsComponents],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap({}),
              paramMap: convertToParamMap({}),
            },
            queryParamMap: of(convertToParamMap({})),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductsComponents);
    component = fixture.componentInstance;

    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();

    const http = TestBed.inject(HttpTestingController);
    http.match((request) => request.url === '/api/products/categories').forEach((request) => {
      request.flush([]);
    });
    http.match((request) => request.url === '/api/products/overall').forEach((request) => {
      request.flush({
        items: [],
        actualPage: 0,
        totalPage: 0,
        totalItems: 0,
        pageSize: 20,
        hasNext: false,
      });
    });
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
