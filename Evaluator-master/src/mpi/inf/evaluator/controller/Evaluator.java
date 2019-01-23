package mpi.inf.evaluator.controller;


import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import mpi.inf.evaluator.database.DatabaseWrapper;
import mpi.inf.evaluator.model.DataType;
import mpi.inf.evaluator.model.Dimension;
import mpi.inf.evaluator.model.Document;
import mpi.inf.evaluator.model.Table;
import mpi.inf.evaluator.model.Unit;
import mpi.inf.evaluator.model.User;

@ManagedBean(name="evaluator")
@SessionScoped
public class Evaluator implements Serializable {

	public Unit getUnit() {
		return unit;
	}
	public void setUnit(Unit unit) {
		this.unit = unit;
	}
	public Dimension getDimension() {
		return dimension;
	}
	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}
	public DataType getData_type() {
		return data_type;
	}
	public void setData_type(DataType data_type) {
		this.data_type = data_type;
	}
	@ManagedProperty(value="#{document}")
	private Document document;
	
	@ManagedProperty(value="#{table}")
	private Table table;
	
	@ManagedProperty(value="#{user}")
	private User user;
	
	@ManagedProperty(value="#{experiement}")
	private String experiement_id;
	
	@ManagedProperty(value="#{unit}")
	private Unit unit;
	
	@ManagedProperty(value="#{dimension}")
	private Dimension dimension; 
	
	@ManagedProperty(value="#{data_type}")
	private DataType data_type; 
	
	private List<String> experiments;
	
	private boolean empty_content= false;
	
	private static final long serialVersionUID = 1L;

	public void init()  {
		load_data();
	}
	public String submit() throws ClassNotFoundException {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		int annotation_id =0, count_table =0, count_doc =0, count_input_doc=0, count_input_table =0;
		StringBuilder evaluation_table = new StringBuilder();
		StringBuilder evaluation_doc = new StringBuilder();
		StringBuilder input_table = new StringBuilder();
		StringBuilder input_doc = new StringBuilder();
		String[] temp;
		String result;
		String input;
		String other_field_id;
		String other_field_content= null;
		Boolean evaluation= false;
		String gold_standard =null;
	    for(String field : ec.getRequestParameterMap().keySet()){
	    	if(field.startsWith("hdn_")){
	    		result = ec.getRequestParameterMap().get(field);
	    		temp = field.split("_");
	    		if(temp == null || temp.length != 3)
	    			continue;
	    		//get the cmb input value
	    		other_field_id = "cmb"+field.substring(3);
	    		input =  ec.getRequestParameterMap().get(other_field_id);
	    		if(input == null || input.trim().isEmpty()){
	    			//get txt input 
	    			other_field_id = "txt"+field.substring(3);
		    		input =  ec.getRequestParameterMap().get(other_field_id);	    			
	    		}    		
	    		if(input == null || input.trim().isEmpty()){
	    			gold_standard = result;
	    		}else
	    			gold_standard = input;	    		
	    		
	    		if(temp[1].equals("doc")){
	    			//doc
	    			count_doc++;
	    			if(gold_standard.equals(result)){
	    				evaluation_doc.append("\""+document.getId()+"\","
	    		    			+annotation_id+",\""+document.getExperiment_id()+"\",1,\""
	    	    						+this.user.getUser_name()+"\"\n");
	    	    				
	    			} else{
	    				evaluation_doc.append("\""+document.getId()+"\","
	    		    			+annotation_id+",\""+document.getExperiment_id()+"\",0,\""
	    	    						+this.user.getUser_name()+"\"\n");
	    	    				
	    			}    			
	    			//append to doc gold standards    				
    				count_input_doc++;
    				input_doc.append("\""+document.getId()+"\","
	    			+annotation_id+",\""+document.getExperiment_id()+"\",\"" +gold_standard 
	    			+"\",\""+this.user.getUser_name()+"\"\n");
	    			
	    		}else{
	    			//table
	    			//append to table evaluation
	    			count_table++;
	    			if(gold_standard.equals(result)){
	    				evaluation_table.append(table.getTable_id()+",\""+document.getId()+"\","
				    			+annotation_id+",\""+document.getExperiment_id()+"\",1,\""
		    					+this.user.getUser_name()+"\"\n");
	    			}else{
	    				evaluation_table.append(table.getTable_id()+",\""+document.getId()+"\","
				    			+annotation_id+",\""+document.getExperiment_id()+"\",0,\""
		    					+this.user.getUser_name()+"\"\n");
	    			}	    			
	    			//append to table gold standards
	    			count_input_table++;
	    			input_table.append(table.getTable_id()+",\""+document.getId()+"\","
			    			+annotation_id+",\""+document.getExperiment_id()+"\",\"" +gold_standard
			    			+"\",\""+this.user.getUser_name()+"\"\n");
	    		}
	    		
	    	}
	    /*	if(field.startsWith("txt_") 
	    			|| field.startsWith("cmb_")){
	    		temp = field.split("_");
	    		if(temp != null && temp.length ==3){
	    			annotation_id = Integer.valueOf(temp[2]);
	    			input = ec.getRequestParameterMap().get(field);
	    			if(input.trim().isEmpty()){
	    				//add correct annotation
	    				//check on the other cmb/txt
	    				if(temp[0].equals("txt")){
	    					other_field_id = "cmb" +field.substring(3);
	    				}else{
	    					other_field_id = "txt" +field.substring(3);
	    				}
	    				if(ec.getRequestParameterMap().containsKey(other_field_id)){
	    					other_field_content = ec.getRequestParameterMap().get(other_field_id);
	    				}else{
	    					other_field_content="";
	    				}
	    				if(other_field_content.trim().isEmpty()){
	    					//add correct evaluation and add the result from the DB?
	    					// you have to store the gold annotations of the correct ones as well
	    					
	    				}
	    			}	
	    			if(temp[1].equals("doc")){
	    				//append to document evaluation
	    				count_doc++;
	    				evaluation_doc.append("\""+document.getId()+"\","
		    			+annotation_id+",\""+document.getExperiment_id()+"\",0,\""
	    						+this.user.getUser_name()+"\"\n");
	    				
		    			//append to doc gold standards    				
	    				count_input_doc++;
	    				input_doc.append("\""+document.getId()+"\","
		    			+annotation_id+",\""+document.getExperiment_id()+"\",\"" +input 
		    			+"\",\""+this.user.getUser_name()+"\"\n");
		    		}else{
		    			//append to table evaluation
		    			count_table++;
		    			evaluation_table.append(table.getTable_id()+",\""+document.getId()+"\","
				    			+annotation_id+",\""+document.getExperiment_id()+"\",0,\""
		    					+this.user.getUser_name()+"\"\n");
		    			//append to table gold standards
		    			count_input_table++;
		    			input_table.append(table.getTable_id()+",\""+document.getId()+"\","
				    			+annotation_id+",\""+document.getExperiment_id()+"\",\"" +input
				    			+"\",\""+this.user.getUser_name()+"\"\n");
		    		}
	    		}
	    	}*/
	    	
	    	/*if(field.startsWith("cb_")){
	    		temp = field.split("_");
	    		if(temp != null && temp.length ==3){
	    			annotation_id = Integer.valueOf(temp[2]);
	    			result = (ec.getRequestParameterMap().get(field).equals("Right")?"1":"0");
	    			if(temp[1].equals("doc")){
		    			//append to doc 	    				
	    				count_doc++;
	    				evaluation_doc.append("\""+document.getId()+"\","
		    			+annotation_id+",\""+document.getExperiment_id()+"\"," +result 
		    			+",\""+this.user.getUser_name()+"\"\n");
		    		}else{
		    			//append to table
		    			count_table++;
		    			evaluation_table.append(table.getTable_id()+",\""+document.getId()+"\","
				    			+annotation_id+",\""+document.getExperiment_id()+"\"," +result
				    			+",\""+this.user.getUser_name()+"\"\n");
		    		}
	    		}
	    		
	    	}else 
	    	else if(field.startsWith("cmb_")){
	    		temp = field.split("_");
	    		if(temp != null && temp.length ==3){
	    			annotation_id = Integer.valueOf(temp[2]);
	    			input = ec.getRequestParameterMap().get(field);
	    			if(input.trim().isEmpty())
	    				continue;
	    			if(temp[1].equals("doc")){
		    			//append to doc 	    				
	    				count_input_doc++;
	    				input_doc.append("\""+document.getId()+"\","
		    			+annotation_id+",\""+document.getExperiment_id()+"\",\"" +input 
		    			+"\",\""+this.user.getUser_name()+"\"\n");
		    		}else{
		    			//append to table
		    			count_input_table++;
		    			input_table.append(table.getTable_id()+",\""+document.getId()+"\","
				    			+annotation_id+",\""+document.getExperiment_id()+"\", \"" +input
				    			+"\",\""+this.user.getUser_name()+"\"\n");
		    		}
	    		}
	    	}*/
	    }	    
	    DatabaseWrapper db_wrapper = DatabaseWrapper.getDatabaseWrapper();
	    db_wrapper.submitTableEvaluation(evaluation_table, count_table);
	    db_wrapper.submitDocumentEvaluation(document.getId(), evaluation_doc, count_doc);
	    db_wrapper.submitTableGoldStandards(input_table, count_input_table);
	    db_wrapper.submitDocumentGoldStandards(document.getId(), input_doc, count_input_doc);
		load_data();
	    return "main";
	}

	public String skip()  {
		
			DatabaseWrapper db_wrapper = DatabaseWrapper.getDatabaseWrapper();
			db_wrapper.remove_assignment(table.getTable_id(), document.getId(),user.getUser_name());
			db_wrapper.skip(table.getTable_id(), document.getId(),user.getUser_name());
			load_data();
		
		return "main";
	}
	private void load_data(){
		try {
			DatabaseWrapper db_wrapper = DatabaseWrapper.getDatabaseWrapper();
			db_wrapper.load_document(table,document, user.getUser_name());
			if(!document.getTitle().isEmpty()){
				empty_content = false;
				db_wrapper.assign(table.getTable_id(), document.getId(),user.getUser_name());
				//load experiments here as links 
			}else{
				empty_content = true;
				//display no more tables available 
			}
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String skip_close() {
		DatabaseWrapper db_wrapper = DatabaseWrapper.getDatabaseWrapper();
		db_wrapper.remove_assignment(table.getTable_id(), document.getId(),user.getUser_name());
		return "login";
	}
	public String checkLogin()  {
	    if (user.getUser_name()== null || user.getUser_name().isEmpty()) {
	        return "login";
	    }
	    return null;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public Table getTable() {
		return table;
	}
	public void setTable(Table table) {
		this.table = table;
	}

	public String login( ){
		if(user.getUser_name()== null || user.getUser_name().isEmpty()){
			return "login";
		}
		init();
		return "main";
	}
	public String getExperiement_id() {
		return experiement_id;
	}
	public void setExperiement_id(String experiement_id) {
		this.experiement_id = experiement_id;
	}	
	public List<String> getExperiments() {
		return experiments;
	}
	public void setExperiments(List<String> experiments) {
		this.experiments = experiments;
	}
	public boolean isEmpty_content() {
		return empty_content;
	}
	public void setEmpty_content(boolean empty_content) {
		this.empty_content = empty_content;
	}
	public String addUnit(){
		DatabaseWrapper db_wrapper = DatabaseWrapper.getDatabaseWrapper();
		db_wrapper.add_unit(unit);
		return null;
	}
}
