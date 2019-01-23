package data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import knowledgebase.Candidate;
import knowledgebase.Candidate.TYPE;
import knowledgebase.CandidateEntity;
import loader.DocumentLoader;
import webservice.model.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Annotation;
import annotations.Annotation.ANNOTATION;
import annotations.Class_;
import annotations.Concept;
import annotations.Entity;
import annotations.Quantitiy;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;

import edu.stanford.nlp.util.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public abstract class Table_ {

	public static enum TABLE_TYPE {
		column, row
	};

	private static Pattern numeric_pattern = Pattern.compile("([^0-9]*)[-+]?(\\d*\\.\\d+|\\d+)(.*)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	protected static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	private TABLE_TYPE type;
	private String title_s;
	private String footer_s;
	private String content_s;
	private int ncol, nrow;
	protected int id = 0;
	protected String document_id;
	protected String table_caption;
	protected int internal_links_count;
	@SuppressWarnings("unused")
	private String[] rows_a;
	private List<String[]> columns_a;
	private boolean[] numeric = null;
	private boolean[] mixed = null;
	/**
	 * mention -> Annotation
	 */
	protected Multimap<String, Annotation> annotations;
	/**
	 * mention -> List of candidates
	 */
	private Multimap<String, Candidate> candidates;
	private Map<String, Pair<Candidate, Double>> results;
	private StringBuilder annotated_ner = new StringBuilder();
	private String processed_content = "";
	private Multimap<Pair<Integer, Integer>, Pair<String, Annotation>> inverted_annotations;


	private boolean missing_annotation = false, no_entities = false;

	protected String experiement_id = "test";

	public void setExperiement_id(String experiement_id) {
		this.experiement_id = experiement_id;
	}

	public Table_(String title, String content) {
		this.title_s = title;
		this.content_s = content;
		annotations = HashMultimap.create();
		inverted_annotations =  HashMultimap.create();
		candidates = HashMultimap.create();
	}

	public static Logger getSlogger() {
		return slogger_;
	}

	public TABLE_TYPE getType() {
		return type;
	}

	protected void setType(TABLE_TYPE type) {
		this.type = type;
	}

	public String getTitle_s() {
		return title_s;
	}

	protected void setTitle(String titel_s) {
		this.title_s = titel_s;
	}

	public String getFooter() {
		return footer_s;
	}

	protected void setFooter(String footer_s) {
		this.footer_s = footer_s;
	}

	public String getContent() {
		return content_s;
	}

	public void setContent(String content) {
		this.content_s = content;
	}

	public int getNcol() {
		return ncol;
	}

	public void setNcol(int ncol) {
		this.ncol = ncol;
		columns_a = new ArrayList<String[]>(nrow);
	}

	public int getNrow() {
		return nrow;
	}

	public void setNrow(int nrow) {
		this.nrow = nrow;
	}

	public void setRows(String[] rows) {
		this.rows_a = rows;
	}

	public void setColumns(String[] row, int row_indx) {

		if (row.length > ncol)
			return;
		// if (isEmpty(row))
		// return;
		columns_a.add(row_indx, row);
		// updateNumeric(row_indx);

	}

	public void setId(int id) {
		this.id = id;
	}

	/*
	 * private void updateNumeric(int row_indx) { if (numeric == null) { numeric
	 * = new boolean[ncol]; setTrue(numeric); } if (row_indx != 0) { for (int i
	 * = 0; i < ncol; i++) { if (columns_a[row_indx][i] == null ||
	 * columns_a[row_indx][i].trim().isEmpty()) { continue; } if
	 * (!containsNumeric(columns_a[row_indx][i])) { numeric[i] = false; } } }
	 * 
	 * }
	 */

	private boolean containsNumeric(String cell) {
		Matcher matcher = Table_.numeric_pattern.matcher(cell);
		return matcher.find();
	}

	private void setTrue(boolean[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = true;
		}

	}

	private void setFalse(boolean[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = false;
		}

	}

	public List<String[]> getCells() {

		return columns_a;
	}

	public void setAnnotaions(Multimap<String, Annotation> annotations) {
		this.annotations = annotations;
	}

	public void addAnnotations(String key, int row, int column, int startOffset, int endOffset, String annotaion) {
		addAnnotations(key, row, column, startOffset, endOffset, Annotation.ANNOTATION.valueOf(annotaion));
	}

	public void addAnnotation(String key, int row, int column, Annotation annotation) {

		annotations.put(key, annotation);
		Pair<Integer, Integer> ref = Pair.makePair(row, column);
		inverted_annotations.put(ref,Pair.makePair(key, annotation));

	}

	public void addAnnotations(String key, int row, int column, int startOffset, int endOffset,
			Annotation.ANNOTATION annotation) {

		key = key.trim();
		if (key.isEmpty())
			return;
		// check if the annotation already exists
		Set<Pair<String, Annotation>> cell_annotaions = (Set<Pair<String, Annotation>>) inverted_annotations
					.get(new Pair<Integer, Integer>(row, column));

			if (cell_annotaions != null) {
				for (Pair<String, Annotation> single_annotaion : cell_annotaions) {
					if (single_annotaion.first.equals(key)) {
						if (annotation == ANNOTATION.ENTITY || annotation == ANNOTATION.OTHER_QUANTITY) {
							// added entities from annotation enrichment might
							// be repeated
							return;
						} else if (single_annotaion.second.getAnnotation() == annotation) {
							return;// if both annotations are equal
						} else if (annotation == ANNOTATION.CONCEPT || annotation == ANNOTATION.CLASS) {
							return;
						}
					}
				}
			}		

		Annotation annotationObj = null;
		if (annotation == Annotation.ANNOTATION.DATE || annotation == Annotation.ANNOTATION.PERCENT
				|| annotation == Annotation.ANNOTATION.MONEY || annotation == ANNOTATION.OTHER_QUANTITY) {
			annotationObj = new Quantitiy(row, column, startOffset, endOffset, annotation);
		} else if (row != 0) {
			annotationObj = new Entity(row, column, startOffset, endOffset, annotation);
		} else if (annotation == Annotation.ANNOTATION.CONCEPT) {
			// either class or concept depends if the column has numerical
			// values
			annotationObj = new Concept(row, column, startOffset, endOffset, annotation);
		} else {
			annotationObj = new Class_(row, column, startOffset, endOffset, annotation);
		}
		addAnnotation(key, row, column, annotationObj);
		

	}

	public Multimap<Pair<Integer, Integer>,Pair<String, Annotation>> getInverted_annotations() {
		return inverted_annotations;
	}

	public void processAnnotaions(String result) {
		String[] lines = result.split("\n");

		int col = 0, row = 0;
		StringBuilder tokens = new StringBuilder();
		String currentTag = null;
		String[] annotation = null;
		String line;
		boolean flag = false;
		for (int i = 0; i < lines.length; i++) {
			if (row >= nrow || col >= ncol) {
				break;
			}
			line = lines[i].trim();
			if (line.isEmpty())
				continue;
			if (line.trim().startsWith(".\t")) {
				flag = true;
				// continue;
			}
			if (line.startsWith("|\t") && flag) {
				flag = false;
				col++;// increment the column index
				continue;
			} else if (line.startsWith("#\t") && flag) {
				flag = false;
				row++;
				col = 0;
				continue;
			}
			annotation = lines[i].split("\t");
			// if(annotation[1].equals("O"))
			// continue;
			if (currentTag != null && !currentTag.equals("O")) {
				if (currentTag.equals(annotation[1])) {// collect the annotation
														// tokens
					tokens.append(isPunctuation(annotation[0]) ? annotation[0] : " " + annotation[0]);
				} else {// mark the annotation
					String key = tokens.toString();
					int[] offset = find(key.trim(), columns_a.get(row)[col]);
					if (offset[0] == -1) {
						missing_annotation = true;
						slogger_.warn("Unmatched annotation in the table: " + key + ", cell content: "
								+ columns_a.get(row)[col]);
						tokens = new StringBuilder();
						currentTag = null;
						if (!annotation[1].equals("O")) {
							currentTag = annotation[1];
							tokens.append(isPunctuation(annotation[0]) ? annotation[0] : " " + annotation[0]);
						}
						continue;// mismatch

					} else {

						addAnnotations(key, row, col, offset[0], offset[1], currentTag);
					}
					tokens = new StringBuilder();
					currentTag = null;
					if (!annotation[1].equals("O")) {
						currentTag = annotation[1];
						tokens.append(isPunctuation(annotation[0]) ? annotation[0] : " " + annotation[0]);
					}
				}
			} else if (!annotation[1].equals("O")) {
				currentTag = annotation[1];
				tokens.append(isPunctuation(annotation[0]) ? annotation[0] : " " + annotation[0]);
			}

		}
	}

	private int[] find(String key, String cell) {

		int[] offset = new int[2];
		offset[0] = -1;
		if (cell == null)
			return offset;

		offset[0] = cell.indexOf(key);
		offset[1] = offset[0] + key.length() - 1;
		if (offset[0] == -1) {
			offset[0] = cell.indexOf(key.replace(" ", ""));
			offset[1] = offset[0] + key.replace(" ", "").length() - 1;
		}
		if (offset[0] == -1) {
			if (StringUtils.getJaroWinklerDistance(key, cell) >= 0.9) {
				offset[0] = 0;
				offset[1] = cell.length() - 1;
			}
		}
		if (offset[0] == -1) {
			String[] tokens = key.split(" ");
			if (tokens != null && tokens.length > 0) {
				offset[0] = cell.indexOf(tokens[0]); // # replacing some unicode
														// characters need to be
														// looked at
				offset[1] = cell.indexOf(tokens[tokens.length - 1]) + tokens[tokens.length - 1].length();
			}
		}
		return offset;
	}

	private boolean isPunctuation(String string) {
		String punctuations = ".,:;";// "[]{}()'\"%-\\/.,:;$#@!`~";
		if (punctuations.contains(string))
			return true;
		return false;
	}

	public String getAsString() {
		StringBuilder table_content;
		if (processed_content == null || processed_content.isEmpty()) {
			table_content = new StringBuilder();
			for (int i = 0; i < nrow; i++) {
				String[] columns = columns_a.get(i);
				if (!isEmpty(columns)) {
					if (i == 0)
						table_content.append(Joiner.on(" .| ").join(columns));
					else
						table_content.append(".#" + Joiner.on(" .| ").join(columns));
				}
			}
			processed_content = table_content.toString();
		}
		if (processed_content.endsWith(" .| "))
			processed_content = processed_content.substring(0, processed_content.length() - 4);
		return processed_content;
	}

	private boolean isEmpty(String[] columns) {
		boolean empty = true;
		for (String column : columns) {
			if (column != null && !column.isEmpty()) {
				empty = false;
				return empty;
			}
		}
		return empty;
	}

	/**
	 * This method takes a key and annotation from the text and find a match of
	 * it in the table contents, and annotate it
	 * 
	 * @param mention
	 *            the mention string in the text
	 * @param annotation
	 *            the annotation from stanford
	 */
	public void annotate(String mention, Annotation.ANNOTATION annotation) {
		int start, end;
		if (annotations.containsKey(mention)) {
			// already exists, do nothing
			return;
		}
		for (int i = 0; i < nrow; i++) {
			for (int j = 0; j < ncol; j++) {// search for a match in the table
											// cells,
				// if there is an exact match
				if (columns_a.get(i)[j] == null)
					break;
				start = columns_a.get(i)[j].indexOf(mention);
				if (start > -1) {
					// add annotations
					end = start + mention.length();
					addAnnotations(mention, i, j, start, end, annotation);
				} else {
					double similarties = StringUtils.getJaroWinklerDistance(mention, columns_a.get(i)[j]);
					if (similarties >= 0.9) {
						// if there is an approximate match, here there may be
						// another annotation with similar key,
						start = 0;
						end = columns_a.get(i)[j].length();
						addAnnotations(columns_a.get(i)[j], i, j, start, end, annotation);
					}
				}
			}

		}
	}

	public boolean isNumericColumn(int j) {

		if (numeric == null)
			return false;
		return numeric[j];
	}

	public boolean isMixed(int j) {
		if (mixed == null)
			return false;
		return mixed[j];
	}

	public Multimap<String, Annotation> getMentions() {
		return annotations;
	}

	public String getCell(int row, int column) {
		return columns_a.get(row)[column];
	}

	public void setCandidates(Multimap<String, Candidate> table_candidates) {
		this.candidates = table_candidates;

	}

	public String getAnnotatedContents() {
		return annotated_ner.toString();
	}

	public void createAnnotatedNERText() {
		// TODO refine the annotations and remove the nested annotations
		Pair<Integer, Integer> temp;
		annotated_ner = new StringBuilder();
		StringBuilder extra_annotations = new StringBuilder();
		String cell_content;
		Set<Pair<String, Annotation>> annotations_set;
		for (int i = 0; i < nrow; i++) {
			if (i != 0)
				annotated_ner.append(" .# ");
			for (int j = 0; j < ncol; j++) {
				// if (!table.isNumericColumn(j))
				temp = Pair.makePair(i, j);
				cell_content = columns_a.get(i)[j];
				if (inverted_annotations.containsKey(temp)) {
					annotations_set = (Set<Pair<String, Annotation>>) inverted_annotations.get(temp);
					for (Pair<String, Annotation> annotation : annotations_set) {
						if (annotation.second.getAnnotation() == ANNOTATION.LOCATION
								|| annotation.second.getAnnotation() == ANNOTATION.ORGANIZATION
								|| annotation.second.getAnnotation() == ANNOTATION.PERSON
								|| annotation.second.getAnnotation() == ANNOTATION.ENTITY) {
							// check if the cell already annotated
							if (!cell_content.contains("[["))// (!nested(cell_content,
																// annotation.first.trim()))
								cell_content = cell_content.replace(annotation.first.trim(),
										"[[" + annotation.first.trim() + "]]");
							else //if (extra_annotations.indexOf("[[" + annotation.first.trim() + "]]") < 0)
								extra_annotations.append(" [[" + annotation.first.trim() + "]] "); 
						}
					}
				}
				annotated_ner.append(cell_content);
				if (j != ncol - 1)
					annotated_ner.append(" .| ");
			}
		}
		annotated_ner.append(extra_annotations.toString());
	}



	public Set<Candidate> getCandidates(String mention) {
		if (candidates != null && candidates.containsKey(mention))
			return (Set<Candidate>) candidates.get(mention);
		else
			return null;
	}

	public Multimap<String, Candidate> getCandidates() {

		return candidates;
	}

	public void setResult(String mention, Candidate winner, double max) {
		if (results == null) {
			results = new HashMap<String, Pair<Candidate, Double>>();
		}
		if (!results.containsKey(mention))
			results.put(mention, new Pair<Candidate, Double>(winner, max));

	}

	public CharSequence getAnnotatedHeaderWithResults() {
		return getAnnotatedRowWithResults(0);
	}

	public String getAnnotatedRowWithResults(int i) {
		StringBuilder annotated_results = new StringBuilder();
		Set<Pair<String, Annotation>> annotations;
		annotated_results.append("<tr>");
		String cell_content;
		String temp;
		String uniqueID;
		//String options_list;
		String url = null, full_semantic_target_id = null;
		PriorityQueue<Pair<Pair<Integer, Integer>, String>> all_content = new PriorityQueue<Pair<Pair<Integer, Integer>, String>>(
				new Comparator<Pair<Pair<Integer, Integer>, String>>() {
					public int compare(Pair<Pair<Integer, Integer>, String> entry1,
							Pair<Pair<Integer, Integer>, String> entry2) {
						return entry1.first.first.compareTo(entry2.first.first);

					}
				});
		Pair<Integer, Integer> position = null;

		for (int j = 0; j < ncol; j++) {
			cell_content = columns_a.get(i)[j];
			annotations = (Set<Pair<String, Annotation>>) inverted_annotations.get(new Pair<Integer, Integer>(i, j));
			if (!annotations.isEmpty()) {
				for (Pair<String, Annotation> annotaion : annotations) {
					uniqueID = annotaion.first + "_" + annotaion.second.getUniqueID();
					position = new Pair<Integer, Integer>(annotaion.second.getStartOffset(),
							annotaion.second.getEndOffset());
//					options_list = getAllCandidatesAsComboBox(uniqueID);

					if (results != null && results.containsKey(uniqueID)) {
						/*if (results.get(uniqueID).first.getType().equals(TYPE.QUANTITY)) {// if it is a quantitiy
							url = getQuantityURL(results.get(uniqueID).first, annotaion, j);
						} else*/
						url = StringEscapeUtils.escapeHtml4(results.get(uniqueID).first.getUrl());
						full_semantic_target_id = StringEscapeUtils.escapeHtml4(
								StringEscapeUtils.unescapeJava(results.get(uniqueID).first.getFullSemanticTargetId()));
						full_semantic_target_id = full_semantic_target_id.replace("YAGO:", "");
						temp = "[[ <span style=\"color: green;\"> <strong>"
								+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(annotaion.first))
								+ "</strong></span>| <span style=\"color: red;\"> <em> " + "<a target=\"_blank\" href=\"" + url + "\" >"
								+ full_semantic_target_id + "</a> </em></span>| score= <span style=\"color: blue;\">"
								+ results.get(uniqueID).second + "</span>]]";
//								+ options_list.replace("#Annotation_ID#",
//										String.valueOf(annotaion.second.getUniqueID()));

						all_content.add(new Pair<Pair<Integer, Integer>, String>(position, temp));
					} else {
						temp = "[[ <span style=\"color: green;\"> <strong>"
								+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(annotaion.first))
								+ "</strong></span>| <span style=\"color: red;\"> NULL </span>]]";
//								+ options_list.replace("#Annotation_ID#",
//										String.valueOf(annotaion.second.getUniqueID()));
						all_content.add(new Pair<Pair<Integer, Integer>, String>(position, temp));
					}
				}
			} else {
				slogger_.info("No annotations for row :" + i + ",column:" + j);
			}

			annotated_results.append("<td>" + getCellHtmlContent(cell_content, all_content) + "</td>\n");
		}

		annotated_results.append("</tr>\n");

		return annotated_results.toString();
	}

	

	private String getQuantityURL(Candidate first, Pair<String, Annotation> annotaion, int j) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getCellHtmlContent(String cell_content,
			PriorityQueue<Pair<Pair<Integer, Integer>, String>> all_content) {
		Pair<Pair<Integer, Integer>, String> content = all_content.poll();
		int current_indx = 0;
		StringBuilder html_cell = new StringBuilder();
		cell_content = StringEscapeUtils.unescapeJava(cell_content);
		// cell_content = cell_content;
		while (content != null) {
			if (content.first.first > current_indx) {
				html_cell.append(
						StringEscapeUtils.escapeHtml4(cell_content.substring(current_indx, content.first.first)));
			}
			html_cell.append(content.second);
			current_indx = content.first.second + 1;

			content = all_content.poll();
		}
		if (current_indx < cell_content.length()) {
			html_cell
					.append(StringEscapeUtils.escapeHtml4(cell_content.substring(current_indx, cell_content.length())));
		}
		return html_cell.toString();
	}

	private String getAllCandidatesAsComboBox(String uniqueID) {
		Set<Candidate> candidates = (Set<Candidate>) this.candidates.get(uniqueID);

		if (candidates == null)
			return " ";
		StringBuilder cmbx_html = new StringBuilder();
		cmbx_html.append("<span style=\"width: 250px ; border:2px solid;" + " white-space: nowrap;padding:3px;\">"
				+ "<select name=\"cmb_table_#Annotation_ID#\">\n");
		cmbx_html.append("<option value=\"\"></option>");
		for (Candidate cand : candidates) {

			cmbx_html.append("<option value=\""
					+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(cand.getFullSemanticTargetId()))
					+ "\">"
					+ StringEscapeUtils.escapeHtml4(StringEscapeUtils.unescapeJava(cand.getFullSemanticTargetId()))
					+ "</option>\n");
		}
		cmbx_html.append("<option value=\"NULL\">OOKB</option>");
		cmbx_html.append("</select> </span> <br/>");
		return cmbx_html.toString();
	}

	public CharSequence getAnnotatedBodyWithResults() {
		StringBuilder annotated_results = new StringBuilder();
		for (int i = 1; i < nrow; i++) {
			annotated_results.append(getAnnotatedRowWithResults(i));
		}

		return annotated_results.toString();
	}

	public void addCandidateEntities(Map<String, List<Candidate>> entities) {
		if (entities == null || entities.isEmpty()) {
			no_entities = true;
			return;
		}
		Set<Annotation> mention_annotations;
		String id, mention;
		for (Entry<String, List<Candidate>> mention_candidates : entities.entrySet()) {
			mention = mention_candidates.getKey().split("_")[0];
			if(mention.equals("Larry")){
				//TODO REMOVE THIS 
				mention_candidates.getValue().add(new CandidateEntity("YAGO:Larry_Page", TYPE.Entity, 0.0001, "https://en.wikipedia.org/wiki/Larry_Page"));
			}
			mention_annotations = (Set<Annotation>) annotations.get(mention); // get all
															// annotaions for
															// that mention
			if (mention_annotations == null)
				continue;
			for (Annotation annotation : mention_annotations) {
				id = mention + "_" + annotation.getUniqueID();
				if (!candidates.containsKey(id)) {
					candidates.putAll(id, new ArrayList<Candidate>( mention_candidates.getValue()));
					break;
				}
			}			
		}

	}

	public Set<Pair<String, Annotation>> getMentions(int i, int j) {
		if (inverted_annotations != null)
			return (Set<Pair<String, Annotation>>) inverted_annotations.get(new Pair<Integer, Integer>(i, j));
		else
			return null;
	}

	public void removeAnnotationsCascaded(Pair<String, Annotation> mention) {
		String id = mention.first + "_" + mention.second.getUniqueID();
		if (candidates != null)
			candidates.removeAll(id);
		annotations.remove(mention.first, mention.second);
		inverted_annotations.remove(new Pair<Integer, Integer>(mention.second.getRowIndx(), mention.second.getColIndx())
				,mention);
	}

	public void deleteResults() {
		if (results != null)
			results.clear();

	}

	public Set<Pair<String, Annotation>> getAnnotations(int i, int j) {

		return (Set<Pair<String, Annotation>>) inverted_annotations.get(new Pair<Integer, Integer>(i, j));
	}

	public void CopyCandidates(int i, int j, Pair<String, Annotation> annotation, Set<Candidate> cands) {
		candidates.putAll(annotation.first + "_" + annotation.second.getUniqueID(), cands);

	}

	public boolean isInteresting2() {
		// if (nrow > 20 || ncol > 10) {
		// return false;
		// }
		if (this.content_s.split(" ").length < 300)
			return false;
		return true;
	}

	public boolean isInteresting() {
		if (this.content_s.split(" ").length > 300)
			return false;
		if (nrow < 5 || ncol < 3) {
			slogger_.info("table has less than 5 rows or less than 3 columns : " + this.document_id);
			return false;
		}

		int n = 0;
		for (int i = 0; i < ncol; i++) {
			if (isNumericColumn(i))
				n++;
		}
		if (n < 2) {
			slogger_.info("table has less than 2 numerical columns: " + this.document_id);
			return false;
		}

		return true;
	}

	public boolean isStillInteresting() {
		if (missing_annotation) {
			slogger_.info("table has un mapped annotaion: " + this.document_id);
			return false;
		}

		if (no_entities) {
			slogger_.info("table has no entities: " + this.document_id);
			return false;
		}
		if (candidates.size() <= 0)
			return false;
		return true;
	}

	public Map<String, Pair<Candidate, Double>> getResult() {
		return results;
	}

	public void setDocumentID(String document_id) {
		this.document_id = document_id;

	}

	public abstract void writeTableWithAnnotationsToDB();

	public abstract void writeResultstoDB();

	public abstract void writeHTMLResultstoDB(String html_content);

	public void setInternalLinksCount(int value) {
		this.internal_links_count = value;

	}

	public void updateNumericColumns() {

		if (numeric == null) {
			numeric = new boolean[ncol];
			setTrue(numeric);
		}
		if (mixed == null) {
			mixed = new boolean[ncol];
			setFalse(mixed);
		}

		int count_numeric = 0;
		int count_non_numeric = 0;
		for (int j = 0; j < ncol; j++) {
			count_numeric = 0;
			count_non_numeric = 0;
			for (int i = 1; i < nrow; i++) {
				if (columns_a.get(i)[j] == null || columns_a.get(i)[j].trim().isEmpty()) {
					continue;
				}
				if (!containsNumeric(columns_a.get(i)[j])) {
					count_non_numeric++;
				} else {
					count_numeric++;
				}
			}
			if (count_non_numeric > 0) {

				if (((double) count_numeric / (double) count_non_numeric) > 2.0) {
					numeric[j] = true;
					mixed[j] = true;
				} else
					numeric[j] = false;
			} else if (count_numeric > 0) {
				numeric[j] = true;
			} else
				numeric[j] = false;
		}

	}

	public int getMentionsCount() {
		if (annotations != null) {
			return annotations.size();
		} else {
			return 0;
		}

	}

	public String[][] getCellsAsArray() {

		return (String[][]) columns_a.toArray(new String[nrow][]);
	}

	public abstract List<Integer> calculatePrecision();

	public int getTotalAnnotationsCount() {
		
		return annotations.size();
	}

	public String[] getHeader() {

		return columns_a.get(0);
	}

	public boolean isColumnHasEntityAnnotations(int j) { // only use with Sunita
															// an DOug datasets
															// with only entity
															// annotations
		Pair<Integer, Integer> temp = new Pair<Integer, Integer>();
		temp.second = j;
		for (int i = 1; i < nrow; i++) {
			temp.first = i;
			if (inverted_annotations.get(temp) != null) {
				return true;
			}
		}
		return false;
	}

	public void writeAnnotationsToPOJO(Document doc) {
		if(getResult() == null)
			return;	
		String mention;
		Annotation annotation;
		Pair<Candidate,Double> result;
		for(Entry<String,Annotation> annotation_entry : this.annotations.entries()){
			mention = annotation_entry.getKey();
			annotation = annotation_entry.getValue();
			if(this.results.containsKey(mention+"_"+annotation.getUniqueID())){
				result = results.get(mention+"_"+annotation.getUniqueID());
				doc.addTableAnnotation(annotation.getRowIndx(), annotation.getColIndx(), 
						annotation.getStartOffset(), annotation.getEndOffset(),
						annotation.getAnnotation(), mention,
						StringEscapeUtils.unescapeJava(result.first.getFullSemanticTargetId()).trim(), result.second,
						result.first.getUrl());
			}else{
				doc.addTableAnnotation(annotation.getRowIndx(), annotation.getColIndx(), 
						annotation.getStartOffset(), annotation.getEndOffset(),
						annotation.getAnnotation(), mention,
						"NULL", 0.0,"");
			}
			
		}
	}

}
