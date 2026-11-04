import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductsComponents } from './products-components';

describe('ProductsComponents', () => {
  let component: ProductsComponents;
  let fixture: ComponentFixture<ProductsComponents>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductsComponents],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductsComponents);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
