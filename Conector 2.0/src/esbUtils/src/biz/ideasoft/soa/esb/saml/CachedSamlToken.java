package biz.ideasoft.soa.esb.saml;

import java.util.Date;

public class CachedSamlToken {

	private String token;
	private Date notBefore;
	private Date notOnOrAfter;
	
	public CachedSamlToken() {
		
	}
	
	public CachedSamlToken(String token, Date notBefore, Date notOnOrAfter) {
	
		this.token = token;
		this.notBefore = notBefore;
		this.notOnOrAfter = notOnOrAfter;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public Date getNotBefore() {
		return notBefore;
	}
	
	public void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}
	
	public Date getNotOnOrAfter() {
		return notOnOrAfter;
	}
	
	public void setNotOnOrAfter(Date notOnOrAfter) {
		this.notOnOrAfter = notOnOrAfter;
	}
		
}
