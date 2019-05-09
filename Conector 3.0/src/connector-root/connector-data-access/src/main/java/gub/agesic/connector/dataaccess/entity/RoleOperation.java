package gub.agesic.connector.dataaccess.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "ROLE_OPERATION")
public class RoleOperation extends GenericEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "ROLE", length = 100)
    private String role;

    @Column(name = "OPERATION_INPUT_NAME", length = 100)
    private String operationInputName;

    @Column(name = "OPERATION_WSDL", length = 100)
    private String operationFromWSDL;

    @Column(name = "WSA_ACTION", length = 256)
    private String wsaAction;

    public RoleOperation() {
    }

    public RoleOperation(final String role, final String operationInputName,
            final String operationFromWSDL, final String wsaAction) {
        this.role = role;
        this.operationInputName = operationInputName;
        this.operationFromWSDL = operationFromWSDL;
        this.wsaAction = wsaAction;
    }

    public String getWsaAction() {
        return wsaAction;
    }

    public void setWsaAction(final String wsaAction) {
        this.wsaAction = wsaAction;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public String getOperationInputName() {
        return operationInputName;
    }

    public void setOperationInputName(final String operationInputName) {
        this.operationInputName = operationInputName;
    }

    public String getOperationFromWSDL() {
        return operationFromWSDL;
    }

    public void setOperationFromWSDL(final String operationFromWSDL) {
        this.operationFromWSDL = operationFromWSDL;
    }
}
