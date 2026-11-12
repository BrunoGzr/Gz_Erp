package com.erpapi.gzerp.dto.Products;

import java.util.List;

public record ProductPageResponseDto(
        List<ProductResponseDto> items,
        int actualPage,
        int totalPage,
        Long totalItems,
        int pageSize,
        Boolean hasNext)
{}



