package com.example.flexdb.repository;

import com.example.flexdb.entity.DynamicTableDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface DynamicTableDefinitionRepository extends JpaRepository<DynamicTableDefinition, Long> {
    Optional<DynamicTableDefinition> findByTableName(String tableName);
    boolean existsByTableName(String tableName);
}