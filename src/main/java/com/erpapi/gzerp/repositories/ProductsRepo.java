package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepo extends JpaRepository<Products, Long> {

}
