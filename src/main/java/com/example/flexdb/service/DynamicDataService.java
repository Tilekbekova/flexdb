package com.example.flexdb.service;

import com.example.flexdb.dto.PaginatedResponse;

import java.util.Map;

public interface DynamicDataService {

    Map<String, Object> insertRow(String tableName, Map<String, Object> data);

    PaginatedResponse getPaginatedData(String tableName, int page, int size);

    Map<String, Object> getRowById(String tableName, Long id);

    Map<String, Object> updateRow(String tableName, Long id, Map<String, Object> data);

    void deleteById(String tableName, Long id);
}
