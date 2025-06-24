package com.example.flexdb.controller;

import com.example.flexdb.dto.CreateTableRequest;
import com.example.flexdb.dto.CreatedTableResponse;
import com.example.flexdb.dto.TableSummaryDto;
import com.example.flexdb.service.DynamicTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dynamic-tables")
@RequiredArgsConstructor
public class DynamicTableController {

    private final DynamicTableService dynamicTableService;

    @PostMapping("/schemas")
    public ResponseEntity<CreatedTableResponse> createTable(@Valid @RequestBody CreateTableRequest request) {
        CreatedTableResponse response = dynamicTableService.createDynamicTable(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    @GetMapping("/{tableName}")
    public ResponseEntity<CreatedTableResponse> getTableSchema(@PathVariable String tableName) {
        CreatedTableResponse response = dynamicTableService.getTableSchema(tableName);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<TableSummaryDto>> getAllTableSchemas() {
        List<TableSummaryDto> tables = dynamicTableService.getAllTableSummaries();
        return ResponseEntity.ok(tables);
    }
}
