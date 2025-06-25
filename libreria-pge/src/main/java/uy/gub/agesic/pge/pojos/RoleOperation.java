package uy.gub.agesic.pge.pojos;

public class RoleOperation {
    private String role;

    private String operationInputName;

    private String operationFromWSDL;

    private String wsaAction;

    private String soapVersion;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOperationInputName() {
        return operationInputName;
    }

    public void setOperationInputName(String operationInputName) {
        this.operationInputName = operationInputName;
    }

    public String getOperationFromWSDL() {
        return operationFromWSDL;
    }

    public void setOperationFromWSDL(String operationFromWSDL) {
        this.operationFromWSDL = operationFromWSDL;
    }

    public String getWsaAction() {
        return wsaAction;
    }

    public void setWsaAction(String wsaAction) {
        this.wsaAction = wsaAction;
    }

    public String getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }
}
