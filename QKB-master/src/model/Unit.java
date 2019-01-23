package model;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;

import javax.faces.bean.SessionScoped;

@ManagedBean(name="unit")
@SessionScoped
public class Unit  implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int id;
	private String name;
	private String measure;
	private String class_name;
	private String wikipedia_id;
	private String wikipedia_title;
	private String key;
	private String data_type;
	private String freebase_id;
	private String aliases;
	private String symbol;
	private String si_conversion;

	public Unit() {
		
	}
	
	public Unit(String name, String key, String class_name, String wiki_title, 
			String wiki_id, String measure, String aliases, String si_conversion, String symbol,
			String data_type, String fb_id) {
		this.name = name;
		this.key = key;
		this.measure = measure;
		this.class_name = class_name;
		this.aliases = aliases;
		this.wikipedia_title = wiki_title;
		this.wikipedia_id = wiki_id;
		this.data_type = data_type;
		this.freebase_id = fb_id;
		this.symbol = symbol;
		this.si_conversion = si_conversion;
	}


	
	public String getClass_name() {
		return class_name;
	}

	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSi_conversion() {
		return si_conversion;
	}

	public void setSi_conversion(String si_conversion) {
		this.si_conversion = si_conversion;
	}

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
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public String getWikipedia_id() {
		return wikipedia_id;
	}
	public void setWikipedia_id(String wikipedi_id) {
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
	public String getData_type() {
		return data_type;
	}
	public void setData_type(String data_type) {
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
