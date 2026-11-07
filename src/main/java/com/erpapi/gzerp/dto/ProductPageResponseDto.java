package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.enums.StockFilter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductPageResponseDto(
        List<ProductResponseDto> items,
        int actualPage,
        int totalPage,
        Long totalItems,
        int pageSize,
        Boolean hasNext)
{}



