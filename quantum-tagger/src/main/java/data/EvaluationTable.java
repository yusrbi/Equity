package data;


import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;


import annotations.Annotation;
import annotations.Annotation.ANNOTATION;
import annotations.Entity;
import edu.stanford.nlp.util.Pair;
import evaluation.DowneyTabEL;
import knowledgebase.Candidate;

public class EvaluationTable extends Table_ {

	public EvaluationTable(String title, int numCols, int numRows, JsonObject json_obj) {
		super(title, "");
		String[] row = new String[numCols];
		String cell_content;
		JsonArray table_data =  json_obj.getJsonArray("tableData");
		JsonArray table_headers = json_obj.getJsonArray("tableHeaders");
		JsonObject cell;
		int row_index=0, col_index =0;		
		setNrow(numRows);
		setNcol(numCols);
		// load the header first
		for( JsonValue json_value : table_headers){
			JsonArray cells = (JsonArray) json_value;
			row = new String[numCols];
			col_index=0;
			for(JsonValue cell_value : cells) {
				cell = (JsonObject)cell_value;				
				cell_content = cell.getString("text");
				row[col_index] =cell_content;
				addAnnotations(cell.getJsonArray("surfaceLinks"), col_index, row_index);
				col_index++;
			}
			setColumns(row, row_index);
			row_index++;
		}
		//load data
		for( JsonValue json_value : table_data){
			JsonArray cells = (JsonArray) json_value;
			row = new String[numCols];
			col_index=0;
			for(JsonValue cell_value : cells) {
				cell = (JsonObject)cell_value;				
				cell_content = cell.getString("text");
				row[col_index] =cell_content;
				addAnnotations(cell.getJsonArray("surfaceLinks"), col_index, row_index);
				col_index++;
			}
			setColumns(row, row_index);
			row_index++;
		}
	}
	public EvaluationTable(String file_name, String content) {
		super(file_name, content);
	}
	private void addAnnotations(JsonArray surfaceLinks, int col_index, int row_index) {
		JsonObject link, target ;
		int offset, end_offset;
		String surface;
		int KBID;
		Annotation annotation ; 		
		for(JsonValue value : surfaceLinks) {
			link = (JsonObject) value;
			offset = link.getInt("offset");
			end_offset= link.getInt("endOffset");
			surface = link.getString("surface");
			target = link.getJsonObject("target");			
			KBID = target.getInt("id");
			if(KBID <=0 || !DowneyTabEL.Valid_IDS.containsKey(KBID))
				continue;
			annotation = new Entity(row_index, col_index, offset, end_offset,ANNOTATION.ENTITY);
			annotation.setGold_standard(KBID, DowneyTabEL.Valid_IDS.get(KBID));
			addAnnotation(surface, row_index, col_index, annotation);

		}
		
	}

	@Override
	public void writeTableWithAnnotationsToDB() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeResultstoDB() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeHTMLResultstoDB(String html_content) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<Integer> calculatePrecision() {
		
		int correct =0, total =0;
		if(super.getResult() == null)
			return Arrays.asList(0,0);
		Pair<Integer,String> gs=null;
		String   result_str, mention ;
		for(Entry<String, Pair<Candidate, Double>> result : super.getResult().entrySet()){		
						
			gs = getGoldStandardForResultsKey(result.getKey());
			if(gs == null || gs.second == null || gs.second.isEmpty())
				continue;			
			total++;
			result_str = result.getValue().first.getSemanticTargetId();
			
			mention = result.getKey();
			if(gs.second.toLowerCase().equals(result_str.toLowerCase()))
				correct++;
			else
				slogger_.info(String.format("Wrong Mention: %s, GS: %s , result: %s", mention, gs.second, result_str) );
			
		}
		return  Arrays.asList(correct, total);		
		
	}
	private Pair<Integer, String> getGoldStandardForResultsKey(String mention_key) {
		String [] parts= mention_key.split("_");
		
		Annotation annotation = getAnnotation(parts[0], Integer.parseInt(parts[1]));
		if(annotation!= null)
			return new Pair<Integer,String>(annotation.getGold_standard_id(),
					annotation.getGold_standard());
		else
			return null;
	}
	private Annotation getAnnotation(String mention, int annotations_id) {

		Set<Annotation> annotations_lst = (Set<Annotation>) annotations.get(mention);
		for(Annotation annotation : annotations_lst){
			if(annotation.getUniqueID() == annotations_id)
				return annotation;
		}
		return null;
	}

}
