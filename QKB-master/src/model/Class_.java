package model;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean(name="quantity_class")
@SessionScoped
public class Class_ implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String aliases;
	private boolean is_dimensionless;

	public Class_(){
		
	}
	
	public Class_(String name, String aliases, boolean is_dimensionless) {
		this.name = name;
		this.aliases = aliases;
		this.is_dimensionless = is_dimensionless;
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAliases() {
		return aliases;
	}

	public void setAliases(String aliases) {
		this.aliases = aliases;
	}

	public boolean isIs_dimensionless() {
		return is_dimensionless;
	}

	public void setIs_dimensionless(boolean is_dimensionless) {
		this.is_dimensionless = is_dimensionless;
	}

}
