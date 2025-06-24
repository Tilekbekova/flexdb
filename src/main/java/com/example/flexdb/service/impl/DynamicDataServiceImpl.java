package com.example.flexdb.service.impl;

import com.example.flexdb.dto.PageInfo;
import com.example.flexdb.dto.PaginatedResponse;
import com.example.flexdb.entity.DynamicColumnDefinition;
import com.example.flexdb.entity.DynamicTableDefinition;
import com.example.flexdb.exception.ResourceNotFoundException;
import com.example.flexdb.repository.DynamicTableDefinitionRepository;
import com.example.flexdb.service.DynamicDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicDataServiceImpl implements DynamicDataService {

    private final DynamicTableDefinitionRepository tableRepo;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Добавляет новую строку в указанную динамическую таблицу.
     *
     * @param tableName имя таблицы
     * @param data      данные для вставки
     * @return карта с ID и вставленными данными
     */
    @Override
    @Transactional
    public Map<String, Object> insertRow(String tableName, Map<String, Object> data) {
        log.info("▶️ Вставка строки в таблицу '{}'", tableName);

        DynamicTableDefinition table = tableRepo.findByTableName(tableName)
                .orElseThrow(() -> new ResourceNotFoundException("Таблица '" + tableName + "' не найдена"));

        List<DynamicColumnDefinition> columns = table.getColumns();

        // Проверка колонок
        Set<String> knownColumns = columns.stream()
                .filter(col -> !col.isPrimaryKeyInternal())
                .map(DynamicColumnDefinition::getColumnName)
                .collect(Collectors.toSet());

        for (String key : data.keySet()) {
            if (!knownColumns.contains(key)) {
                throw new IllegalArgumentException("Неизвестная колонка: '" + key + "'");
            }
        }

        for (DynamicColumnDefinition col : columns) {
            if (col.isPrimaryKeyInternal()) continue;
            String colName = col.getColumnName();
            Object value = data.get(colName);

            if (!col.isNullable() && value == null) {
                throw new IllegalArgumentException("Колонка '" + colName + "' обязательна для заполнения");
            }

            if (value != null) {
                validateValueType(colName, col.getColumnType(), value);
            }
        }

        List<String> columnNames = new ArrayList<>(data.keySet());
        List<Object> values = columnNames.stream().map(data::get).toList();

        String sql = String.format(
                "INSERT INTO \"%s\" (%s) VALUES (%s) RETURNING id",
                tableName,
                columnNames.stream().map(col -> "\"" + col + "\"").collect(Collectors.joining(", ")),
                columnNames.stream().map(col -> "?").collect(Collectors.joining(", "))
        );

        log.debug("📥 SQL: {}", sql);
        log.debug("📦 Params: {}", values);

        Long id = jdbcTemplate.query(sql, values.toArray(), rs -> rs.next() ? rs.getLong("id") : null);

        if (id == null) {
            throw new IllegalStateException("База данных не вернула ID");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.putAll(data);

        log.info("✅ Успешно вставлено в таблицу '{}', ID: {}", tableName, id);
        return result;
    }

    /**
     * Получает список строк из таблицы с пагинацией.
     *
     * @param tableName имя таблицы
     * @param page      номер страницы
     * @param size      размер страницы
     * @return пагинированный ответ
     */
    @Override
    @Transactional(readOnly = true) // readOnly так как только читаем из бд
    public PaginatedResponse getPaginatedData(String tableName, int page, int size) {

        log.info("Получение страницы {} (размер {}) из таблицы '{}'", page, size, tableName);

        tableRepo.findByTableName(tableName)
                .orElseThrow(() -> new ResourceNotFoundException("Таблица '" + tableName + "' не найдена"));

        int maxSize = 100;
        int safeSize = Math.min(size, maxSize);
        int offset = page * safeSize;

        String sql = String.format("SELECT * FROM \"%s\" ORDER BY id ASC LIMIT ? OFFSET ?", tableName);
        List<Map<String, Object>> content = jdbcTemplate.queryForList(sql, safeSize, offset);

        String countSql = String.format("SELECT COUNT(*) FROM \"%s\"", tableName);
        Integer totalElementsRaw = jdbcTemplate.queryForObject(countSql, Integer.class);
        int totalElements = totalElementsRaw != null ? totalElementsRaw : 0;

        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        return new PaginatedResponse(
                content,
                new PageInfo(page, safeSize),
                totalPages,
                totalElements,
                page == totalPages - 1,
                page == 0
        );
    }

    /**
     * Получает запись по ID.
     */

    @Override
    @Transactional(readOnly = true) // readOnly так как только читаем из бд
    public Map<String, Object> getRowById(String tableName, Long id) {
        log.info("Получение записи id = {} из таблицы '{}'", id, tableName);

        tableRepo.findByTableName(tableName)
                .orElseThrow(() -> new ResourceNotFoundException("Таблица '" + tableName + "' не найдена"));

        String sql = String.format("SELECT * FROM \"%s\" WHERE id = ?", tableName);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, id);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("Запись с id " + id + " не найдена в таблице '" + tableName + "'");
        }

        return rows.get(0);
    }

    /**
     * Полностью обновляет запись по ID.
     */

    @Override
    @Transactional
    public Map<String, Object> updateRow(String tableName, Long id, Map<String, Object> data) {
        log.info("Обновление записи id = {} в таблице '{}'", id, tableName);

        DynamicTableDefinition table = tableRepo.findByTableName(tableName)
                .orElseThrow(() -> new ResourceNotFoundException("Таблица '" + tableName + "' не найдена"));

        String checkSql = String.format("SELECT COUNT(*) FROM \"%s\" WHERE id = ?", tableName);
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);
        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Запись с id " + id + " не найдена в таблице '" + tableName + "'");
        }

        List<DynamicColumnDefinition> columns = table.getColumns();

        Set<String> knownColumns = columns.stream()
                .filter(col -> !col.isPrimaryKeyInternal())
                .map(DynamicColumnDefinition::getColumnName)
                .collect(Collectors.toSet());

        for (String key : data.keySet()) {
            if (!knownColumns.contains(key)) {
                throw new IllegalArgumentException("Неизвестная колонка: '" + key + "'");
            }
        }

        for (DynamicColumnDefinition col : columns) {
            if (col.isPrimaryKeyInternal()) continue;
            String colName = col.getColumnName();
            Object value = data.get(colName);

            if (!col.isNullable() && value == null) {
                throw new IllegalArgumentException("Колонка '" + colName + "' обязательна для заполнения");
            }

            if (value != null) {
                validateValueType(colName, col.getColumnType(), value);
            }
        }

        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (String col : data.keySet()) {
            setClauses.add("\"" + col + "\" = ?");
            values.add(data.get(col));
        }

        values.add(id);

        String sql = String.format(
                "UPDATE \"%s\" SET %s WHERE id = ?",
                tableName,
                String.join(", ", setClauses)
        );

        jdbcTemplate.update(sql, values.toArray());
        log.info("✅ Запись id = {} в таблице '{}' успешно обновлена", id, tableName);

        return getRowById(tableName, id);
    }

    /**
     * Удаляет запись по ID из указанной таблицы.
     */
    @Override
    @Transactional
    public void deleteById(String tableName, Long id) {

        log.info("Удаление записи id = {} из таблицы '{}'", id, tableName);

        tableRepo.findByTableName(tableName)
                .orElseThrow(() -> new ResourceNotFoundException("Таблица '" + tableName + "' не найдена"));

        String sql = String.format("DELETE FROM \"%s\" WHERE id = ?", tableName);
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected == 0) {
            throw new ResourceNotFoundException("Запись с id = " + id + " не найдена в таблице '" + tableName + "'");
        }

        log.info("Запись id = {} успешно удалена из таблицы '{}'", id, tableName);
    }

    /**
     * Проверка значения на соответствие ожидаемому типу колонки.
     */
    private void validateValueType(String columnName, String columnType, Object value) {
        switch (columnType.toUpperCase()) {
            case "TEXT", "DATE", "TIMESTAMP" -> {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("Колонка '" + columnName + "' ожидает строку (TEXT), но получено: " + value.getClass().getSimpleName());
                }
            }
            case "INTEGER" -> {
                if (!(value instanceof Integer)) {
                    throw new IllegalArgumentException("Колонка '" + columnName + "' ожидает целое число (INTEGER), но получено: " + value.getClass().getSimpleName());
                }
            }
            case "BIGINT" -> {
                if (!(value instanceof Integer || value instanceof Long)) {
                    throw new IllegalArgumentException("Колонка '" + columnName + "' ожидает большое целое число (BIGINT), но получено: " + value.getClass().getSimpleName());
                }
            }
            case "DECIMAL" -> {
                if (!(value instanceof Number)) {
                    throw new IllegalArgumentException("Колонка '" + columnName + "' ожидает число с точкой (DECIMAL), но получено: " + value.getClass().getSimpleName());
                }
            }
            case "BOOLEAN" -> {
                if (!(value instanceof Boolean)) {
                    throw new IllegalArgumentException("Колонка '" + columnName + "' ожидает логическое значение (BOOLEAN), но получено: " + value.getClass().getSimpleName());
                }
            }
            default -> throw new IllegalArgumentException("Неизвестный тип колонки: " + columnType);
        }
    }
}