package webservice.model;


public class Annotation {

	private int rowIndx;
	private int colIndx;
	private int startOffset;
	private int endOffset;
	private String annotation;
	private String mention;
	private String semantic_target_ID;
	private Double score;
	private String semantic_target_URL;
	public int getRowIndx() {
		return rowIndx;
	}
	public void setRowIndx(int rowIndx) {
		this.rowIndx = rowIndx;
	}
	public int getColIndx() {
		return colIndx;
	}
	public void setColIndx(int colIndx) {
		this.colIndx = colIndx;
	}
	public int getStartOffset() {
		return startOffset;
	}
	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
	public int getEndOffset() {
		return endOffset;
	}
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}
	public String getAnnotation() {
		return annotation;
	}
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}
	public String getMention() {
		return mention;
	}
	public void setMention(String mention) {
		this.mention = mention;
	}
	public String getSemantic_target_ID() {
		return semantic_target_ID;
	}
	public void setSemantic_target_ID(String semantic_target_ID) {
		this.semantic_target_ID = semantic_target_ID;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public String getSemantic_target_URL() {
		return semantic_target_URL;
	}
	public void setSemantic_target_URL(String semantic_target_URL) {
		this.semantic_target_URL = semantic_target_URL;
	}
}
