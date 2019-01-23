package controller;

import java.io.Serializable;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import model.InvalidQuantityException;
import model.Quantity;


@ManagedBean(name="quantity_controler")
@SessionScoped
public class QuantityController implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean empty_content= true;
	
	public boolean isEmpty_content() {
		return empty_content;
	}


	public void setEmpty_content(boolean empty_content) {
		this.empty_content = empty_content;
	}


	@ManagedProperty(value="#{quantity}")
	private Quantity quantity;
	
	
	public void setQuantity(Quantity quantity) {
		this.quantity = quantity;
	}


	public Quantity getQuantity() {
		return quantity;
	}


	public String load_quantity(){
		
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		Map<String, String> parameterMap = (Map<String, String>) ec.getRequestParameterMap();
		if(parameterMap == null || 
				(!parameterMap.containsKey("class")&&
				!parameterMap.containsKey("measure")&&
				!parameterMap.containsKey("unit") &&
				!parameterMap.containsKey("value")
				)
		){
			empty_content=true;
		}else{
			empty_content = false;
			String class_ = parameterMap.get("class");
			String measure = parameterMap.get("measure");
			String unit = parameterMap.get("unit");
			String value = parameterMap.get("value");
			quantity.init(class_,measure,unit,value);
		}
		return "";//class/"+quantity.getClass_()+"/measure/"+ quantity.getMeasure().getName() 
		//+"/unit/"+quantity.getUnit().getName()+"/value/"+quantity.getValue();
	}

}
