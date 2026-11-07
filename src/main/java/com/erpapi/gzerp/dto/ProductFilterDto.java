package com.erpapi.gzerp.dto;

import com.erpapi.gzerp.enums.Marketplace;
import com.erpapi.gzerp.enums.StockFilter;

public record ProductFilterDto(
        String category,
        Marketplace marketplace,
        Boolean hasListings,
        StockFilter stock,
        String searchTerm
) {
}
