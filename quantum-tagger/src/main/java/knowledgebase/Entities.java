package knowledgebase;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringEscapeUtils;

import knowledgebase.Candidate.TYPE;
import utils.DatabaseAccess;
import annotators.AIDAWrapper;
import edu.stanford.nlp.util.Pair;

public class Entities {

	static int count = 0;

	public static Map<String, List<Candidate>> getCandidates(
			String annotatedContents, String technique) {
		AIDAWrapper aida = new AIDAWrapper();
		String result = aida.process(annotatedContents.replace("\"", "\\\""), technique);
		if (result.isEmpty())
			return null;
		JsonReader json_reader = Json.createReader(new StringReader(result));
		JsonObject json_obj = json_reader.readObject();
		JsonArray mentions = json_obj.getJsonArray("mentions");
		JsonObject entityMetadata = json_obj.getJsonObject("entityMetadata");
		Iterator<JsonValue> mentions_itr = mentions.iterator();
		JsonObject mention = null;
		JsonObject entity = null;
		Candidate candidate;
		Map<String, List<Candidate>> candidates = new HashMap<String, List<Candidate>>();
		List<Candidate> candidates_lst = null;
		JsonObject meta;
		JsonArray cats;

		while (mentions_itr.hasNext()) {
			mention = (JsonObject) mentions_itr.next();
			String name = mention.getString("name");
			@SuppressWarnings("unused")
			int offset = mention.getInt("offset");
			@SuppressWarnings("unused")
			int length = mention.getInt("length");
			JsonArray entities = mention.getJsonArray("allEntities");
			Iterator<JsonValue> entities_itr = entities.iterator();
			candidates_lst = new LinkedList<Candidate>();
			int indx = 0;
			String temp;
			while (entities_itr.hasNext()) {
				entity = (JsonObject) entities_itr.next();
				String kbID = entity.getString("kbIdentifier");
				if(kbID.startsWith("MENTION:")){
					continue;
				}
				try {
					kbID = URLDecoder.decode(kbID, "utf8");
					kbID = StringEscapeUtils.unescapeJson(kbID);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				float score = Float.parseFloat(entity
						.getString("disambiguationScore"));
				if(score <= 0)
					score =(float)0.0001;
				candidate = new CandidateEntity(kbID, TYPE.Entity, score, "https://en.wikipedia.org/wiki/"+kbID.replace("YAGO:", ""));

				// get the list of categories
				meta = entityMetadata.getJsonObject(entity.getString("kbIdentifier"));
				if (meta != null) {
					cats = meta.getJsonArray("type");
					if (cats != null) {
						// use iterator
						String[] categories = new String[cats.size()];
						Iterator<JsonValue> cats_itr = cats.iterator();
						indx = 0;
						while (cats_itr.hasNext()) {
							temp = StringEscapeUtils.unescapeJson(cats_itr
									.next().toString());
							temp = StringEscapeUtils.unescapeXml(temp);
							try {
								categories[indx] = URLDecoder.decode(temp, "utf8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
								categories[indx] = Classes.getClassName(temp);
							} finally {
								indx++;
							}
						}
						candidate.setCategories(categories);
					}
				}
				candidates_lst.add(candidate);
			}
			if (candidates_lst.size() > 0) {
				CandidatesSearch.normalize(candidates_lst);
				candidates.put(name + "_" + (count++), candidates_lst);
			}
		}
		return candidates;
	}

	public static void reset_count() {
		count = 0;

	}

	public static List<Candidate> findInDB(String mention) {
		double sim =0;
		int limit =20, count =0;
		List<Candidate> candidates = new LinkedList<Candidate>();
		Candidate candidate;
		try{
			DatabaseAccess data_access = DatabaseAccess.getDatabaseAccess();
			List<Pair<String,Integer>> cands = data_access.getCandidatesForMention(mention);
			
			if(cands != null){
				cands.sort(new Comparator<Pair<String,Integer>>() {
					public int compare(Pair<String,Integer> entry1, Pair<String,Integer> entry2) {
						return entry2.second.compareTo(entry1.second);// largest first 
					}
				});
				sim =0;
				for(Pair<String, Integer> cand : cands){
					count++;
					sim = (double)cand.second/ (double)data_access.getMax_mention_candidate_count();
					candidate =  new CandidateEntity(cand.first, TYPE.Entity,sim,
							"https://en.wikipedia.org/wiki/"+cand.first);
					candidates.add(candidate);
					if(count > limit)
						break;// only add the top n of candidates
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return candidates;
	}

	// public static List<Candidate> getCandidates(String mention) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// public static List<Candidate> getCandidates(String mention, String cell)
	// {
	//
	// return null;
	// }
	// private static void normalize(List<Candidate> candidates_a) {
	// double sum_weights =0;
	// for(Candidate candidate : candidates_a){
	// sum_weights += candidate.getScore();
	// }
	// for(Candidate candidate : candidates_a){
	// candidate.normalizeScore(sum_weights);
	// }
	// }

}
