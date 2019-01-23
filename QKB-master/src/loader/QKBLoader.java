package loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.faces.bean.ApplicationScoped;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import model.Class_;
import model.Measure;
import model.Unit;
import utils.JSONReader;

@ManagedBean(name="qkbLoader")
@ApplicationScoped
public class QKBLoader implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String qkb_location ="resources/QKB/";
	
	Map<String,Unit> units;
	Map<String, Measure> measures;
	Map<String, Class_> classes = new HashMap<String, Class_>();
	
	
	private Unit unit;
	private Measure measure;	
	private Class_ quantity_class;
	
	public QKBLoader() throws ObjectNotFoundException, UnsupportedEncodingException, IOException, ParseException{
		units = new HashMap<String,Unit>();
		measures = new HashMap<String, Measure>();
		classes = new HashMap<String, Class_>();
		loadFromJSON();
	}

	
	private void loadFromJSON() throws ObjectNotFoundException, UnsupportedEncodingException, IOException, ParseException {
		
		ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String qkb_location_path = servletContext.getRealPath(qkb_location);
		JSONArray units_json_arr;
		JSONArray measures_json_arr;
		JSONArray classes_json_arr;
		
//		if(qkb_location_path== null || qkb_location_path.isEmpty()){		
			
			units_json_arr = JSONReader.readJsonArrayFromInputStream(
					servletContext.getResourceAsStream(qkb_location+"units.json"));
			measures_json_arr = JSONReader.readJsonArrayFromInputStream(
					servletContext.getResourceAsStream(qkb_location+"measures.json"));
			classes_json_arr = JSONReader.readJsonArrayFromInputStream(
					servletContext.getResourceAsStream(qkb_location+"classes.json"));
//		}else {
//			units_json_arr = JSONReader.readJSONArrayFromFile(qkb_location_path+"units.json");
//			measures_json_arr = JSONReader.readJSONArrayFromFile(qkb_location_path+"measures.json");
//			classes_json_arr = JSONReader.readJSONArrayFromFile(qkb_location_path+"classes.json");
//		}
		String name, key, wiki_title, class_, measure_name, wiki_id, aliases, class_name, si_conversion, symbol, data_type, fb_id;
		boolean is_dimensionless=false;
		
		if(units_json_arr==null  || measures_json_arr==null || classes_json_arr==null){
			throw new ObjectNotFoundException("Could Not Load QKB!");
		}else{
			
			
			Iterator<JSONObject> json_arr_iterator = units_json_arr.iterator();
			JSONObject json_obj ;
			Unit unit;
			while (json_arr_iterator.hasNext()){
				json_obj = json_arr_iterator.next();
				//read units data here
				name = (String) json_obj.get("name");
				key = (String) json_obj.get("key");
				class_name = (String) json_obj.get("class");
				wiki_title =(String)  json_obj.get("wiki_title");
				measure_name = (String) json_obj.get("measure");
				wiki_id = (String)  json_obj.get("wiki_id");
				aliases = (String) json_obj.get("aliases");
				aliases = aliases.replace('_', ' ');
				si_conversion = (String)  json_obj.get("si_conversion");	
				symbol = (String)  json_obj.get("symbol");
				data_type = (String) json_obj.get("data_type");
				fb_id = (String) json_obj.get("fb_id");
				unit = new Unit(name,key,class_name, wiki_title, wiki_id, measure_name,aliases,si_conversion, symbol, data_type, fb_id);
				units.put(key, unit);
			}
		
			//read dimensions/measures
			json_arr_iterator = measures_json_arr.iterator();	
			Measure measure;
			while (json_arr_iterator.hasNext()){
				json_obj = json_arr_iterator.next();				
				name = (String) json_obj.get("name");
				wiki_title = (String) json_obj.get("wiki_title");
				aliases = (String) json_obj.get("aliases");
				aliases = aliases.replace('_', ' ');
				wiki_id = (String)  json_obj.get("wiki_id");
				fb_id = (String) json_obj.get("fb_id");
				class_name = (String) json_obj.get("class");
				measure = new Measure(name, wiki_title, wiki_id, fb_id, aliases, class_name);
				measures.put(name, measure);
			}
			
			
			//read dimensionless classes
			json_arr_iterator = classes_json_arr.iterator();	
			Class_ q_class;
			while (json_arr_iterator.hasNext()){
				json_obj = json_arr_iterator.next();		
				aliases = (String) json_obj.get("aliases");
				name = (String) json_obj.get("name");
				aliases = aliases.replace('_', ' ');
				is_dimensionless = (boolean)json_obj.get("is_dimensionless");
				q_class = new Class_(name,aliases, is_dimensionless);
				classes.put(name, q_class);
			}
		
		}
		
	}






	public Unit getUnit() {
		return unit;
	}


	public void setUnit(Unit unit) {
		this.unit = unit;
	}


	public Measure getMeasure() {
		return measure;
	}


	public void setMeasure(Measure measure) {
		this.measure = measure;
	}


	public Class_ getQuantity_class() {
		return quantity_class;
	}


	public void setQuantity_class(Class_ quantity_class) {
		this.quantity_class = quantity_class;
	}


	public Unit getUnit(String unit_name) {
		
		if(units.containsKey(unit_name)){
			this.unit = units.get(unit_name);
		}else
			this.unit = null;
		return unit;
	}


	public Class_ get_quantity_class(String class_name) {
		
		if(classes.containsKey(class_name)){
			this.quantity_class = classes.get(class_name);
		}else
			this.quantity_class =null;
		return quantity_class;
	}


	public Measure getMeasure(String measure_name) {

		if(measures.containsKey(measure_name)){
			this.measure = measures.get(measure_name);
		}else
			this.measure = null;
		return measure;
	}
	
	
}
