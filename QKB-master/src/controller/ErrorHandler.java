package controller;



import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class ErrorHandler {

	public String getStatusCode(){
		String val = String.valueOf((Integer)FacesContext.getCurrentInstance().getExternalContext().
				getRequestMap().get("javax.servlet.error.status_code"));
		return val;
	}

	public String getMessage(){
		String val =  (String)FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.message");
		return val;
	}

	public String getExceptionType(){
		String val ="";
		try{
		 val = FacesContext.getCurrentInstance().getExternalContext().
			getRequestMap().get("javax.servlet.error.exception_type").toString();
		}catch(Exception exc){
			
		}
		return val;
	}

	public String getException(){
		String val = "";
		try {
			val = (String) ((Exception) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
					.get("javax.servlet.error.exception")).toString();
		} catch (Exception exc) {

		}
		return val;
	}

	public String getRequestURI() {
		String val = "";
		try {
			val = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
					.get("javax.servlet.error.request_uri");
		} catch (Exception exc) {

		}
		return val;
	}

	public String getServletName() {
		String val = "";
		try {

			val = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
					.get("javax.servlet.error.servlet_name");
		} catch (Exception exc) {

		}
		return val;
	}

}