package webservice.model;

import java.util.ArrayList;
import java.util.List;

import annotations.Annotation.ANNOTATION;

public class Document {

	private String title;
	private String context;
	private String table_title;
	private String table_content;
	
	private String document_results;
	private String document_html;
	private String table_header;
	private String table_body;
	private int max_itr;
	private double gamma;
	private double hp_same_string;
	private double hp_same_row;
	private double hp_same_column;
	private double hp_header_cell;
	private double hp_mention_candidate;
	private double hp_candidate_candidate;
	private List<Annotation> table_annotations;
	private List<Annotation> context_annotations;
	
	
	public int getMax_itr() {
		return max_itr;
	}
	public void setMax_itr(int max_itr) {
		this.max_itr = max_itr;
	}
	public double getGamma() {
		return gamma;
	}
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}
	public double getHp_same_string() {
		return hp_same_string;
	}
	public void setHp_same_string(double hp_same_string) {
		this.hp_same_string = hp_same_string;
	}
	public double getHp_same_row() {
		return hp_same_row;
	}
	public void setHp_same_row(double hp_same_row) {
		this.hp_same_row = hp_same_row;
	}
	public double getHp_same_column() {
		return hp_same_column;
	}
	public void setHp_same_column(double hp_same_column) {
		this.hp_same_column = hp_same_column;
	}
	public double getHp_header_cell() {
		return hp_header_cell;
	}
	public void setHp_header_cell(double hp_header_cell) {
		this.hp_header_cell = hp_header_cell;
	}
	public double getHp_mention_candidate() {
		return hp_mention_candidate;
	}
	public void setHp_mention_candidate(double hp_mention_candidate) {
		this.hp_mention_candidate = hp_mention_candidate;
	}
	public double getHp_candidate_candidate() {
		return hp_candidate_candidate;
	}
	public void setHp_candidate_candidate(double hp_candidate_candidate) {
		this.hp_candidate_candidate = hp_candidate_candidate;
	}
	public String getDocument_html() {
		return document_html;
	}
	public void setDocument_html(String document_html) {
		this.document_html = document_html;
	}
	public String getTable_header() {
		return table_header;
	}
	public void setTable_header(String table_header) {
		this.table_header = table_header;
	}
	public String getTable_body() {
		return table_body;
	}
	public void setTable_body(String table_body) {
		this.table_body = table_body;
	}
	
	
	public String getDocument_results() {
		return document_results;
	}
	public void setDocument_results(String document_results) {
		this.document_results = document_results;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String content) {
		this.context = content;
	}
	public String getTable_title() {
		return table_title;
	}
	public void setTable_title(String table_title) {
		this.table_title = table_title;
	}
	public String getTable_content() {
		return table_content;
	}
	public void setTable_content(String table_content) {
		this.table_content = table_content;
	}

	public void addTableAnnotation(int rowIndx, int colIndx, int startOffset, int endOffset, ANNOTATION annotation_name,
			String mention, String semantic_target_ID, Double score, String semantic_target_URL) {
		Annotation annotation = new Annotation();
		annotation.setRowIndx(rowIndx);
		annotation.setColIndx(colIndx);
		annotation.setStartOffset(startOffset);
		annotation.setEndOffset(endOffset);
		annotation.setAnnotation(annotation_name.name());
		annotation.setMention(mention);
		annotation.setSemantic_target_ID(semantic_target_ID);
		annotation.setScore(score);
		annotation.setSemantic_target_URL(semantic_target_URL);
		if(this.table_annotations == null){
			this.table_annotations = new ArrayList<Annotation>();
		}
		table_annotations.add(annotation);
		
	}
	public void addContextAnnotation(int startOffset, int endOffset, ANNOTATION annotation_name, String mention, String semantic_target_ID,
			Double score, String semantic_target_URL) {
		Annotation annotation = new Annotation();
		annotation.setStartOffset(startOffset);
		annotation.setEndOffset(endOffset);
		annotation.setAnnotation(annotation_name.name());
		annotation.setMention(mention);
		annotation.setSemantic_target_ID(semantic_target_ID);
		annotation.setScore(score);
		annotation.setSemantic_target_URL(semantic_target_URL);
		if(this.context_annotations == null){
			this.context_annotations = new ArrayList<Annotation>();
		}
		context_annotations.add(annotation);		
	}
	
	public List<Annotation> getTable_annotations() {
		return table_annotations;
	}
	public void setTable_annotations(List<Annotation> table_annotations) {
		this.table_annotations = table_annotations;
	}
	public List<Annotation> getContext_annotations() {
		return context_annotations;
	}
	public void setContext_annotations(List<Annotation> context_annotations) {
		this.context_annotations = context_annotations;
	}
		
	
	
}
