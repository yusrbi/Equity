package knowledgebase;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;

import resources.Resources;
import utils.DatabaseAccess;


public class WeightsCalculator {
//TODO weigh using some params  
	

	public static double getHeaderCellCandidatesSimilarity(
			Candidate header_candidate, Candidate cell_candidate, boolean general_relatedness) {

		//TODO pick the average and see how it changes 
		double sim, temp ;
		sim = 0.0;//StringUtils.getJaroWinklerDistance(strip(header_candidate.getSemanticTargetId()), strip(cell_candidate.getSemanticTargetId()));
		if(cell_candidate.getCategories() !=null){
			for(String cat : cell_candidate.getCategories()){
				temp =  StringUtils.getJaroWinklerDistance(strip(header_candidate.getSemanticTargetId()),Classes.getClassName(cat));
				if(temp > sim)
						sim = temp;
			}
		}
		
		//check the co-occurences here 
		DatabaseAccess data_access = null;
		String id1 = getID(cell_candidate);
		String id2 = getID(header_candidate);
		int count =0;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			if(general_relatedness){
				count = data_access.getGeneralRelatednessCount(id1, id2);
			}else{
				count = data_access.getHeaderCellCount(id1,id2);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(count!=0){
			double frac = Resources.getResources().getHeader_count_fraction();
			sim = sim *(1-frac) + ((double)count/(double) data_access.getMaxCount()) * frac;
		}
		return sim;
	}

	private static CharSequence strip(String str) {
		if(str.startsWith("YAGO"))
			return str.substring(5);
		if(str.startsWith("yago"))
			return str.substring(4);
		else
			return str;
	}

	public static double getSameRowSimilarity(Candidate cell_candidate,
			Candidate other_cell_candidate, boolean general_relatedness) {
		String id1 = getID(cell_candidate);
		String id2 = getID(other_cell_candidate);
		int count =0;
		DatabaseAccess data_access = null;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			if(general_relatedness){
				count = data_access.getGeneralRelatednessCount(id1, id2);
			}else{
				count = data_access.getRowPairCount(id1,id2);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(count !=0){
			return ((double)count/(double) data_access.getMaxCount()) ;
		}else
			return 0.0;
	}

	public static double getSameColumnSimilarity(Candidate cell_candidate,
			Candidate other_cell_candidate, boolean general_relatedness) {
		String id1 = getID(cell_candidate);
		String id2 = getID(other_cell_candidate);
		int count =0;
		DatabaseAccess data_access = null ;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			if(general_relatedness){
				count = data_access.getGeneralRelatednessCount(id1, id2);
			}else{
				count = data_access.getColumnPairCount(id1,id2);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(count !=0){
			return ((double)count/(double) data_access.getMaxCount()) ;
		}else
			return 0.0;
		
	}
	
	public static String getID(Candidate cell_candidate){
		String id1 ="";
		switch(cell_candidate.getType()){
		case Entity:
			id1 = cell_candidate.getSemanticTargetId().replace("YAGO:", "");
			break;
		case CONCEPT:
			id1 = cell_candidate.getSemanticTargetId().replace(" ", "_");
			break;
		case QUANTITY:
			id1 = cell_candidate.getSemanticTargetId();
			break;
		case CLASS:
			id1 = cell_candidate.getSemanticTargetId().replace(" ", "_");//"Category:"+
			break;
		default:
			id1 = cell_candidate.getSemanticTargetId().replace(" ", "_");
			break;
				
		}
		return id1.replace("'", "''");// --> not needed .replace("\\u","\\"); // for POSTGRES
	}

	public static double getSameColumnMentionSimilarity(String mention1, String mention2) {
		if(mention1.equals(mention2))
			return 0;
		int count =0;
		DatabaseAccess data_access = null;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			count = data_access.getSameColumnMentionCount(mention1,mention2);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		if(count !=0){
			return ((double)count/(double) data_access.getSameColumnMentionsMaxCount()) ;
		}else
			return 0;//return 0.05/(double) data_access.getSameColumnMentionsMaxCount();
		
	}

	public static double getSameRowMentionsSimilarity(String mention1, String mention2) {
		if(mention1.equals(mention2))
			return 0;
		int count =0;
		DatabaseAccess data_access = null;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			count = data_access.getSameRowMentionsCount(mention1,mention2);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}		
		if(count !=0){
			return ((double)count/(double) data_access.getSameRowMentionsMaxCount()) ;
		}else
			return 0;//0.05/(double) data_access.getSameRowMentionsMaxCount();
	}

	public static double getHeaderCellMentionsSimilarity(String header, String mention) {		
		int count =0;
		DatabaseAccess data_access = null;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			count = data_access.getHeaderMentionCount(header,mention);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}		
		if(count !=0){
			return ((double)count/(double) data_access.getHeaderMentionMaxCount()) ;
		}else
			return 0; //0.05/(double) data_access.getHeaderMentionMaxCount();
	}

	public static double getCandCandSimilarity(Candidate cand1, Candidate cand2) {
		if(cand1.equals(cand2))
			return 0;
		int count =0;
		String id1 = getID(cand1);
		String id2 = getID(cand2);
		DatabaseAccess data_access = null;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			count = data_access.getGeneralRelatednessCount(id1, id2);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}		
		if(count !=0){
			return ((double)count/(double) data_access.getMaxCount()) ;
		}else
			return 0.0;
	}

	
	

}
