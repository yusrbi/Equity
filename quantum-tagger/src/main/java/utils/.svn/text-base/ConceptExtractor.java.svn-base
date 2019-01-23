package utils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConceptExtractor {
	
	public static void main(String[] arg){
		try{
			List<String> wp2fb = laodFreebaseWikipediaLinks("data/FreebaseWpLinks-rdf-filtered");
			Map<String,String> wp2yago = loadYagoWikipediaLinks("data/yagoWikipediaLinks.tsv");
			findLinksInFbNotInYago(wp2fb, wp2yago);
		}catch(Exception exc){
			exc.printStackTrace();
		}
	}

	private static void findLinksInFbNotInYago(List<String> wp2fb,
			Map<String, String> wp2yago) throws IOException {
		List<String> concepts = new LinkedList<String>();
		for(String key : wp2fb){
			if(key.contains("index.html?curid=")){
				continue;
			}
			if(!wp2yago.containsKey(key)){
				concepts.add(key);
			}
		}
		FileUtils.writeListToFile(concepts,"data/concepts.list");		
	}

	private static Map<String, String> loadYagoWikipediaLinks(String fileName) throws IOException {
		return FileUtils.loadFileToMap(fileName, "\t|\\s",3,0);
	}

	private static List<String> laodFreebaseWikipediaLinks(String fileName) throws IOException {
		return FileUtils.loadFileToList(fileName, "\t|\\s",2);
	}

}
