package annotations;



public abstract class Annotation {
	// the most basic annotaions 
	// the row index, column index , and the start and end positions
	public static enum ANNOTATION{ LOCATION,
		  ORGANIZATION,
		  DATE,
		  MONEY,
		  PERSON,
		  PERCENT,
		  TIME, 
		  OTHER_QUANTITY,
		  CLASS,
		  CONCEPT,
		  ENTITY};
	private int rowIndx, colIndx;
	private int startOffset, endOffset;
	private Annotation.ANNOTATION annotation;
	private static int count =0;
	private int uniqueID;
	private String gold_standard;
	private int gold_standard_id;
	public Annotation(int rowIndx, int columnIndx, int startOffset, int endOffset, Annotation.ANNOTATION  annotation){
		this.rowIndx= rowIndx;
		this.colIndx = columnIndx;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this. annotation = annotation;
		this.uniqueID = count++; //TODO reset
	}
	public int getRowIndx() {
		return rowIndx;
	}
	
	public int getColIndx() {
		return colIndx;
	}
	
	public int getStartOffset() {
		return startOffset;
	}
	
	public int getEndOffset() {
		return endOffset;
	}
	
	public Annotation.ANNOTATION  getAnnotation() {
		return annotation;
	}
	public int getUniqueID() {
		
		return this.uniqueID;
	}
	public static void reset_counter() {
		count =0;
		
	}
	public void setEndOffset(int endOffset2) {
		this.endOffset = endOffset2;
		
	}
	public String getGold_standard() {
		return gold_standard;
	}
	public void setGold_standard(int KBID, String gold_standard) {
		this.gold_standard = gold_standard;
		this.gold_standard_id = KBID;
	}
	public int getGold_standard_id() {
		return gold_standard_id;
	}
	
	
	
	
}
