package uy.gub.agesic.pge.enums;

public enum SamlVersion {
    V1_1("1.1"),
    V2_0("2.0");

    private final String name;

    SamlVersion(String name) {
        this.name = name;
    }

    public static SamlVersion fromString(String name) {
        for (SamlVersion sv : SamlVersion.values()) {
            if (sv.name.equalsIgnoreCase(name)) {
                return sv;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
