package com.example.flexdb.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableSummaryDto {

    private String tableName;


    private String userFriendlyName;


    private int columnCount;
}

