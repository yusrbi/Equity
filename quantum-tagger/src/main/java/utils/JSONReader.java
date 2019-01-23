package utils;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONReader {

	public static JSONObject readJSONObjectFromFile(String file_name) {
		JSONObject json_object = null;
		JSONParser json_parser = new JSONParser();
		try {
			json_object = (JSONObject) json_parser.parse(new InputStreamReader(new FileInputStream(file_name),"UTF-8"));
			return json_object;
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json_object;
	}
	
	public static JSONArray readJSONArrayFromFile(String file_name) {
		JSONArray json_object = null;
		JSONParser json_parser = new JSONParser();
		try {
			json_object = (JSONArray) json_parser.parse(new FileReader(file_name));
			return json_object;
			
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json_object;
	}

}
