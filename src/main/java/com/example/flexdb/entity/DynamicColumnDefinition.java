package com.example.flexdb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_dynamic_column_definitions")
@Getter
@Setter
@NoArgsConstructor
public class DynamicColumnDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_definition_id", nullable = false)
    private DynamicTableDefinition tableDefinition;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "column_type", nullable = false)
    private String columnType;

    @Column(name = "postgres_column_type", nullable = false)
    private String postgresColumnType;

    @Column(name = "is_nullable", nullable = false)
    private boolean isNullable = true;

    @Column(name = "is_primary_key_internal", nullable = false)
    private boolean isPrimaryKeyInternal = false;
    /**
     * CreationTimestamp
     * Дата и время создания записи.
     * Устанавливается автоматически при первом сохранении сущности в базу данных.
     * Аналог DEFAULT CURRENT_TIMESTAMP на уровне Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
