package com.example.dalats.model;

public class IncidentSearchDTO {
    private String keyword;
    private String ward;
    private Integer categoryId;
    private int pageIndex;
    private int pageSize;

    public IncidentSearchDTO(String keyword, String ward, Integer categoryId, int pageIndex, int pageSize) {
        this.keyword = keyword;
        this.ward = ward;
        this.categoryId = categoryId;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
}