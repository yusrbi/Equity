package mpi.inf.evaluator.model;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name="table")
@SessionScoped
public class Table implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String table_title ="";
	private String table_html;
	private int table_id;
	private String document_id;
	
	public String getTable_html() {
		return table_html;
	}

	public void setTable_html(String table_html) {
		this.table_html = table_html;
	}

	public int getTable_id() {
		return table_id;
	}

	public void setTable_id(int table_id) {
		this.table_id = table_id;
	}

	public String getDocument_id() {
		return document_id;
	}

	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}

	public String getTable_title() {
		return table_title;
	}

	public void setTable_title(String table_title) {
		this.table_title = table_title;
	}

}
