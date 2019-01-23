package mpi.inf.evaluator.model;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import mpi.inf.evaluator.database.DatabaseWrapper;
@ManagedBean(name="dimension")
@SessionScoped
public class Dimension  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<SelectItem> all_dimensions;

	public List<SelectItem> getAll_dimensions() {
		
		DatabaseWrapper db_wrapper = DatabaseWrapper.getDatabaseWrapper();
		all_dimensions = db_wrapper.getDimensions();
		return all_dimensions;
	}
	
	
	
}
