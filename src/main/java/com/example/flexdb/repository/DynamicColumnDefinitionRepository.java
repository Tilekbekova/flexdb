package com.example.flexdb.repository;

import com.example.flexdb.entity.DynamicColumnDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface DynamicColumnDefinitionRepository extends JpaRepository<DynamicColumnDefinition, Long> {
}