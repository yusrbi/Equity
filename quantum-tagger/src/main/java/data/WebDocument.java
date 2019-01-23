package data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Annotation;
import edu.stanford.nlp.util.Pair;
import knowledgebase.Candidate;
import loader.DocumentLoader;
import resources.Resources;
import utils.DatabaseAccess;
import utils.FileUtils;

public class WebDocument extends Document_ implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6650976084268633578L;
	private String url_s;
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	int count_i = 0;

	public WebDocument(String url, String title, String content, String strong_content) {
		super(title, content.replace('"', '\''));
		this.setId(url.trim());
		this.url_s = url.trim();
	}

	public WebDocument(String url, JsonObject json_table, int id) {
		super();
		this.url_s = url.trim();
		addJsonTable(json_table, id);
		super.getSlogger().info("Creating document: " + url);
	}

	@Override
	public void addJsonTable(JsonObject json_table, String table_title, int id) {
		count_i++;
		super.getSlogger().info("Adding table to the web document, table: " + Integer.toString(count_i));
		Set<Entry<String, JsonValue>> entries = json_table.entrySet();
		Entry<String, JsonValue> entry = entries.iterator().next();
		
		String table_content_s = entry.getValue().toString();
		String table_caption = entry.getKey();
		if (table_content_s.startsWith("\"")) {
			table_content_s = table_content_s.substring(1);
		}
		if (table_content_s.endsWith("\"")) {
			table_content_s = table_content_s.substring(0, table_content_s.length() - 1);
		}
		Table_ table = new WebTable(table_title, table_caption, table_content_s);
		if (table.getNrow() < 2) {
			return;// do not add tables with less than two rows
		}
		if (super.getTables() == null)
			super.setTables(new ArrayList<Table_>());

		super.getTables().add(table);
		table.setId(id);
		table.setDocumentID(super.getId());
		
		return;

	}

	@Override
	public void addJsonTable(JsonObject json_table, int id) {

		addJsonTable(json_table, "", id);
	}

	public void writeDocumentWithAnnotationsToDB() {
		try {
			DatabaseAccess db_access = DatabaseAccess.getDatabaseAccess();
			db_access.insertDocumentData(super.getId(), super.getTitle(),
					this.url_s, super.getContent() );
			writeAnnotations();

		} catch (SQLException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void writeResultstoDB() {
		if(super.getResults() == null)
			return;
		StringBuilder results_sb = new StringBuilder();
		int count = 0;
		int annotation_id = 0;
		try {
			DatabaseAccess db_access = DatabaseAccess.getDatabaseAccess();

			for (Entry<String, Pair<Candidate, Double>> result : super.getResults().entrySet()) {
				annotation_id = Integer.parseInt(result.getKey().split("_")[1]);
				results_sb.append("\"" + super.getId() + "\"," + annotation_id + ",\"" + super.getExperiement_id()
						+ "\", \"" + StringEscapeUtils.unescapeJava(result.getValue().first.getFullSemanticTargetId()).trim()//.replace("'", "''")
						+ "\"," + result.getValue().second
						+ "\n");
				count++;
			}
			db_access.copyToDB("evaluation.document_results", results_sb, count);
		} catch (Exception e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}

	}

	private void writeAnnotations() {
		StringBuilder annotations_sb = new StringBuilder();
		int count = 0;
		try {
			DatabaseAccess db_access = DatabaseAccess.getDatabaseAccess();
			if(super.getAnnotations() == null)
				return;
			for (Annotation annotation : super.getAnnotations().values()) {
				
					annotations_sb.append(
							annotation.getUniqueID() + ",\"" + super.getId().trim() + "\"," + annotation.getStartOffset() + ","
									+ annotation.getEndOffset() + ",\"" + annotation.getAnnotation() + "\"\n");
					count++;
				
			}
			db_access.copyToDB("evaluation.document_annotations", annotations_sb, count);
		} catch (Exception e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}

	}
	public void writeHTMLResultstoDB(String html_content){
		try {
			DatabaseAccess db_access = DatabaseAccess.getDatabaseAccess();
			db_access.insertDocumentHTMLResults(super.getId(), super.getExperiement_id() ,
					html_content );//.replace("'", "''")
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean isWikipediaPage() {

		return super.Id.contains("wikipedia.org/") || this.url_s.contains("wikipedia.org/");
	}
	public void saveGraph(String prefix, boolean write_potential_edges) throws IOException {
		String data = graph.getPythonEdgeList(write_potential_edges);
		String annotations = annotated_ner + table_l.get(0).getAnnotatedContents();
		File file = new File(Resources.getResources().getgraph_save_dir()
				+ this.file_name.substring(0, this.file_name.indexOf("."))+prefix + ".txt");
		FileUtils.writeFileContent(file, annotations + "\n" + data);
	}

}
