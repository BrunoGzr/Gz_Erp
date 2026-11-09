package com.erpapi.gzerp.repositories;

import com.erpapi.gzerp.models.DefaultCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DefaultCategoryRepo extends JpaRepository<DefaultCategory, Long> {


    List<DefaultCategory> findAllByActiveTrue();


}
