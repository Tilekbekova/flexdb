package com.example.flexdb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_dynamic_table_definitions")
@Getter
@Setter
@NoArgsConstructor
public class DynamicTableDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name", nullable = false, unique = true)
    private String tableName;

    @Column(name = "user_friendly_name")
    private String userFriendlyName;
    /**
     * CreationTimestamp
     * Дата и время создания записи.
     * Устанавливается автоматически при первом сохранении сущности в базу данных.
     * Аналог DEFAULT CURRENT_TIMESTAMP на уровне Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "tableDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DynamicColumnDefinition> columns = new ArrayList<>();
}
