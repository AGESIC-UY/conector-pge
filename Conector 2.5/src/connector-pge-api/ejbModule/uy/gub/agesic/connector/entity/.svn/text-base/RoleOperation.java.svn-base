package uy.gub.agesic.connector.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(name = "ROLE_OPERATION")
public class RoleOperation extends AbstractEntity{
	private static final long serialVersionUID = 1L;
	
	@Column(name="ROLE", length=100)
	private String role;

	@Column(name="OPERATION", length=100)
	private String operation;

	@Column(name="OPERATION_WSDL", length=100)
	private String operationFromWSDL;
	
	@Column(name="WSA_ACTION", length=256)
	private String wsaAction;
	
	@Column(name="SOAP_ACTION", length=256)
	private String soapAction;
	
	public String getWsaAction() {
		return wsaAction;
	}
	public void setWsaAction(String wsaAction) {
		this.wsaAction = wsaAction;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getOperationFromWSDL() {
		return operationFromWSDL;
	}
	public void setOperationFromWSDL(String operationFromWSDL) {
		this.operationFromWSDL = operationFromWSDL;
	}
	public String getSoapAction() {
		return soapAction;
	}
	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}
	public String toString(){
		return role + "- " + wsaAction + "- " + operation + "- " + operationFromWSDL; 
	}
}
