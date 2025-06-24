package com.example.flexdb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageInfo {
    private int pageNumber;
    private int pageSize;
}

