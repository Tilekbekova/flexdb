package com.example.flexdb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class PaginatedResponse {
    private List<Map<String, Object>> content;
    private PageInfo pageable;
    private int totalPages;
    private int totalElements;
    private boolean last;
    private boolean first;
}

