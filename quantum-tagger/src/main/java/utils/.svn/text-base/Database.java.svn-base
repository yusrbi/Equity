package utils;

import java.util.List;
import java.util.Map;

import edu.stanford.nlp.util.Pair;

public class Database {

	private Map<String, Integer> colum_pairs_map;
	private Map<String, Integer> row_pairs_map;
	private Map<String, Integer> header_cell_map;
	private Map<String, Integer> header_header_map;
	private Map<String, Integer> mention_count_map;
	private Map<String, Integer> generla_relatedness;
	private int max;
	private Map<Pair<String, String>, Integer> mention_unit_count;
	private Map<Pair<String, String>, Integer> mention_mention_row_count;
	private Map<Pair<String, String>, Integer> mention_mention_column_count;
	private Map<Pair<String, String>, Integer> header_mention_count;
	int max_header_mention_count =0;
	int max_mention_mention_row_count =0;
	int max_mention_mention_column_count =0;
	private int max_mention_unit;
	private Map<String, List<Pair<String, Integer>>> mention_candidates;
	private int max_mention_candidate_count;
	



	public int getMax() {
		return max;
	}


	public void init(Map<String, Integer> colum_pairs_map,
			Map<String, Integer> row_pairs_map,
			Map<String, Integer> header_cell_map,
			Map<String, Integer> header_header_map,
			Map<String, Integer> mention_count_map,
			Map<String, Integer> generla_relatedness, int max ) {
		this.colum_pairs_map = colum_pairs_map;
		this.row_pairs_map= row_pairs_map;
		this.header_cell_map = header_cell_map;
		this.header_header_map = header_header_map;
		this.mention_count_map = mention_count_map;	
		this.generla_relatedness = generla_relatedness;
		this.max = max;
	}

	
	public int getGenralRelatedness(String cand1, String cand2) {
		String key = cand1+ cand2;
		if(generla_relatedness == null)
			return 0;
		if(generla_relatedness.containsKey(key))
			return generla_relatedness.get(key);
		key = cand2+ cand1;
		if(generla_relatedness.containsKey(key))
			return generla_relatedness.get(key);
		return 0;
	}

	
	public int getColumnPairCount(String cand1, String cand2) {
		String key = cand1+ cand2;
		if(colum_pairs_map == null)
			return 0;
		if(colum_pairs_map.containsKey(key))
			return colum_pairs_map.get(key);
		key = cand2+ cand1;
		if(colum_pairs_map.containsKey(key))
			return colum_pairs_map.get(key);
		return 0;
	}



	public int getRowPairCount(String cand1, String cand2) {
		String key = cand1+ cand2;
		if(row_pairs_map == null)
			return 0;
		if(row_pairs_map.containsKey(key))
			return row_pairs_map.get(key);
		key = cand2+ cand1;
		if(row_pairs_map.containsKey(key))
			return row_pairs_map.get(key);
		return 0;
	}



	public int getHeaderCellCount(String cand1, String cand2) {
		String key = cand1+ cand2;
		if(header_cell_map == null)
			return 0;
		if(header_cell_map.containsKey(key))
			return header_cell_map.get(key);
		key = cand2+ cand1;
		if(header_cell_map.containsKey(key))
			return header_cell_map.get(key);
		return 0;
	}



	public int getMentionCount(String mention, String cand) {
		String key = cand+ mention;
		if(mention_count_map == null)
			return 0;
		if(mention_count_map.containsKey(key))
			return header_cell_map.get(key);
		return 0;
	}



	public int getHeaderHeaderCount(String cand1, String cand2) {
		String key = cand1+ cand2;
		if(header_header_map == null)
			return 0;
		if(header_header_map.containsKey(key))
			return header_header_map.get(key);
		key = cand2+ cand1;
		if(header_header_map.containsKey(key))
			return header_header_map.get(key);
		return 0;
	}


	public void setMentionUnitCount(
			Map<Pair<String, String>, Integer> mention_unit_count) {
		this.mention_unit_count = mention_unit_count;
		
	}

	public int getCountUnitMention(String mention, String unit_key) {
		Pair<String,String> key = new Pair<String,String>(mention,unit_key);
		if(mention_unit_count.containsKey(key)){
			return mention_unit_count.get(key);
		}else 
			return 0;
	}

	public void setMaxMentionUnitCount(int max) {
		this.max_mention_unit = max;
		
	}

	public int getMaxMentionUnitCount() {
		
		return max_mention_unit;
	}

	
	public Map<Pair<String, String>, Integer> getMention_mention_row_count() {
		return mention_mention_row_count;
	}


	public void setMention_mention_row_count(Map<Pair<String, String>, Integer> mention_mention_row_count) {
		this.mention_mention_row_count = mention_mention_row_count;
	}


	public Map<Pair<String, String>, Integer> getMention_mention_column_count() {
		return mention_mention_column_count;
	}


	public void setMention_mention_column_count(Map<Pair<String, String>, Integer> mention_mention_column_count) {
		this.mention_mention_column_count = mention_mention_column_count;
	}


	public Map<Pair<String, String>, Integer> getHeader_mention_count() {
		return header_mention_count;
	}


	public void setHeader_mention_count(Map<Pair<String, String>, Integer> header_mention_count) {
		this.header_mention_count = header_mention_count;
	}


	public int getMax_header_mention_count() {
		return max_header_mention_count;
	}


	public void setMax_header_mention_count(int max_header_mention_count) {
		this.max_header_mention_count = max_header_mention_count;
	}


	public int getMax_mention_mention_row_count() {
		return max_mention_mention_row_count;
	}


	public void setMax_mention_mention_row_count(int max_mention_mention_row_count) {
		this.max_mention_mention_row_count = max_mention_mention_row_count;
	}


	public int getMax_mention_mention_column_count() {
		return max_mention_mention_column_count;
	}


	public void setMax_mention_mention_column_count(int max_mention_mention_column_count) {
		this.max_mention_mention_column_count = max_mention_mention_column_count;
	}


	public int getSameColumnMentionCount(String mention1, String mention2) {
		if(mention1.equals(mention2))
			return 0;
		Pair<String,String> key = new Pair<String,String>(mention1,mention2);
		
		int count =0;
		if(mention_mention_column_count.containsKey(key)){
			count+= mention_mention_column_count.get(key);
		}
		key = new Pair<String,String>(mention2,mention1);
		if(mention_mention_column_count.containsKey(key)){
			count+= mention_mention_column_count.get(key);
		}
		return count;
	}


	public int getSameRowMentionCount(String mention1, String mention2) {
		if(mention1.equals(mention2))
			return 0;
		Pair<String,String> key = new Pair<String,String>(mention1,mention2);
		int count =0;
		if(mention_mention_row_count.containsKey(key)){
			count+= mention_mention_row_count.get(key);
		}
		key = new Pair<String,String>(mention2,mention1);
		if(mention_mention_row_count.containsKey(key)){
			count+= mention_mention_row_count.get(key);
		}
		return count;
	}

	public int getHeaderMentionCount(String header, String mention) {
		Pair<String,String> key = new Pair<String,String>(header,mention);
		if(header_mention_count.containsKey(key)){
			return header_mention_count.get(key);
		}else 
			return 0;
	}


	public void setMention_candidates(Map<String, List<Pair<String, Integer>>> candidates) {
		this.mention_candidates = candidates;
		
	}


	public void setMax_mention_candidate_count(int max_mention_candidate_count) {
		this.max_mention_candidate_count= max_mention_candidate_count;
		
	}

	public Map<String, List<Pair<String, Integer>>> getMention_candidates() {
		return mention_candidates;
	}


	public int getMax_mention_candidate_count() {
		return max_mention_candidate_count;
	}

	public List<Pair<String, Integer>> getCandidatesForMention(String mention){
		if(mention_candidates.containsKey(mention))
			return mention_candidates.get(mention);
		else
			return null;
	}


	
}
