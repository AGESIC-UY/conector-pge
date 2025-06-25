package gub.agesic.connector.enums;

public enum SamlVersion {
    V1_1("1.1"),
    V2_0("2.0");

    private final String name;

    SamlVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
