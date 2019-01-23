package knowledgebase;

import java.util.LinkedList;
import java.util.List;

import knowledgebase.Candidate.TYPE;
import lsh.Common;
import lsh.LSHTable;
import resources.Resources;

import org.apache.commons.lang3.StringUtils;

public class Classes {

	private static double sim_cut_off =0.0;

	public static List<Candidate> getCandidates(String mention) {
		List<Candidate> candidates = new LinkedList<Candidate>();
		Resources resources = Resources.getResources();
		LSHTable classes = resources.getLSHClasses();
		Candidate candidate;
		double sim =0;
		for(String category : classes.deduplicate(Common.getCounter(mention))){
			sim = StringUtils.getJaroWinklerDistance(category, mention);
			if(sim > sim_cut_off){
				candidate =  new CandidateClass(category.replace(" ", "_"),TYPE.CLASS,sim, 
						"https://en.wikipedia.org/wiki/"+category.replace(" ", "_"));
				candidates.add(candidate);
			}
			
		}		
		if(candidates.size() ==0){// no matches found so far
			String[] temp = mention.split("/|,|-|\\(|\\)|&|\\|");
			if(temp.length>1){
				for(String possible_mention : temp){
					for(String category : classes.deduplicate(Common.getCounter(possible_mention))){
						sim = StringUtils.getJaroWinklerDistance(category, possible_mention);
						if(sim > sim_cut_off){
							candidate =  new CandidateClass(category.replace(" ", "_"),TYPE.CLASS,sim,
									"https://en.wikipedia.org/wiki/"+category.replace(" ", "_"));
							candidates.add(candidate);
						}						
					}
				}
			}
		}
		return candidates;
	}

	public static String getClassName(String string) {
		if(string.startsWith("\"")){
			string = string.substring(1, string.length()-1);
		}
		String out; 
		if(string.startsWith("YAGO_wordnet_")){
			out = string.substring(13, string.length()-10).replace("_", " ");
			
		}else if (string.startsWith("YAGO_wikicategory_")){
			out = string.substring(18).replace("_", " ");
		}else if(string.startsWith("YAGO_yago")){
			out = string.substring(9).replace("_", " ");
		}else {
			out = string.substring(5);
		}
		return out;
	}
	public static void setSimCutOff(double val) {
		sim_cut_off  = val;
		
	}

}
