package knowledgebase;

import org.apache.commons.lang3.StringEscapeUtils;

public abstract class Candidate {
	private String semanticTargetId;
	public static enum TYPE{
		QUANTITY,
		CLASS,
		CONCEPT,
		Entity,
		DIMENSION,
		QUANTITY_CLASS
	}
	private TYPE type ;
	private double score; //normalized
	private String[] cats = null;
	private static int count; 
	private int unique_id;
	private String url;
	
	public Candidate(String semanticTargetId, TYPE type, double score, String url ){
		if(semanticTargetId == null)
			this.semanticTargetId = semanticTargetId;
		else
			this.semanticTargetId = semanticTargetId.trim();
		this.unique_id = count++; //TODO resset
		this.type = type;
		this.score = score;
		this.url = StringEscapeUtils.unescapeJava(url);
	}
	public String getFullSemanticTargetId() {
		return semanticTargetId;
	}
	public String getSemanticTargetId() {
		if(type == TYPE.QUANTITY){
			int start = semanticTargetId.indexOf(",");
			int end = semanticTargetId.indexOf(")");
			return semanticTargetId.substring(start+1, end);
		}else if(type == TYPE.QUANTITY_CLASS){
			int start = semanticTargetId.indexOf(".");
			return semanticTargetId.substring(start+1, semanticTargetId.length());
		}else if(type == TYPE.DIMENSION){
			int start = semanticTargetId.indexOf(".");
			return semanticTargetId.substring(start+1, semanticTargetId.length());
		}else if(type == TYPE.Entity){
			int start = semanticTargetId.indexOf(":"); //remove Yago:
			if(start >0)
				return semanticTargetId.substring(start+1, semanticTargetId.length());
			
		}
		return semanticTargetId;
	}
	public void setSemanticTargetId(String semanticTargetId) {
		this.semanticTargetId = semanticTargetId;
	}
	
	
	public TYPE getType() {
		return type;
	}
	public void setType(TYPE type) {
		this.type = type;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public void normalizeScore(double sum_weights) {
		this.score /= sum_weights;
		
	}
	public void setCategories(String[] categories) {
		this.cats = categories;
	}
	public String[] getCategories() {
		
		return cats;
	}
	public String getSemanticTargetIdForGraph() {
		
		return semanticTargetId+"_"+unique_id;
	}
	public static void reset_counter() {
		count =0;
		
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url){
		this.url = url;
	}
}
