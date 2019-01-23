package mpi.inf.evaluator.model;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name="document")
@SessionScoped
public class Document implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	private String url;
	private String id;
	private String content_html;
	private String experiment_id;
	
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setContent_html(String content_html) {
		this.content_html = content_html;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getId() {
		return id;
	}

	public String getContent_html() {
		return content_html;
	}

	public void setExperiment_id(String experiment_id) {
		this.experiment_id = experiment_id;
		
	}

	public String getExperiment_id() {
		return experiment_id;
	}
	
}
