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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotators.AnnotationEnrichment;

public class ResourcesLoader_old {

	Logger s_logger_ = LoggerFactory.getLogger(ResourcesLoader_old.class);

	public Resources load() throws IOException {
		InputStream in_stream = null;
		Resources resources = Resources.getResources();
		try {
			Properties prop = new Properties();
			String resources_file_name = "resources.properties";
			in_stream = getClass().getClassLoader().getResourceAsStream(resources_file_name);
			if (in_stream != null) {
				prop.load(in_stream);
				String units_file_name = prop.getProperty("units_file");
				String stanford_ner_result = prop.getProperty("stanford_ner_result_saved");
				resources.setUnits_file_name(units_file_name);
				resources.setStanford_ner_result(stanford_ner_result);
				resources.setConceptsFile(prop.getProperty("concepts_file"));
				resources.setCategoriesFiel(prop.getProperty("categories_file"));
				resources.setGraphSaveDir(prop.getProperty("graph_save_dir"));
				resources.setDBConnectionURL(prop.getProperty("db_connection_url"));
				resources.setDBUser(prop.getProperty("db_user"));
				resources.setDBPassword(prop.getProperty("db_password"));
				resources.setRWRGamma(Double.parseDouble(prop.getProperty("rwr_gamma")));
				resources.setRWRMaxItr(Integer.parseInt(prop.getProperty("rwr_max_itr")));
				resources.setHTMLResultsPath(prop.getProperty("html_results_dir"));
				resources.setHTMLTemplate(prop.getProperty("html_template"));
				resources.setRWRAlpha(prop.getProperty("rwr_alpha")); // not use
																		// up
																		// till
																		// this
																		// point
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
				resources.setNumbers_file(prop.getProperty("numbers_file"));
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

	public void loadHyperparamCandCandOnly_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0118963611;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.9881036389;
		Edge.setHyper_param(hyperParams);
	}

	public void loadHyperparamSameRowOnly_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.9201266927;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 	0.0798733073;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}
	public void loadHyperparamSameColumnOnly_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.8401339519;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 	0.1598660481;
		Edge.setHyper_param(hyperParams);
	}

	

	public void loadHyperparamHeaderOnly_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 	0.8141307361;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.1858692639;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}

	public void loadTableStructureOnly_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] =0.5199606635;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.2372011683;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.1977020166;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0451361516;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}
	
	public void loadHyperparamCandCandWithSimSurface_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.1339088238;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0103033334;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.8557878428;
		Edge.setHyper_param(hyperParams);
	}

	public void loadHyperparamSameRowWithSimSurface_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.5093434412;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.4514661967;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0391903621;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}	
	public void loadHyperparamSameColumnWithSimSurface_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.6750844871;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.2729725539;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.051942959;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}
	public void loadHyperparamHeaderWithSimSurface_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.7072323452;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.2383511463;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] =0.0544165085;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}
	public void loadTableStructureWithoutSameSurface_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.1094889614;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.0499478352;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0416304347;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0095043927;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.789428376;
		Edge.setHyper_param(hyperParams);
	}
	
	public void loadTableStructureWithSimSurface_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.3697288808;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.3277161893;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.1495010458;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.1246058713;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0284480128;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}
	public void setHyperparams(double[] hyperparams) {
		Edge.setHyper_param(hyperparams);

	}

	public void setHyperparams_full(String experiemnt) {
		switch(experiemnt){
		case "TrainedParams":
			loadTrainedHyperparams_full();
			break;
		case "CandidateCandidate":
			loadHyperparamCandCandOnly_full();
			break;
			case "SameRow":
				loadHyperparamSameRowOnly_full();
			break;
			case "SameColumn":
				loadHyperparamSameColumnOnly_full();
			break;
			case "SameHeader":
				loadHyperparamHeaderOnly_full();
			break;
			case "TableStructure":
				loadTableStructureOnly_full();
			break;
			case "SameSurface+CandidateCandidate":
				loadHyperparamCandCandWithSimSurface_full();
			break;
			case "SameSurface+SameRow":
				loadHyperparamSameRowWithSimSurface_full();
			break;
			case "SameSurface+SameColumn":
				loadHyperparamSameColumnWithSimSurface_full();
			break;
			case "SameSurface+SameHeader":
				loadHyperparamHeaderWithSimSurface_full();
			break;
			case "SameSurface+TableStructure":
				loadTableStructureWithSimSurface_full();
			break;		
			case "TableStructureWithoutSameSurface":
				loadTableStructureWithoutSameSurface_full();
				break;
			default:
				loadTrainedHyperparams_full();
				break;
		}
		
	}
	
	public void setHyperparams_part(String experiemnt) {
		switch(experiemnt){
		case "TrainedParams":
			loadTrainedHyperparams_part();
			break;
		case "CandidateCandidate":
			loadHyperparamCandCandOnly_part();
			break;
			case "SameRow":
				loadHyperparamSameRowOnly_part();
			break;
			case "SameColumn":
				loadHyperparamSameColumnOnly_part();
			break;
			case "SameHeader":
				loadHyperparamHeaderOnly_part();
			break;
			case "TableStructure":
				loadTableStructureOnly_part();
			break;
			case "SameSurface+CandidateCandidate":
				loadHyperparamCandCandWithSimSurface_part();
			break;
			case "SameSurface+SameRow":
				loadHyperparamSameRowWithSimSurface_part();
			break;
			case "SameSurface+SameColumn":
				loadHyperparamSameColumnWithSimSurface_part();
			break;
			case "SameSurface+SameHeader":
				loadHyperparamHeaderWithSimSurface_part();
			break;
			case "SameSurface+TableStructure":
				loadTableStructureWithSimSurface_part();
			break;	
			case "TableStructureWithoutSameSurface":
				loadTableStructureWithoutSameSurface_part();
				break;
			default:
				loadTrainedHyperparams_part();
				break;
		}
		
	}

	private void loadTrainedHyperparams_full() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.1099443638;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0974512672;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.0444563523;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0370534031;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0084594383;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.7026351754;
		Edge.setHyper_param(hyperParams);		
	}


	

	private void loadTrainedHyperparams_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.0020782346;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.00179455;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.9935177493;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.00037962085636615063;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0009292325124572968;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0013006127;
		Edge.setHyper_param(hyperParams);		
	}



	public void loadHyperparamCandCandOnly_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.4167251189;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.5832748811;
		Edge.setHyper_param(hyperParams);
	}



	public void loadHyperparamSameRowOnly_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.6588448263;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 	0.3411551737;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}

	public void loadHyperparamSameColumnOnly_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] =0.9990655786;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] =0.0009344214;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 	0.0;
		Edge.setHyper_param(hyperParams);
	}


	public void loadHyperparamHeaderOnly_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 	0.2900407833;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.7099592167;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}	

	public void loadTableStructureOnly_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] =0.001803002;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.998196998;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0003814088;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.000933609;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}


	public void loadHyperparamCandCandWithSimSurface_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.4824039188;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.2156952885;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] =0.3019007927;
		Edge.setHyper_param(hyperParams);
	}

	public void loadHyperparamSameRowWithSimSurface_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.4327836646;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.3737075479;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.1935087875;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}	
	public void loadHyperparamSameColumnWithSimSurface_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.0020854812;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 	0.9969820462;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0009324727;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}
	
	
	public void loadHyperparamHeaderWithSimSurface_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.6135756166;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.1120788309;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] =0.2743455525;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}

	public void loadTableStructureWithSimSurface_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0.0020809411;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] =0.0017968871;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.9948116139;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0003801152;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0009304427;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0;
		Edge.setHyper_param(hyperParams);
	}

	public void loadTableStructureWithoutSameSurface_part() {
		double[] hyperParams = new double[6];
		hyperParams[Edge.TYPE.SIMILAR_SURFACE.getVal()] = 0;
		hyperParams[Edge.TYPE.SAME_ROW.getVal()] = 0.0017982873;
		hyperParams[Edge.TYPE.SAME_COLUMN.getVal()] = 0.9955868122;
		hyperParams[Edge.TYPE.HEADER_CELL.getVal()] = 0.0003804114;
		hyperParams[Edge.TYPE.MENTION_CANDIDATE.getVal()] = 0.0009311677;
		hyperParams[Edge.TYPE.CANDIDATE_CANDIDATE.getVal()] = 0.0013033213;
		Edge.setHyper_param(hyperParams);
	}
	
	
}
