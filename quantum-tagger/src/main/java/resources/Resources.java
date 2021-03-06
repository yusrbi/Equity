package resources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import edu.stanford.nlp.util.Pair;
import knowledgebase.Unit;
import knowledgebase.Units_Measures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import utils.DatabaseAccess;
import utils.FileUtils;
import utils.JSONReader;
import lsh.Common;
import lsh.LSHTable;

public class Resources {
	private String units_file_name = "";
	private static Resources resources = null;
	private String stanford_ner_result = "";
	private LSHTable lsh_categories;
	private LSHTable lsh_concepts;
	Logger s_logger_ = LoggerFactory.getLogger(Resources.class);
	private String categories_file;
	private String concepts_file;
	private String graph_save_dir;
	private String db_connectin_url;
	private String db_user;
	private String db_password;
	private double rwr_gamma; // (1-gamma) is the probability of the restart
	private double rwr_alpha; // alpha is the fraction of times to do a random
								// jump , for making the matrix irreducible
	private int rwr_max_itr;
	private String html_results_path;
	private String html_template_path;
	private Double header_count_fraction;
	private boolean general_relatedness;
	private String numbers_file;
	private Set<String> numbers;
	private boolean NER_switch;
	private boolean context_switch;

	private String qkb_location;
	private String qkb_url;
	
	
	public Set<String> getNumbers() {
		return numbers;
	}

	public void setNumbers_file(String numbers_file) {
		this.numbers_file = numbers_file;
	}

	public boolean is_general_relatedness() {
		return general_relatedness;
	}

	private Resources() {

	}

	public String getUnits_file_name() {
		return units_file_name;
	}

	public void setUnits_file_name(String units_file_name) {
		this.units_file_name = units_file_name;
	}

	public void loadUnits() throws SQLException {
		DatabaseAccess.getDatabaseAccess().load_units();
	}
	// public void loadUnits() throws IOException {
	// BufferedReader reader=null ;
	// String line ;
	// units_abbreviations = new LinkedHashSet<String>();
	// units_aliases = new LinkedHashSet<String>();
	// Unit unit =null;
	// Units_Measures units = Units_Measures.getUnits_Measures();
	// try{
	// reader = new BufferedReader(
	// new InputStreamReader(
	// new FileInputStream(units_file_name), "UTF8"));
	// while ((line = reader.readLine())!= null){
	// line = line.toLowerCase();
	// String[] parts = line.split("\t",-1);
	// if(parts.length <4)
	// continue;
	// String freebaseid = parts[0].trim();
	// String unit_key = parts[1].trim();
	// String[] unit_aliases = parts[2].trim().split(",");
	// String[] unit_abbreviations = parts[3].trim().split(",");
	// String measure = parts[4].trim();
	// String[] unit_wiki_links = null;
	// String unit_wiki_title = null;
	// if(parts.length >6 ){
	// unit_wiki_links = parts[5].trim().split(",");
	// unit_wiki_title = parts[6].trim();
	// }
	// unit = new
	// Unit(freebaseid,unit_key,unit_aliases,unit_abbreviations,measure,
	// unit_wiki_links, unit_wiki_title);
	// units_abbreviations.addAll(Arrays.asList(unit_abbreviations));
	// units_aliases.addAll(Arrays.asList(unit_aliases));
	// units.addUnit(unit);
	// }
	// }catch(Exception e){
	// s_logger_.error(e.getMessage());
	// e.printStackTrace();
	// }finally{
	// if(reader!=null)
	// reader.close();
	// }
	//
	// }

	public static Resources getResources() {
		if (resources == null) {
			resources = new Resources();
		}
		return resources;
	}

	public void setStanford_ner_result(String stanford_ner_result) {
		this.stanford_ner_result = stanford_ner_result;

	}

	public Map<String, String> loadStanfordNerResult() {

		return null;
	}

	public void setConceptsFile(String file_name) {
		this.concepts_file = file_name;

	}

	public void setCategoriesFiel(String file_name) {
		this.categories_file = file_name;

	}

	public void load() throws SQLException, IOException {
		loadNumbers();
		loadQKB();
		loadCategories();
		loadConcepts();		
		loadTermExpansion();
		loadStatisticalModifiers();

	}

	private void loadQKB() throws SQLException {
		
		loadQKBFromJSON();		
	}
	
	@SuppressWarnings("unchecked")
	private void loadQKBFromJSON() throws SQLException {
		JSONArray units_json_arr = JSONReader.readJSONArrayFromFile(qkb_location+"units.json");
		JSONArray measures_json_arr = JSONReader.readJSONArrayFromFile(qkb_location+"measures.json");
		JSONArray classes_json_arr = JSONReader.readJSONArrayFromFile(qkb_location+"classes.json");
		String name, key, wiki_title, class_, dimension, dimension_wiki_title, aliases;
	
		Unit unit;
		Units_Measures units_measures = Units_Measures.getUnits_Measures();
		//TODO change to Multimap 
		Map<String, Pair<String, String>> dimensions = new HashMap<String, Pair<String, String>>();
		Multimap<String, String> units_aliases;
		units_aliases =  HashMultimap.create();
		if(units_json_arr==null  || measures_json_arr==null || classes_json_arr==null){
			loadUnits();
			loadDimensionlessClasses();
			loadDimensions();
			loadClasses();
		}else{
			Iterator<JSONObject> json_arr_iterator = units_json_arr.iterator();
			JSONObject json_obj ;
			while (json_arr_iterator.hasNext()){
				json_obj = json_arr_iterator.next();
				//read units data here
				name = (String) json_obj.get("name");
				key = (String) json_obj.get("key");
				class_ = (String) json_obj.get("class");
				wiki_title =(String)  json_obj.get("wiki_title");
				dimension = (String) json_obj.get("measure");
				dimension_wiki_title = (String)  json_obj.get("measure_wiki_title");
				aliases = (String) json_obj.get("aliases");
				aliases = aliases.replace('_', ' ');
				if (!aliases.isEmpty()){
					for(String alias : aliases.split(",")){
						units_aliases.put(alias.toLowerCase().trim(), key);
					}
				}
				unit = new Unit(name, key, wiki_title, class_, dimension, dimension_wiki_title);
				units_measures.addUnit(unit);				
			}
			units_measures.setAliases(units_aliases);
			//read dimensions/measures
			json_arr_iterator = measures_json_arr.iterator();
			Pair<String,String> dimension_SemanticId;
			while (json_arr_iterator.hasNext()){
				json_obj = json_arr_iterator.next();				
				dimension = (String) json_obj.get("name");
				wiki_title = (String) json_obj.get("wiki_title");
				aliases = (String) json_obj.get("aliases");
				aliases = aliases.replace('_', ' ');
				dimension_SemanticId = new Pair<String, String>(dimension, wiki_title);
				if(!aliases.isEmpty()){
					for(String alias : aliases.split(",")){
						dimensions.put(alias.toLowerCase().trim(), dimension_SemanticId);
					}
				}
				//add by name
				dimensions.put(dimension, dimension_SemanticId);				
			}
			units_measures.setDimensions(dimensions);
			//read dimensionless classes
			json_arr_iterator = classes_json_arr.iterator();
			Set<String> dimensionless_classes = new HashSet<String>();
			Map<String, String> classes = new HashMap<String, String>();
			while (json_arr_iterator.hasNext()){
				json_obj = json_arr_iterator.next();		
				aliases = (String) json_obj.get("aliases");
				aliases = aliases.replace('_', ' ');
				if((boolean)json_obj.get("is_dimensionless")){
					dimensionless_classes.add((String)json_obj.get("name"));
				}
				if(!aliases.isEmpty()){
					for(String alias : aliases.split(",")){
						classes.put(alias.toLowerCase().trim(), (String)json_obj.get("name"));
					}
				}
			}
			Units_Measures.setDimensionless_classes(dimensionless_classes);
			units_measures.setClasses(classes);
		}
		
	}
	

	

	private void loadTermExpansion() throws SQLException {
		DatabaseAccess.getDatabaseAccess().load_term_expansion();

	}

	private void loadNumbers() throws IOException {
		this.numbers = FileUtils.readFile(this.numbers_file);

	}

	private void loadStatisticalModifiers() throws SQLException {
		DatabaseAccess.getDatabaseAccess().load_statistical_modifiers();

	}

	private void loadClasses() throws SQLException {
		DatabaseAccess.getDatabaseAccess().load_classes();

	}

	private void loadDimensionlessClasses() throws SQLException {
		DatabaseAccess.getDatabaseAccess().load_dimensionless_classes();

	}

	private void loadDimensions() throws SQLException {
		DatabaseAccess.getDatabaseAccess().load_dimensions();

	}

	private void loadConcepts() throws IOException {
		BufferedReader reader = null;
		// StringEscapeUtils escapeUtils = new StringEscapeUtils();
		String line;
		lsh_concepts = new LSHTable(2, 15, 100, 999999999, 0.5);
		try {
			reader = new BufferedReader(new FileReader(concepts_file));
			while ((line = reader.readLine()) != null) {
				line = line.toLowerCase();
				if (line.isEmpty())
					continue;
				// String key = line;
				String concept = URLDecoder.decode(line.trim().replace('_', ' '), "utf-8");
				lsh_concepts.put(Common.getCounter(concept));// 363814
			}

		} catch (Exception e) {
			s_logger_.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}

	}

	private void loadCategories() throws IOException {
		BufferedReader reader = null;
		String line;
		// Set<String> categories = new HashSet<String>();
		lsh_categories = new LSHTable(5, 12, 100, 999999999, 0.5);
		try {
			reader = new BufferedReader(new FileReader(categories_file));
			while ((line = reader.readLine()) != null) {
				line = line.toLowerCase();
				String[] content = line.split("\t");
				if (content.length < 2)
					continue;
				// String key = content[0];
				String category = URLDecoder.decode(content[1].replace('_', ' '), "utf-8");
				lsh_categories.put(Common.getCounter(category)); // 475674
				// categories.add(category);
			}

		} catch (Exception e) {
			s_logger_.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	public LSHTable getLSHClasses() {
		//
		return lsh_categories;
	}

	public LSHTable getLSHConcepts() {
		//
		return lsh_concepts;
	}

	public void setGraphSaveDir(String property) {
		this.graph_save_dir = property;

	}

	public String getgraph_save_dir() {
		return this.graph_save_dir;
	}

	public String getStanford_ner_result() {
		return stanford_ner_result;
	}

	public void setDBConnectionURL(String property) {
		this.db_connectin_url = property;

	}

	public String getDb_connectin_url() {
		return db_connectin_url;
	}

	public void setDBUser(String property) {
		this.db_user = property;

	}

	public void setDBPassword(String property) {
		this.db_password = property;

	}

	public String getDb_user() {
		return db_user;
	}

	public String getDb_password() {
		return db_password;
	}

	public void setRWRGamma(double property) {

		this.rwr_gamma = property;

	}

	public double getRwr_gamma() {
		return rwr_gamma;
	}

	public void setRWRMaxItr(int itr) {
		this.rwr_max_itr = itr;

	}

	public int getRwr_max_itr() {
		return rwr_max_itr;
	}

	public void setHTMLResultsPath(String property) {
		this.html_results_path = property;
	}

	public String getHtml_results_path() {
		return html_results_path;
	}

	public void setHTMLTemplate(String property) {
		this.html_template_path = property;

	}

	public String getHtml_template_path() {
		return html_template_path;
	}

	public void setRWRAlpha(String property) {
		this.rwr_alpha = Double.valueOf(property);
	}

	public double getAlpha() {
		// TODO Auto-generated method stub
		return rwr_alpha;
	}

	public void setHeaderCount(Double val) {
		this.header_count_fraction = val;

	}

	public Double getHeader_count_fraction() {
		return header_count_fraction;
	}

	public void setGeneral_relatedness(boolean val) {
		this.general_relatedness = val;
	}

	public void set_NER_switch(String key) {
		if (key.equals("on")) {
			this.NER_switch = true;
		} else {
			this.NER_switch = false;
		}

	}

	public void set_context_switch(String key) {
		if (key.equals("on")) {
			this.context_switch = true;
		} else {
			this.context_switch = false;
		}

	}

	public boolean isNER_switch() {
		return NER_switch;
	}

	public boolean isContext_switch() {
		return context_switch;
	}

	public void setQKBLocation(String qkb_location) {
		this.qkb_location = qkb_location;
		
	}
	public String getQkb_url() {
		return qkb_url;
	}

	public void setQkb_url(String qkb_url) {
		this.qkb_url = qkb_url;
	}

}
