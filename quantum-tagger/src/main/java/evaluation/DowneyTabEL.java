package evaluation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import data.Document_;
import data.EvaluationDocument;
import data.EvaluationTable;
import data.Table_;
import loader.DocumentLoader;
import utils.FileUtils;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DowneyTabEL implements Iterator<Document_>{

	private static Logger slogger_ = LoggerFactory.getLogger(DowneyTabEL.class);
	String file_path ;
	private BufferedReader reader ; 
	int current_index =0, length = 3000;
	int total_annotations=0;
	public static final 	Map<Integer, String> Valid_IDS = new HashMap<Integer,String>();
	public int getTotal_annotations() {
		return total_annotations;
	}

	public DowneyTabEL(String downey_main_path) throws FileNotFoundException {
		//known to be 3000 tables
		this.file_path = downey_main_path;
		this.total_annotations =0;
		reader = FileUtils.getBufferedUTF8Reader(file_path);		
	}

	@Override
	public boolean hasNext() {
		if(current_index < length)
			return true;
		else
			return false;
	}

	@Override
	public Document_ next() {	

		Document_ document = null;
		try {
			document = laodDocument(reader.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		current_index++;
		total_annotations+= document.getTable(0).getTotalAnnotationsCount();
		return document;
	}
	public static Document_ laodDocument(String table_json_str) {
		Document_ document = null;
		String table_title = "";
		String page_title = "";
		if(table_json_str != null && !table_json_str.isEmpty()){
			JsonReader json_reader = Json.createReader(new ByteArrayInputStream(table_json_str.getBytes()));
			JsonObject json_obj = json_reader.readObject();
			document = new EvaluationDocument(); 
			String id = json_obj.getString("_id");
			int numCols = json_obj.getInt("numCols");
			int numRows = json_obj.getInt("numDataRows") + json_obj.getInt("numHeaderRows");
			if(json_obj.containsKey("pgTitle"))
				page_title = json_obj.getString("pgTitle");
			if(json_obj.containsKey("tableCaption"))
				table_title= json_obj.getString("tableCaption");			
			Table_ table = new EvaluationTable(table_title, numCols, numRows, json_obj);
			document.addTable(table);
			document.setFile_name(page_title);
			slogger_.info(" Document ID :" + id);
		}
		return document;
	}

	public static void load_valid_wiki_ids_in_yago(){
	
		try {
			BufferedReader reader = FileUtils.getBufferedUTF8Reader("/GW/D5data/yibrahim/wikitables/tables_wiki_random_yago_wiki_ids_title.txt");
			String line ;
			String[] temp;
			while((line=reader.readLine())!=null){
				temp = line.split("\t");
				Valid_IDS.put(Integer.parseInt(temp[0].trim()), temp[1].trim());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
