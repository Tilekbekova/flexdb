package com.example.flexdb.controller;


import com.example.flexdb.dto.PaginatedResponse;
import com.example.flexdb.service.DynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dynamic-tables/data")
@RequiredArgsConstructor
public class DynamicDataController {

    private final DynamicDataService dynamicDataService;

    @PostMapping("/{tableName}")
    public ResponseEntity<Map<String, Object>> insertRow(@PathVariable String tableName,
                                                         @RequestBody Map<String, Object> rowData) {
        Map<String, Object> createdRow = dynamicDataService.insertRow(tableName, rowData);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRow);
    }

    @GetMapping("/{tableName}")
    public ResponseEntity<PaginatedResponse> getTableData(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaginatedResponse response = dynamicDataService.getPaginatedData(tableName, page, size);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> getRow(
            @PathVariable String tableName,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(dynamicDataService.getRowById(tableName, id));
    }


    @PutMapping("/{tableName}/{id}")
    public ResponseEntity<Map<String, Object>> updateRow(
            @PathVariable String tableName,
            @PathVariable Long id,
            @RequestBody Map<String, Object> data
    ) {
        return ResponseEntity.ok(dynamicDataService.updateRow(tableName, id, data));
    }


    @DeleteMapping("/{tableName}/{id}")
    public ResponseEntity<Void> deleteRow(
            @PathVariable String tableName,
            @PathVariable Long id
    ) {
        dynamicDataService.deleteById(tableName, id);
        return ResponseEntity.noContent().build();
    }


}
