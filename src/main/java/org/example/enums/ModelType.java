package org.example.enums;

public enum ModelType {
    USER("U"),
    PROJECT("P"),
    TASK("T"),
    STATUS_REPORT("SR");

    private final String prefix;

    ModelType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
