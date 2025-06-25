package gub.agesic.connector.dataaccess.enums;

public enum EnvironmentType {
    PRODUCTION("Produccion"),
    TESTING("Testing");

    private final String name;

    EnvironmentType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}