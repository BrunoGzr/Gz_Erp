import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../service/product-service';
import { registerProductRequest } from '../../models/productModels'

@Component({
  imports: [FormsModule],
  selector: 'app-dashboard-component',
  styleUrl: './dashboard-component.css',
  templateUrl: './dashboard-component.html',
})
export class DashboardComponent {


   product: registerProductRequest = {
    name: '',
    stock: 0,
    baseSellPrice: 0, 
    costPrice: 0,
    imageUrl: '',
    sku: ''
  };

  private productsService = inject(ProductService)

registerProduct(productInfo: registerProductRequest) {
    // Se chegou até aqui, o formulário já está válido (botão desabilitado se inválido)
    this.productsService.registerProduct(productInfo).subscribe({
      next: (response) => {
        console.log('Produto cadastrado com sucesso!', response);
        alert('Produto salvo com sucesso!');
        
        // Limpa o formulário após salvar (opcional)
        this.product = { name: '', stock: 0, baseSellPrice: 0, costPrice: 0, imageUrl: '', sku: '' };
      },
      error: (err) => {
        console.error('Erro ao cadastrar', err);
        alert('Ocorreu um erro ao salvar o produto.');
      }
    });
  }
}
