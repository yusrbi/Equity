package mpi.inf.evaluator.model;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;

@ManagedBean(name="unit")
@SessionScoped
public class Unit  implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;
	String name;
	String dimension;
	int wikipedia_id;
	String wikipedia_title;
	String key;
	int data_type;
	String freebase_id;
	String aliases;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDimension() {
		return dimension;
	}
	public void setDimension(String dimension) {
		this.dimension = dimension;
	}
	public int getWikipedia_id() {
		return wikipedia_id;
	}
	public void setWikipedia_id(int wikipedi_id) {
		this.wikipedia_id = wikipedi_id;
	}
	public String getWikipedia_title() {
		return wikipedia_title;
	}
	public void setWikipedia_title(String wikipedia_title) {
		this.wikipedia_title = wikipedia_title;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getData_type() {
		return data_type;
	}
	public void setData_type(int data_type) {
		this.data_type = data_type;
	}
	public String getFreebase_id() {
		return freebase_id;
	}
	public void setFreebase_id(String freebase_id) {
		this.freebase_id = freebase_id;
	}
	public String getAliases() {
		return aliases;
	}
	public void setAliases(String aliases) {
		this.aliases = aliases;
	}
}
