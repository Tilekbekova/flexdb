package com.example.flexdb.dto;


import lombok.Data;

@Data
public class CreatedColumnDto {
    private String name;
    private String type;
    private String postgresType;
    private boolean isNullable;
    private boolean isPrimaryKey;
}

