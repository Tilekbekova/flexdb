package com.example.flexdb.service.impl;


import com.example.flexdb.dto.*;
import com.example.flexdb.entity.DynamicColumnDefinition;
import com.example.flexdb.entity.DynamicTableDefinition;
import com.example.flexdb.enums.SupportedColumnType;
import com.example.flexdb.exception.ResourceNotFoundException;
import com.example.flexdb.repository.DynamicTableDefinitionRepository;
import com.example.flexdb.service.DynamicTableService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DynamicTableServiceImpl implements DynamicTableService {

    private final JdbcTemplate jdbcTemplate;
    private final DynamicTableDefinitionRepository tableRepo;

    /**
     * Создаёт новую таблицу в PostgreSQL на основе пользовательского запроса и сохраняет метаданные.
     */
    @Override
    @Transactional
    public CreatedTableResponse createDynamicTable(CreateTableRequest request) {
        // 1. Валидация входных данных
        validateTableName(request.getTableName());
        validateColumns(request.getColumns());

        // 2. Подготовка колонок (включая автоматическую id)
        List<DynamicColumnDefinition> columns = prepareColumnEntities(request);

        // 3. Генерация безопасного SQL
        String createSql = generateCreateTableSql(request.getTableName(), columns);

        // 4. Выполнение DDL (создание таблицы)
        jdbcTemplate.execute(createSql);

        // 5. Сохранение метаданных
        DynamicTableDefinition table = new DynamicTableDefinition();
        table.setTableName(request.getTableName());
        table.setUserFriendlyName(request.getUserFriendlyName());
        columns.forEach(col -> col.setTableDefinition(table));
        table.setColumns(columns);
        tableRepo.save(table);


        // 6. Сохранение метаданных
        CreatedTableResponse response = new CreatedTableResponse();
        response.setTableName(table.getTableName());
        response.setUserFriendlyName(table.getUserFriendlyName());
        response.setColumns(columns.stream().map(col -> {
            CreatedColumnDto dto = new CreatedColumnDto();
            dto.setName(col.getColumnName());
            dto.setType(col.getColumnType());
            dto.setPostgresType(col.getPostgresColumnType());
            dto.setNullable(col.isNullable());
            dto.setPrimaryKey(col.isPrimaryKeyInternal());
            return dto;
        }).toList());

        return response;

    }

    /**
     * Генерирует список колонок, включая авто-добавляемую id.
     */
    private List<DynamicColumnDefinition> prepareColumnEntities(CreateTableRequest request) {
        List<DynamicColumnDefinition> result = new ArrayList<>();

        // Автоматически добавляем колонку id
        DynamicColumnDefinition idColumn = new DynamicColumnDefinition();
        idColumn.setColumnName("id");
        idColumn.setColumnType("BIGINT");
        idColumn.setPostgresColumnType("BIGINT");
        idColumn.setNullable(false);
        idColumn.setPrimaryKeyInternal(true);
        result.add(idColumn);

        // Остальные колонки из запроса
        for (ColumnDefinitionDto col : request.getColumns()) {
            DynamicColumnDefinition entity = new DynamicColumnDefinition();
            entity.setColumnName(col.getName());
            entity.setColumnType(col.getType());
            entity.setPostgresColumnType(SupportedColumnType.valueOf(col.getType()).getPostgresType());
            entity.setNullable(Boolean.TRUE.equals(col.getIsNullable()));
            entity.setPrimaryKeyInternal(Boolean.TRUE.equals(col.getIsPrimaryKeyInternal()));
            result.add(entity);
        }

        return result;
    }

    /**
     * Генерирует SQL CREATE TABLE с безопасным квотированием.
     */
    private String generateCreateTableSql(String tableName, List<DynamicColumnDefinition> columns) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sql.append('"').append(tableName).append('"').append(" (\n");

        List<String> columnDefs = new ArrayList<>();
        for (DynamicColumnDefinition col : columns) {
            StringBuilder def = new StringBuilder();
            def.append('"').append(col.getColumnName()).append('"')
                    .append(" ").append(col.getPostgresColumnType());

            if (!col.isNullable()) {
                def.append(" NOT NULL");
            }
            if (col.isPrimaryKeyInternal()) {
                def.append(" PRIMARY KEY");
            }

            columnDefs.add(def.toString());
        }

        sql.append(String.join(",\n", columnDefs));
        sql.append("\n);");

        return sql.toString();
    }

    /**
     * Проверка имени таблицы на соответствие требованиям (регулярка, префиксы, уникальность).
     */
    private void validateTableName(String tableName) {
        if (tableName.startsWith("pg_") || tableName.startsWith("app_")) {
            throw new IllegalArgumentException("Имя таблицы начинается с зарезервированного префикса.");
        }
        if (!tableName.matches("^(?=.*_)[a-z0-9_]{3,63}$")) {
            throw new IllegalArgumentException("Имя таблицы должно содержать от 3 до 63 символов: только строчные латинские буквы, цифры и подчёркивания. Подчёркивание обязательно.");
        }

        if (tableRepo.existsByTableName(tableName)) {
            throw new IllegalArgumentException("Таблица с таким именем уже существует.");
        }
    }

    /**
     * //Проверка списка колонок на дубли, запрещённые имена и поддерживаемые типы.
     */
    private void validateColumns(List<ColumnDefinitionDto> columns) {
        Set<String> names = new HashSet<>();

        for (ColumnDefinitionDto col : columns) {
            String colName = col.getName() != null ? col.getName() : "[без имени]";
            if (columns.isEmpty()) {
                throw new IllegalArgumentException("Список колонок не может быть пустым.");
            }
            if (col.getType() == null) {
                throw new IllegalArgumentException("У колонки '" + colName + "' не указан тип.");
            }

            if (!SupportedColumnType.isSupported(col.getType())) {
                throw new IllegalArgumentException("Неподдерживаемый тип колонки '" + colName + "': " + col.getType());
            }

            if ("id".equalsIgnoreCase(colName)) {
                throw new IllegalArgumentException("Имя колонки 'id' зарезервировано системой.");
            }

            if (colName.startsWith("pg_") || colName.startsWith("app_")) {
                throw new IllegalArgumentException("Имя колонки '" + colName + "' начинается с зарезервированного префикса.");
            }

            if (!colName.matches("^(?=.*_)[a-z0-9_]{3,63}$")) {
                throw new IllegalArgumentException("Имя колонки должно содержать от 3 до 63 символов: только строчные латинские буквы, цифры и подчёркивания. Подчёркивание обязательно.");
            }



            if (!names.add(colName)) {
                throw new IllegalArgumentException("Повторяющееся имя колонки: '" + colName + "'");
            }
        }
    }

    /**
     * Получение полной схемы таблицы по имени.
     * @param tableName имя таблицы
     * @return объект с описанием таблицы и всех её колонок
     */
    @Override
    public CreatedTableResponse getTableSchema(String tableName) {
        DynamicTableDefinition table = tableRepo.findByTableName(tableName)
                .orElseThrow(() -> new ResourceNotFoundException("Таблица '" + tableName + "' не найдена"));

        CreatedTableResponse response = new CreatedTableResponse();
        response.setTableName(table.getTableName());
        response.setUserFriendlyName(table.getUserFriendlyName());
        response.setColumns(table.getColumns().stream().map(col -> {
            CreatedColumnDto dto = new CreatedColumnDto();
            dto.setName(col.getColumnName());
            dto.setType(col.getColumnType());
            dto.setPostgresType(col.getPostgresColumnType());
            dto.setNullable(col.isNullable());
            dto.setPrimaryKey(col.isPrimaryKeyInternal());
            return dto;
        }).toList());

        return response;
    }

    /**
     * Получение списка всех созданных пользователем таблиц с краткой информацией.
     * @return список таблиц
     */
    @Override
    public List<TableSummaryDto> getAllTableSummaries() {
        List<DynamicTableDefinition> allTables = tableRepo.findAll();

        return allTables.stream().map(t -> {
            TableSummaryDto dto = new TableSummaryDto();
            dto.setTableName(t.getTableName());
            dto.setUserFriendlyName(t.getUserFriendlyName());
            dto.setColumnCount(t.getColumns() != null ? t.getColumns().size() : 0);
            return dto;
        }).collect(Collectors.toList());
    }

}


