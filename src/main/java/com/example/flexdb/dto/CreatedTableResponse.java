package com.example.flexdb.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatedTableResponse {
    private String tableName;
    private String userFriendlyName;
    private List<CreatedColumnDto> columns;
}

