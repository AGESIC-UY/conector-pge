package gub.agesic.connector.pojo;

import gub.agesic.connector.enums.SoapVersion;

public class SoapVersionInfo {
    private String prefix;
    private SoapVersion version;

    public SoapVersionInfo() {
        this.prefix = SoapVersion.UNDEFINED.getName();
        this.version = SoapVersion.UNDEFINED;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public SoapVersion getVersion() {
        return version;
    }

    public void setVersion(SoapVersion version) {
        this.version = version;
    }
}
