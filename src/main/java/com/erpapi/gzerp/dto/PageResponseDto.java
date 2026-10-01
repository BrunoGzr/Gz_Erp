package com.erpapi.gzerp.dto;

import java.util.List;

public class PageResponseDto {

    private List<?> itens;
    private int actualPage;
    private int totalPage;
    private long totalItens;
    private int pageSize;
    private boolean hasNext;

    public PageResponseDto() {
    }


    public long getTotalItens() {
        return totalItens;
    }

    public void setTotalItens(long totalItens) {
        this.totalItens = totalItens;
    }

    public List<?> getItens() {
        return itens;
    }

    public void setItens(List<?> itens) {
        this.itens = itens;
    }

    public int getActualPage() {
        return actualPage;
    }

    public void setActualPage(int actualPage) {
        this.actualPage = actualPage;
    }

    public long getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
