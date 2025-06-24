package com.example.flexdb.enums;

import lombok.Getter;

@Getter
public enum SupportedColumnType {
    TEXT("TEXT"),
    INTEGER("INTEGER"),
    BIGINT("BIGINT"),
    DECIMAL("DECIMAL"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    TIMESTAMP("TIMESTAMP");

    private final String postgresType;

    SupportedColumnType(String postgresType) {
        this.postgresType = postgresType;
    }

    public static boolean isSupported(String type) {
        for (SupportedColumnType value : values()) {
            if (value.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
