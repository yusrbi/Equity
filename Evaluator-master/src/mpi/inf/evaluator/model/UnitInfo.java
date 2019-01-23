package mpi.inf.evaluator.model;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;

@ManagedBean(name="unit_info")
@SessionScoped
public class UnitInfo  implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String unit_name;
	String dimension_name;
	String unit_wiki_title;
	String _class;	
	String unit_fb_id;
	String dimension_wiki_title;
	String dimension_fb_id;
	public String getUnit_name() {
		return unit_name;
	}
	public void setUnit_name(String unit_name) {
		this.unit_name = unit_name;
	}
	public String getDimension_name() {
		return dimension_name;
	}
	public void setDimension_name(String dimension_name) {
		this.dimension_name = dimension_name;
	}
	public String getUnit_wiki_title() {
		return unit_wiki_title;
	}
	public void setUnit_wiki_title(String unit_wiki_title) {
		this.unit_wiki_title = unit_wiki_title;
	}
	public String get_class() {
		return _class;
	}
	public void set_class(String _class) {
		this._class = _class;
	}
	public String getUnit_fb_id() {
		return unit_fb_id;
	}
	public void setUnit_fb_id(String unit_fb_id) {
		this.unit_fb_id = unit_fb_id;
	}
	public String getDimension_wiki_title() {
		return dimension_wiki_title;
	}
	public void setDimension_wiki_title(String dimension_wiki_title) {
		this.dimension_wiki_title = dimension_wiki_title;
	}
	public String getDimension_fb_id() {
		return dimension_fb_id;
	}
	public void setDimension_fb_id(String dimension_fb_id) {
		this.dimension_fb_id = dimension_fb_id;
	}
	}
