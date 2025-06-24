package com.example.flexdb.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ColumnDefinitionDto {

    @NotBlank(message = "Имя колонки не должно быть пустым")
    @Size(min = 1, max = 63, message = "Имя колонки должно содержать от 1 до 63 символов")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Имя колонки может содержать только строчные латинские буквы, цифры и подчёркивания")
    private String name;

    @NotBlank(message = "Тип колонки не должен быть пустым")
    private String type;

    private String postgresColumnType;

    private Boolean isNullable = true;

    private Boolean isPrimaryKeyInternal = false;
}