package knowledgebase;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import annotations.Annotation;
import data.Document_;
import data.Table_;
import edu.stanford.nlp.util.Pair;

public class MentionsFilter {

	public void filterTableMentions(HashMap<String, Document_> documents) {
		for(Document_ document : documents.values()){
			filterTableMentions(document);
		}
		
	}
	/**
	 * This method only filters out the overlapping mentions and pick the maximal one with good candidates 
	 *  It require first finding all th overlapping mentions within a single cell, then for each compare there candidates
	 * @param document
	 */
	public void filterTableMentions(Document_ document) {
		Set<Pair<String, Annotation>> cell_mentions;	
		for(Table_ table : document.getTables()){	
			for(int i =0; i< table.getNrow(); i++){
				for(int j =0; j < table.getNcol(); j++){
					cell_mentions = table.getMentions(i,j);
					if(cell_mentions !=null)
						filterOverlapppingMentions(cell_mentions, table);
				}
			}
		}
	}
	private void filterOverlapppingMentions(
			Set<Pair<String, Annotation>> cell_mentions, Table_ table) {
		String mention1, mention2;
		List<Pair<String, Annotation>>  losers = new LinkedList<Pair<String,Annotation>>();
		
		for(Pair<String,Annotation> mention_1 : cell_mentions){			
			mention1 = mention_1.first;
			for(Pair<String,Annotation> mention_2 : cell_mentions){				
				mention2= mention_2.first;
				if(mention1.equals(mention2))
					continue;
				if(overlap(mention1,mention2)){
					if(compareCandidates(mention_1, mention_2, table) < 1){
						// j is better annotation
						losers.add(mention_1); 
						break;
					}
				}
			}
		}
		for(Pair<String, Annotation> mention : losers ){
			table.removeAnnotationsCascaded(mention);
		}
	}
	

	/**
	 * return a number larger than one if the first mention is the one to keep
	 * @param mention1
	 * @param mention2
	 * @param table_candidates
	 * @return
	 */
	private int compareCandidates(Pair<String, Annotation> mention1,
			Pair<String, Annotation> mention2, Table_ table) {
//		int mention1_score=0;
//		int mention2_score=0;
		Set<Candidate> mention1_cands, mention2_cands;
		String mention1_id = mention1.first +"_" + mention1.second.getUniqueID();
		String mention2_id = mention2.first+"_"+mention2.second.getUniqueID();
		mention1_cands = table.getCandidates(mention1_id);
		mention2_cands = table.getCandidates(mention2_id);
		// the easy decision part
		if(mention1_cands ==null && mention2_cands == null){
			if( mention1.first.length() > mention2.first.length())
				return 1;
			else
				return -1;
		}
		if(mention1_cands == null || mention1_cands.isEmpty())
			return -1;
		if(mention2_cands == null || mention2_cands.isEmpty())
			return 1;
		
		if(mention1.first.length() > mention2.first.length()){
			return 1;
		}else{
			return -1;
		}
		//TODO more complex strategy 
	}
	private boolean overlap(String mention1, String mention2) {
		boolean overlap= false;
		if(mention1.length() > mention2.length())
			overlap =  mention1.contains(mention2);
		else
			overlap = mention2.contains(mention1);
		return overlap;
	}
	

}
