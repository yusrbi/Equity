package knowledgebase;

import java.util.LinkedList;
import java.util.List;

import knowledgebase.Candidate.TYPE;
import lsh.Common;
import lsh.LSHTable;

import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.util.Pair;
import resources.Resources;
import utils.DatabaseAccess;

public class Concepts {

	private static Double sim_cut_off;

	public static List<Candidate> getCandidates(String mention) {
		
		List<Candidate> candidates = new LinkedList<Candidate>();
		//check if the mention is a number/ year/date
		if(Units_Measures.is_qunatity(mention))
			return candidates;
		if(mention.toLowerCase().equals("notes")
				|| mention.toLowerCase().equals("details")
				|| mention.toLowerCase().equals("references")
				|| mention.toLowerCase().equals("ref"))
			return candidates;
		Candidate candidate;
		//strip the headers from all the modifiers 
		String pure_mention = Units_Measures.stripHeaderUnit(mention);
		//pure_mention =  Units_Measures.stripStatisticalModifiers(mention);
		if(pure_mention.isEmpty()){
			//only a statistical term 
			//what kind of candidates??
			candidate = new CandidateConcept("QUANTITY_CLASS.Statistical",TYPE.QUANTITY_CLASS,1.0,"");
			candidates.add(candidate);
			return candidates;
		}		
		
		Resources resources = Resources.getResources();
		LSHTable concepts = resources.getLSHConcepts();		
		Units_Measures units_measures = Units_Measures.getUnits_Measures();
		
		//1- check for matches with concepts in the knowledge base
		double sim =0;
		try{
			DatabaseAccess data_access = DatabaseAccess.getDatabaseAccess();
			List<Pair<String,Integer>> cands = data_access.getCandidatesForMention(pure_mention);
			sim =0;
			if(cands != null){
				for(Pair<String, Integer> cand : cands){
					sim = (double)cand.second/ (double)data_access.getMax_mention_candidate_count();
					candidate =  new CandidateConcept(cand.first, TYPE.CONCEPT,sim,
							"https://en.wikipedia.org/wiki/"+cand.first);
					candidates.add(candidate);
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(candidates.size() ==0){
			for(String concept : concepts.deduplicate(Common.getCounter(pure_mention))){
				sim = StringUtils.getJaroWinklerDistance(concept, pure_mention);
				if(sim >sim_cut_off) {
					candidate =  new CandidateConcept(concept.replace(" ", "_"),TYPE.CONCEPT,sim,
							"https://en.wikipedia.org/wiki/"+concept.replace(" ", "_"));
					candidates.add(candidate);
				}
			}
		}		
		//2- check for matches with Dimensions
		if(candidates.size() ==0){
			candidates.addAll(units_measures.getDimensions(pure_mention));
		}	
		if(candidates.size() ==0){
			//check for the unit in the header and get its dimension 
			String unit = Units_Measures.getHeaderUnit(mention);
			candidates.addAll(Units_Measures.getDimensionsCandidatesForUnit(unit));
		}
		if(candidates.size() ==0){
			//3- check for approximate matches with quantity classes 
			candidates.addAll(units_measures.getClasses(pure_mention));	
		}
		return CandidatesSearch.removeDuplicates(candidates);
	}

	
	public static void setSimCutOff(Double val) {
		sim_cut_off = val;
		
	}

}
