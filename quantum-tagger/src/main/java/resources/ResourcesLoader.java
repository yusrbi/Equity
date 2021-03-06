package resources;

import graph.Edge;
import graph.GraphBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import knowledgebase.Classes;
import knowledgebase.Concepts;
import knowledgebase.Units_Measures;
import webservice.model.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotators.AnnotationEnrichment;
import executer.Hyperparams;
import executer.Hyperparams.EXPERIMENT_ID;
import executer.Hyperparams.SETTINGS_ID;

public class ResourcesLoader {

	Logger s_logger_ = LoggerFactory.getLogger(ResourcesLoader.class);
	
	public Resources load() throws IOException {
		return load("resources/");
	}
	
	public Resources load(String resources_path) throws IOException {
		InputStream in_stream = null;
		Resources resources = Resources.getResources();
		try {
			Properties prop = new Properties();
			String resources_file_name = "resources.properties";
			in_stream = getClass().getClassLoader().getResourceAsStream(resources_file_name);
			if (in_stream != null) {
				prop.load(in_stream);
//				String units_file_name = prop.getProperty("units_file");
//				String stanford_ner_result = prop.getProperty("stanford_ner_result_saved");
//				resources.setUnits_file_name(units_file_name);
//				resources.setStanford_ner_result(stanford_ner_result);
				resources.setConceptsFile(resources_path + prop.getProperty("concepts_file"));
				resources.setCategoriesFiel(resources_path+prop.getProperty("categories_file"));
				resources.setGraphSaveDir(prop.getProperty("graph_save_dir"));
				resources.setDBConnectionURL(prop.getProperty("db_connection_url"));
				resources.setDBUser(prop.getProperty("db_user"));
				resources.setDBPassword(prop.getProperty("db_password"));
				resources.setRWRGamma(Double.parseDouble(prop.getProperty("rwr_gamma")));
				resources.setRWRMaxItr(Integer.parseInt(prop.getProperty("rwr_max_itr")));
				resources.setHTMLResultsPath(prop.getProperty("html_results_dir"));
				resources.setHTMLTemplate(resources_path+prop.getProperty("html_template"));
				resources.setRWRAlpha(prop.getProperty("rwr_alpha")); // Not Used 
				resources.setQKBLocation(resources_path+prop.getProperty("qkb")); 
				resources.setQkb_url(prop.getProperty("qkb_url")); 
				
				
				double[] hyperParams = new double[6];
				hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.SIMILAR_SURFACE"));
				hyperParams[Edge.TYPE.SAME_ROW.getVal()] = Double.valueOf(prop.getProperty("hyper_param.SAME_ROW"));
				hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.SAME_COLUMN"));
				hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.HEADER_CELL"));
				hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.MENTION_CANDIDATE"));
				hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.CANDIDATE_CANDIDATE"));
				Edge.setHyper_param(hyperParams);
				resources.setHeaderCount(Double.valueOf(prop.getProperty("header_count_fraction"))); //
				resources.setGeneral_relatedness(Boolean.valueOf(prop.getProperty("general_relatedness")));
				resources.setNumbers_file(resources_path+prop.getProperty("numbers_file"));
				resources.set_NER_switch(prop.getProperty("NER_switch"));
				resources.set_context_switch(prop.getProperty("context_switch"));
				Classes.setSimCutOff(Double.valueOf(prop.getProperty("classes_sim_cut_off")));
				Concepts.setSimCutOff(Double.valueOf(prop.getProperty("concepts_sim_cut_off")));
				Units_Measures.setSimCutoff(Double.valueOf(prop.getProperty("sim_cut_off_dimension_measures")));
				GraphBuilder.setCandRelSimCutOff(Double.valueOf(prop.getProperty(("sim_cut_off_cand_reltd"))));
				resources.load();
				loadStopwords();
			}

		} catch (Exception e) {
			s_logger_.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (in_stream != null) {
				in_stream.close();
			}
		}
		return resources;
	}

	private void loadStopwords() {
		InputStream in_stream = null;
		try {
			String resources_file_name = "stopwords.lst";
			in_stream = getClass().getClassLoader().getResourceAsStream(resources_file_name);
			//InputStreamReader reader = null;
			BufferedReader reader = null;
			String line;
			Set<String> stopwords = new LinkedHashSet<String>(174);
			if (in_stream != null) {
				reader =  new BufferedReader (new InputStreamReader(in_stream,"utf-8"));
				while((line = reader.readLine())!=null){
					stopwords.add(line.trim());
				}
				AnnotationEnrichment.setStopwords(stopwords);
			}
		
		}catch(Exception exc){
			
		}
	}

	public void loadHyperparams() throws IOException {
		InputStream in_stream = null;
		Properties prop = new Properties();
		String resources_file_name = "resources.properties";
		in_stream = getClass().getClassLoader().getResourceAsStream(resources_file_name);
		try {
			if (in_stream != null) {
				prop.load(in_stream);
				double[] hyperParams = new double[6];
				hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.SIMILAR_SURFACE"));
				hyperParams[Edge.TYPE.SAME_ROW.getVal()] = Double.valueOf(prop.getProperty("hyper_param.SAME_ROW"));
				hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.SAME_COLUMN"));
				hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.HEADER_CELL"));
				hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.MENTION_CANDIDATE"));
				hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = Double
						.valueOf(prop.getProperty("hyper_param.CANDIDATE_CANDIDATE"));
				Edge.setHyper_param(hyperParams);
			}
		} catch (Exception e) {
			s_logger_.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (in_stream != null) {
				in_stream.close();
			}
		}
	}
	public void loadHyperparams(EXPERIMENT_ID exp_id, SETTINGS_ID settings) {
		double[] hyperParams =  Hyperparams.getHyperparams(exp_id, settings);
		StringBuilder hyperParams_str = new StringBuilder();
		for(double hyperParam : hyperParams)
			hyperParams_str.append(hyperParam+",");
		s_logger_.info("Parameters:" + hyperParams_str.toString());
		setHyperparams(hyperParams);
	}
	
	public void setHyperparams(double[] hyperparams) {
		Edge.setHyper_param(hyperparams);

	}

	public void loadSettings(Document doc) {
		Resources resources = Resources.getResources();
		double[] hyperParams = new double[6];
		if(isValidHyperparams(doc)){
			hyperParams[0] = doc.getHp_same_string();
			hyperParams[1] = doc.getHp_same_row();
			hyperParams[2] = doc.getHp_same_column();
			hyperParams[3] = doc.getHp_header_cell();
			hyperParams[4] = doc.getHp_mention_candidate();
			hyperParams[5] = doc.getHp_candidate_candidate();
		}else{
			loadHyperparams(EXPERIMENT_ID.part_src, SETTINGS_ID.DEFAULT);
		}
		if(doc.getGamma() >0){
			resources.setRWRGamma(doc.getGamma());
		}
		if(doc.getMax_itr() >0){
			resources.setRWRMaxItr(doc.getMax_itr());
		}
		
	}

	public boolean isValidHyperparams(Document doc) {
		double sum = doc.getHp_candidate_candidate()+ doc.getHp_header_cell()+ doc.getHp_mention_candidate()
				+ doc.getHp_same_column()+ doc.getHp_same_row()+ doc.getHp_same_string();
		if(0.9 < sum  && sum <1.1)
			return true;
		return false;
	}

	
	
	
}
