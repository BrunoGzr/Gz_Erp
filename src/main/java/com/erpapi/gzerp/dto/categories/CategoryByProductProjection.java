package com.erpapi.gzerp.dto.categories;

import com.erpapi.gzerp.enums.CategoryType;

public interface CategoryByProductProjection {
    Long getProductId();
    String getPublicId();
    String getName();
    CategoryType getType();
}