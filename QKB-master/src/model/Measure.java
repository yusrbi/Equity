package model;

import java.io.Serializable;


import javax.faces.bean.ManagedBean;

import javax.faces.bean.SessionScoped;



@ManagedBean(name="measure")
@SessionScoped
public class Measure  implements Serializable {

	/**
	 * 
	 */

	
	private static final long serialVersionUID = 1L;
	private String name;
	private String wikipedia_id;
	private String wikipedia_title;
	private String freebase_id;
	private String aliases;
	private String class_name;

	
	public Measure() {
	
	}
	public Measure(String name, String wiki_title, String wiki_id, String fb_id, String aliases, String class_name) {
		this.name = name;
		this.wikipedia_title = wiki_title;
		this.wikipedia_id =wiki_id;
		this.freebase_id = fb_id;
		this.aliases =aliases;
		this.class_name = class_name;
	}

	public String getClass_name() {
		return class_name;
	}
	public void setClass_name(String class_name) {
		this.class_name = class_name;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWikipedia_id() {
		return wikipedia_id;
	}

	public void setWikipedia_id(String wikipedia_id) {
		this.wikipedia_id = wikipedia_id;
	}

	public String getWikipedia_title() {
		return wikipedia_title;
	}

	public void setWikipedia_title(String wikipedia_title) {
		this.wikipedia_title = wikipedia_title;
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
