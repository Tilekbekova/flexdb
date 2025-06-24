package com.example.flexdb.enums;

public enum SupportedColumnType {
    TEXT("TEXT"),
    INTEGER("INTEGER"),
    BIGINT("BIGINT"),
    DECIMAL("NUMERIC(19,4)"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    TIMESTAMP("TIMESTAMP WITHOUT TIME ZONE");

    private final String postgresType;

    SupportedColumnType(String postgresType) {
        this.postgresType = postgresType;
    }

    public String getPostgresType() {
        return postgresType;
    }

    public static boolean isSupported(String type) {
        for (SupportedColumnType t : values()) {
            if (t.name().equalsIgnoreCase(type)) return true;
        }
        return false;
    }
}
