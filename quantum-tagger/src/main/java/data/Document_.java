package data;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import javax.json.JsonObject;

import knowledgebase.Candidate;


import webservice.model.Document;


import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import graph.Graph;
import annotations.Annotation;
import annotations.Entity;
import annotations.Quantitiy;

public abstract class Document_ {

	public static enum DOCUMENT_TYPE {
		WEB
	};

	private static Logger slogger_ = LoggerFactory.getLogger(Document_.class);

	private String content_s;
	protected StringBuilder annotated_ner;
	private String title_s;
	private String full_content;
	protected List<Table_> table_l;
	private DOCUMENT_TYPE type;
	protected String Id;
	protected String file_name;
	private Multimap<String, Annotation> annotations;

	private Multimap<String, Candidate> candidates;
	private Map<String, Pair<Candidate, Double>> results;
	private PriorityQueue<Annotation> all_annotations;

	protected Graph graph;
//	private String radio_buttons = " <span style=\"width: 250px ; border:2px solid red; white-space: nowrap;padding:3px;\"> <input type=\"radio\" name=\"cb_doc_#Annotation_ID#\" value=\"Right\" checked /> Correct "
//			+ "  <input type=\"radio\" name=\"cb_doc_#Annotation_ID#\" value=\"Wrong\"/> Incorrect"
//			+ "\\-> <input type=\"text\" name=\"txt_doc_#Annotation_ID#\"/>"
//			+ "<input type=\"hidden\" name=\"hdn_doc_#Annotation_ID#\" value=\"#SemanticTargetID#\"></span>";

	private String experiement_id;

	public Document_() {

		annotations = HashMultimap.create();
		annotated_ner = new StringBuilder();
	}

	public Document_(String title, String content) {
		this.title_s = title;
		this.content_s = content;
		all_annotations = new PriorityQueue<Annotation>(new Comparator<Annotation>() {
			public int compare(Annotation entry1, Annotation entry2) {
				if (entry1.getStartOffset() < entry2.getStartOffset())
					return -1;
				else if (entry1.getStartOffset() == entry2.getStartOffset())
					return 0;
				else
					return 1;

			}
		});
		annotations = HashMultimap.create();
		annotated_ner = new StringBuilder();
	}

	public static Logger getSlogger() {
		return slogger_;
	}

	public String getContent() {
		return content_s;
	}



	public String getTitle() {
		return title_s;
	}

	protected void setTitle(String title_s) {
		this.title_s = title_s;
	}

	public List<Table_> getTables() {
		return table_l;
	}

	protected void setTables(List<Table_> web_table_l) {
		this.table_l = web_table_l;
	}

	public DOCUMENT_TYPE getType() {
		return type;
	}

	protected void setType(DOCUMENT_TYPE type) {
		this.type = type;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public abstract void addJsonTable(JsonObject json_table, String title, int id);

	public abstract void addJsonTable(JsonObject json_table, int id);

	public void addTable(Table_ table) {
		if (this.table_l == null) {
			this.table_l = new LinkedList<Table_>();
		}
		table_l.add(table);
	}

	public void merge(Document_ document) {
		this.table_l.addAll(document.getTables());

	}

	public Table_ getTable(int table_index) {

		return this.table_l.get(table_index);
	}

	public void setAnnotations(Multimap<String, Annotation> annotations) {
		if (annotations != null)
			this.annotations = annotations;

	}

	public void addAnnotations(String key, int row, int column, int startOffset, int endOffset, String annotaion) {

		addAnnotations(key, row, column, startOffset, endOffset, Annotation.ANNOTATION.valueOf(annotaion));
	}

	public void addAnnotations(String key, int row, int column, int startOffset, int endOffset,
			Annotation.ANNOTATION annotation) {
		if (key.trim().isEmpty())
			return;
		Annotation annotationObj = null;
		if (annotation == Annotation.ANNOTATION.DATE || annotation == Annotation.ANNOTATION.PERCENT
				|| annotation == Annotation.ANNOTATION.MONEY || annotation == Annotation.ANNOTATION.TIME) {
			annotationObj = new Quantitiy(row, column, startOffset, endOffset, annotation);
			annotated_ner.append(" " + key.trim() + " ");
		} else if (annotation == Annotation.ANNOTATION.OTHER_QUANTITY) {
			if (annotationExists(key, startOffset, endOffset))
				return;// do nothing if already marked
			annotationObj = new Quantitiy(row, column, startOffset, endOffset, annotation); // do
																							// not
																							// add
																							// it
																							// to
																							// the
																							// annotated
																							// ner
		} else {

			annotationObj = new Entity(row, column, startOffset, endOffset, annotation);
			annotated_ner.append(" [[" + key.trim() + "]] ");
			// mark the annotation in the annotated string
		}
		// TODO overlapping annotations check
		annotations.put(key, annotationObj);
		all_annotations.add(annotationObj);

	}

	private boolean annotationExists(String key, int startOffset, int endOffset) {
		if (all_annotations != null) {
			for (Annotation annotation : all_annotations) {
				if (annotation.getStartOffset() == startOffset)
					return true;
				else if (annotation.getStartOffset() <= startOffset && annotation.getEndOffset() >= startOffset) {
					return true;
				} else if (annotation.getStartOffset() <= endOffset && annotation.getEndOffset() >= endOffset)
					return true;
			}
		}
		return false;
	}

	/**
	 * Process tha annotations of the context only generated from Stanford NER
	 * 
	 * @param triples
	 *            the NER tags of the context
	 */
	public void processAnnotations(List<Triple<String, Integer, Integer>> triples) {
		String currentTag = null;
		String key;
		annotated_ner = new StringBuilder();
		int start = 0;
		int end = 0;
		int indx = 0;
		for (Triple<String, Integer, Integer> triple : triples) {

			currentTag = triple.first;
			start = triple.second;
			end = triple.third;
			key = content_s.substring(start, end);
			if (start > indx) {
				annotated_ner.append(content_s.substring(indx, start - 1));// copy
																			// the
																			// part
																			// of
																			// the
																			// text
																			// before
																			// the
																			// mention
				indx = end + 1;// advance the index to the token after
								// the mention
			}
			addAnnotations(key, -1, -1, start, end, currentTag);
			currentTag = null;
			start = end = 0;
			key = "";
		}

		if (indx < content_s.length() - 1) {
			annotated_ner.append(content_s.substring(indx, content_s.length()));
		}

	}

	public void propagateAnnotationsToTables() {
		if (annotations == null)
			return;
		for (Table_ table : table_l) {
			for (Entry<String, Annotation> entry : annotations.entries()) {
				table.annotate(entry.getKey(), entry.getValue().getAnnotation());
			}
		}

	}

	public Multimap<String, Annotation> getMentions() {
		return this.annotations;

	}

	public void setCandidates(Multimap<String, Candidate> candidates2) {
		this.candidates = candidates2;

	}

	public String getAnnotatedContents() {

		return annotated_ner.toString();
	}

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public Set<Candidate> getCandidates(String mention) {
		return (Set<Candidate>) candidates.get(mention);

	}

	public int mentionCount() {

		if (annotations != null)
			return annotations.size();
		else
			return 0;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;

	}

	public Graph getGraph() {

		return this.graph;
	}

	public Multimap<String, Candidate> getAllCandidates() {

		return candidates;
	}

	public abstract void saveGraph(String prefix, boolean write_potential_edges) throws IOException;

	public void setResult(String mention, Candidate winner, double max) {
		if (results == null) {
			results = new HashMap<String, Pair<Candidate, Double>>();
		}
		if (!results.containsKey(mention))
			results.put(mention, new Pair<Candidate, Double>(winner, max));

	}

	public String getAnnotatedContentsWithResults() {
		// link: <p><a
		// href="https://en.wikipedia.org/wiki/Egypt">EGYPT|&lt;EGYPT&gt;</a></p>
		String annotated_results = content_s;
		String temp;
		String uniqueID;
		int diff = 0;
		Annotation annotation;
		for (Entry<String, Annotation> mention : annotations.entries()) {
			annotation = mention.getValue();
			uniqueID = mention.getKey() + "_" + annotation.getUniqueID();
			if (results != null && results.containsKey(uniqueID)) {
				temp = "[[<span style=\"color: green;\"><strong>" + mention.getKey()
						+ "</strong></span>|<span style=\"color: red;\">" + " <a target=\"_blank\" href=\""
						+ results.get(uniqueID).first.getUrl() + "\" >"
						+ results.get(uniqueID).first.getFullSemanticTargetId()
						+ " </a></span>| score= <span style=\"color: blue;\"><em>" + results.get(uniqueID).second
						+ "</em></span>]]";
			} else {
				temp = "[[<span style=\"color: green;\"><strong>" + mention.getKey()
						+ "</strong></span>|<span style=\"color: red;\"><em>" + " NULL </em></span>]]";
			}
			if (diff + annotation.getStartOffset() > annotated_results.length())
				continue;
			else {
				annotated_results = annotated_results.substring(0, diff + annotation.getStartOffset() - 1) + temp
						+ annotated_results.substring(diff + annotation.getEndOffset(), annotated_results.length() - 1);
				diff += temp.length() - mention.getKey().length() - 1;
			}

		}

		return annotated_results;
	}

	public String getAnnotatedContentsWithResults2() {
		if (this.annotations == null || this.annotations.size() == 0)
			return content_s;
		StringBuilder annotated_results = new StringBuilder();
		String temp;
		String uniqueID;
		int start = 0, end = 0, indx = 0;
		String mention;
		//String options_list = null;
		String url = null, full_semantic_target_id = null;
		Annotation annotation;
		PriorityQueue<Annotation> annotations_copy = new PriorityQueue<Annotation>(new Comparator<Annotation>() {
			public int compare(Annotation entry1, Annotation entry2) {
				if (entry1.getStartOffset() < entry2.getStartOffset())
					return -1;
				else if (entry1.getStartOffset() == entry2.getStartOffset())
					return 0;
				else
					return 1;
			}
		});
		while ((annotation = all_annotations.poll()) != null) {
			start = annotation.getStartOffset();
			end = annotation.getEndOffset();
			mention = content_s.substring(start, end);
			uniqueID = mention + "_" + annotation.getUniqueID();
			//options_list = getAllCandidatesAsComboBox(uniqueID);
			if (results != null && results.containsKey(uniqueID)) {
				url = StringEscapeUtils.escapeHtml4(results.get(uniqueID).first.getUrl());
				full_semantic_target_id = StringEscapeUtils.escapeHtml4(
						StringEscapeUtils.unescapeJava(results.get(uniqueID).first.getFullSemanticTargetId()));
				full_semantic_target_id = full_semantic_target_id.replace("YAGO:", "");
				temp = "<strong><em>[[<span style=\"color: green;\"><strong>"
						+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(mention))
						+ "</strong></span>|<span style=\"color: red;\"><em>" + " <a target=\"_blank\" href=\"" + url + "\" >"
						+ full_semantic_target_id + " </a> </em></span>| score= <span style=\"color: blue;\"><em>"
						+ results.get(uniqueID).second + "</em></span>]]</em></strong>";
						// + radio_buttons.replace("#Annotation_ID#",
						// String.valueOf(annotation.getUniqueID()))
						// .replace("#SemanticTargetID#",
						// full_semantic_target_id)
					//	+ options_list.replace("#Annotation_ID#", String.valueOf(annotation.getUniqueID()));
			} else {
				temp = "<strong><em>[[<span style=\"color: green;\"><strong>"
						+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(mention))
						+ "</strong></span>|<span style=\"color: red;\"><em>" + " NULL </em></span>]]</em></strong>";
//						+ radio_buttons.replace("#Annotation_ID#", String.valueOf(annotation.getUniqueID()))
//								.replace("#SemanticTargetID#", "NULL");
						//+ options_list;
			}
			if (start > indx) {
				annotated_results.append(StringEscapeUtils.escapeHtml4(content_s.substring(indx, start - 1)));

			}
			annotated_results.append(temp);
			indx = end + 1;// advance the index to the token after
			// the mention
			annotations_copy.offer(annotation);
		}
		if (indx < content_s.length() - 1) {
			annotated_results.append(StringEscapeUtils.escapeHtml4(content_s.substring(indx, content_s.length())));
		}
		all_annotations = annotations_copy; // refill
		//writeHTMLResultstoDB(annotated_results.toString().replace("'", "''"));
		return annotated_results.toString();
	}

	/*private String getAllCandidatesAsComboBox(String uniqueID) {
		Set<Candidate> candidates = (Set<Candidate>) this.candidates.get(uniqueID);
		if (candidates == null)
			return " ";
		StringBuilder cmbx_html = new StringBuilder();
		cmbx_html.append("<span style=\"width: 250px ; border:2px solid;" + " white-space: nowrap;padding:3px;\">"
				+ "<select name=\"cmb_doc_#Annotation_ID#\">\n");
		cmbx_html.append("<option value=\"\"></option>");

		for (Candidate cand : candidates) {

			cmbx_html.append("<option value=\""
					+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(cand.getFullSemanticTargetId()))
					+ "\">"
					+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(cand.getFullSemanticTargetId()))
					+ "</option>\n");
		}
		cmbx_html.append("<option value=\"NULL\">OOKB</option>");
		cmbx_html.append("</select> </span><br/>");
		return cmbx_html.toString();
	}*/

	public void addCandidateEntitie(Map<String, List<Candidate>> entities) {
		if (entities == null)
			return;
		Set<Annotation> mention_annotations;
		String id, mention;
		for (Entry<String, List<Candidate>> mention_candidates : entities.entrySet()) {
			mention = mention_candidates.getKey().split("_")[0];
			mention_annotations = (Set<Annotation>) annotations.get(mention); // get
																				// all
			// annotaions for
			// that mention
			for (Annotation annotation : mention_annotations) {
				id = mention + "_" + annotation.getUniqueID();
				if (!candidates.containsKey(id)) { // TODO check for the shared
													// Candidates
					candidates.putAll(id, new ArrayList<Candidate>( mention_candidates.getValue()));
					break;
				}
			}

		}
	}

	public void deleteResults() {
		if (results != null)
			results.clear();
		for (Table_ table : this.table_l) {
			table.deleteResults();
		}
	}

	public Map<String, Pair<Candidate, Double>> getResults() {

		return results;
	}

	public String getExperiement_id() {

		return this.experiement_id;
	}

	public void setExperiement_id(String experiement_id) {
		this.experiement_id = experiement_id;
		for (Table_ table : table_l) {
			table.setExperiement_id(experiement_id);
		}
	}

	public Multimap<String, Annotation> getAnnotations() {
		return annotations;
	}

	public abstract void writeDocumentWithAnnotationsToDB();

	public abstract void writeResultstoDB();

	public abstract void writeHTMLResultstoDB(String html_results);
	// /**
	// * Process all the annotations from Stanfored NER
	// *
	// * @param sentences
	// * the Stanford COre NLP output contian the NER tags and the pos
	// */
	// public void processPOSAnnotations(
	// List<CoreMap> sentences) {
	// String word, ne,pos;
	// int start , end;
	// // first process the annotations for the document
	// for (CoreMap sentence : sentences) {
	// for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
	//
	// word = token.get(TextAnnotation.class);
	// pos = token.get(PartOfSpeechAnnotation.class);
	// ne = token.get(NamedEntityTagAnnotation.class);
	// System.out.println(word +" " + pos + " "+ ne);
	// end = token.endPosition();
	// start = token.beginPosition();
	//
	//
	// }
	//
	// }
	//
	//
	// }

	public void setShortContent(String page_short_content) {
		if (full_content == null || full_content.isEmpty())
			this.full_content = this.content_s;
		this.content_s = page_short_content;

	}

	public void setNumberInternalLinks(int value) {
		table_l.get(0).setInternalLinksCount(value);

	}

	public boolean continsKeyword(String keyword) {

		boolean result = false;
		if (this.content_s != null)
			result = this.content_s.contains(keyword);
		if (this.title_s != null)
			result |= this.title_s.contains(keyword);
		return result;
	}

	public void deleteAllMentions() {
		annotations.clear();
		all_annotations.clear();
		candidates.clear();
	}

	public void writeHTMLResultstoPOJO (Document doc){		
		Table_ table = getTable(0);
		String table_header, table_body, document_html;
		document_html = getAnnotatedContentsWithResults2();
		table_header = table.getAnnotatedHeaderWithResults().toString();
		table_body = table.getAnnotatedBodyWithResults().toString();
		doc.setDocument_html(document_html);
		doc.setTable_body(table_body);
		doc.setTable_header(table_header);		
	}

	public void writeAnnotationstoPOJO(Document doc) {
		
		Table_ table = getTable(0);
		table.writeAnnotationsToPOJO(doc);
		if(getResults() == null)
			return;
	
		String mention;
		Annotation annotation;
		Pair<Candidate,Double> result;
		for(Entry<String,Annotation> annotation_entry : this.annotations.entries()){
			mention = annotation_entry.getKey();
			annotation = annotation_entry.getValue();
			if(this.results.containsKey(mention+"_"+annotation.getUniqueID())){
				result = results.get(mention+"_"+annotation.getUniqueID());
				doc.addContextAnnotation(annotation.getStartOffset(), annotation.getEndOffset(),
						annotation.getAnnotation(), mention,
						StringEscapeUtils.unescapeJava(result.first.getFullSemanticTargetId()).trim(), result.second,
						result.first.getUrl());
			}else{
				doc.addContextAnnotation(annotation.getStartOffset(), annotation.getEndOffset(),
						annotation.getAnnotation(), mention,
						"NULL", 0.0,"");
			}
			
		}
	}

	// public String getContentAndTables() {
	// if (all_content == null || all_content.isEmpty()) {
	// StringBuilder all_content = new StringBuilder();
	// all_content.append(this.content_s);
	// for (int i = 1; i <= table_l.size(); i++) {
	// all_content.append("TABLE:#" + String.valueOf(i) + "\n");
	// all_content.append(table_l.get(i - 1).getAsString());
	// //all_content.append("END TABLE:#" + String.valueOf(i) + "\n");
	// }
	// this.all_content = all_content.toString();
	// }
	// return this.all_content;
	// }
	//
	// /**
	// * this method process the annotations of the copmbined context and tables
	// * @param triples
	// */
	// public void processNEAnnotations(List<Triple<String, Integer, Integer>>
	// triples) {
	//
	// String currentTag = null;
	// StringBuilder key = new StringBuilder();
	// annotated_ner = new StringBuilder();
	// int start = 0;
	// int end = 0;
	// int indx = 0;
	// String tag, token;
	// for (Triple<String, Integer, Integer> triple : triples) {
	// start = triple.second;
	// end = triple.third;
	// tag = triple.first;
	// token = all_content.substring(start, end)
	// if(start >= content_s.length()){
	// processTableAnnotation(token, tag, start, end);
	// }else{
	// processDocAnnotation(token, tag, start, end);
	// }
	//
	// }
	//
	// }
	//
	// private void processDocAnnotation(String token, String tag, int start,
	// int end) {
	// addAnnotations(token, -1, -1, start, end,tag);
	//
	// }
	//
	// private void processTableAnnotation(String token,String tag, int start,
	// int end) {
	// // TODO Auto-generated method stub
	//
	// }

}
