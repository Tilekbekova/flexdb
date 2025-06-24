package com.example.flexdb.service;


import com.example.flexdb.dto.CreateTableRequest;
import com.example.flexdb.dto.CreatedTableResponse;
import com.example.flexdb.dto.TableSummaryDto;

import java.util.List;

public interface DynamicTableService {
    CreatedTableResponse createDynamicTable(CreateTableRequest request);

    CreatedTableResponse getTableSchema(String tableName);

    List<TableSummaryDto> getAllTableSummaries();
}

