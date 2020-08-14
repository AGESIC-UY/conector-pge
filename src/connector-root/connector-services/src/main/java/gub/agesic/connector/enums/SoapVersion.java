package gub.agesic.connector.enums;

public enum SoapVersion {
    V1_1("1.1"),
    V1_2("1.2"),
    MULTIPLE("multiple"),
    UNDEFINED("undefined");

    private String name;

    SoapVersion(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}