package com.example.flexdb.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateTableRequest {

    @NotBlank(message = "Имя таблицы не должно быть пустым")
    @Size(min = 3, max = 63, message = "Имя таблицы должно содержать от 3 до 63 символов")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Имя таблицы может содержать только строчные латинские буквы, цифры и подчёркивания")
    private String tableName;

    @Size(max = 255, message = "Описание таблицы должно быть не длиннее 255 символов")
    private String userFriendlyName;

    @NotNull(message = "Список колонок обязателен")
    @Size(min = 1, message = "Список колонок не может быть пустым")
    private List<@Valid ColumnDefinitionDto> columns;
}
