package uy.gub.agesic.connector.web;

import javax.faces.context.FacesContext;

public class ContextHelpBean {

	private String helpViewPath;
	
	public String getHelpViewPath() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String viewId = facesContext.getViewRoot().getViewId();
		
		helpViewPath = "/help/" + getViewId(viewId) + "Help.xhtml";
		return helpViewPath;
	}
	
	public void setHelpViewPath(String path) {
		helpViewPath = path;
	}
	
	private String getViewId(String viewPath) {
		String[] pathParts = viewPath.split("/");
		String viewWithExtension = pathParts[pathParts.length-1];
		return viewWithExtension.substring(0, viewWithExtension.length() - 6);
		
	}
	
}
