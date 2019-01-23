package mpi.inf.evaluator.model;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import mpi.inf.evaluator.database.DatabaseWrapper;

@ManagedBean(name="data_type")
@SessionScoped
public class DataType  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<SelectItem> all_data_types;

	public List<SelectItem> getAll_data_types() {
		DatabaseWrapper db_wrapper = DatabaseWrapper.getDatabaseWrapper();
		all_data_types = db_wrapper.getDataTyeps();
		return all_data_types;
	}
	
}
