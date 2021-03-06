package loader;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;
import java.io.File;
import java.io.FileInputStream;

import java.io.InputStreamReader;
import java.util.HashMap;

import data.Document_;
import data.Table_;
import data.WebDocument;
import data.WebTable;
import resources.Resources;
import webservice.model.Document;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentLoader {
	private static DocumentLoader doc_loader = null;
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	private boolean context_switch = false;

	public Document_ load(String path, String fileName) {
		JsonObject json_obj = null;
		try {
			FileInputStream input_stream = new FileInputStream(path+fileName);
			JsonReader json_reader = Json.createReader(new InputStreamReader(input_stream, "utf-8"));
			json_obj = json_reader.readObject();

		} catch (Exception e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}

		if (json_obj != null)
			return createDocumentFromJsonObject(json_obj, fileName);
		else
			return null;
	}

	public static DocumentLoader getLoader(Resources resources) {
		if (doc_loader == null)
			doc_loader = new DocumentLoader();
		doc_loader.set_context_switch(resources.isContext_switch());
		return doc_loader;
	}

	private void set_context_switch(boolean context_switch) {
		this.context_switch  = context_switch;
		
	}

	private Document_ createDocumentFromJsonObject(JsonObject json_obj, String file_name) {

		String title_s = "", table_title_s = "", content_s = "", url_s, strong_content_s = "", page_short_content = "";
		int table_id = 0;
		if (json_obj.containsKey("multiple_headers")) {
			if (json_obj.getBoolean("multiple_headers")) {
				return null;// do not process tables with multiple headers
			}
		}
//		if (json_obj.containsKey("nrow")) {
//			if (json_obj.getInt("nrow") < 4) {
//				return null;
//			}
//		}
		url_s = json_obj.getString("url");
		if (json_obj.containsKey("page_title")) {
			title_s = StringEscapeUtils.unescapeJson(json_obj.getString("page_title"));
			title_s = StringEscapeUtils.unescapeXml(title_s);
		}
		if (json_obj.containsKey("title")) {
			table_title_s = StringEscapeUtils.unescapeJson(json_obj.getString("title"));
			table_title_s = StringEscapeUtils.unescapeXml(table_title_s);
		}
		if (json_obj.containsKey("page_content") && context_switch) {
			content_s = StringEscapeUtils.unescapeJson(json_obj.getString("page_content"));
			content_s = StringEscapeUtils.unescapeXml(content_s);
		}
		if (json_obj.containsKey("page_strong_content")&& context_switch) {
			strong_content_s = StringEscapeUtils.unescapeJson(json_obj.getString("page_strong_content"));
			strong_content_s = StringEscapeUtils.unescapeXml(strong_content_s);
		}
		if (json_obj.containsKey("page_short_content")&& context_switch) {
			page_short_content = StringEscapeUtils.unescapeJson(json_obj.getString("page_short_content"));
			page_short_content = StringEscapeUtils.unescapeXml(page_short_content);
		}
		
		JsonObject json_table = json_obj.getJsonObject("content");
		Document_ new_doc = new WebDocument(url_s, title_s, content_s, strong_content_s);
		new_doc.setId(file_name);
		if (content_s.split(" ").length > 200 && page_short_content.length() > 0) {
			int start = content_s.indexOf(page_short_content);
			int end = content_s.indexOf("\n==", start + page_short_content.length());
			if (start > 0 && end > 0) {
				String temp = content_s.substring(start, end);
				new_doc.setShortContent(temp);
			} else if (start > 0) {
				String temp = content_s.substring(start, content_s.length() - 1);
				new_doc.setShortContent(temp);
			}

		}
		String[] new_words = new_doc.getContent().split(" ");
		if (new_words.length > 200) {
			StringBuilder new_context = new StringBuilder();
			for (int i = 0; i < 200; i++) {
				new_context.append(" " + new_words[i]);
			}
			new_doc.setShortContent(new_context.toString());
		}
		
//		if (!wiki_id.isEmpty()) {
//			new_doc.setId(wiki_id);
//		}
		if (json_obj.containsKey("table_id")) {
			table_id = Integer.valueOf(String.valueOf(json_obj.getString("table_id")).split("-")[1]);

		}
		new_doc.addJsonTable(json_table, table_title_s, table_id);
		if (json_obj.containsKey("internal_links")) {
			List<Table_> tables = new_doc.getTables();
			if (tables != null && tables.size() >= 1) {
				tables.get(0).setInternalLinksCount(json_obj.getInt("internal_links"));
			}
		}
		return new_doc;
	}

	/**
	 * @param optionValue
	 * @param documents
	 *            the hashmap at which the documents will be added to
	 * @return the loaded documets in a hashmap This method will laod all the
	 *         documents in a given directory
	 */
	public void load(String directory, HashMap<String, Document_> documents) {

		File dir = new File(directory);

		for (String file : dir.list()) {

			// if(file.equals("finance_2011.json")){
			// int x=0;
			// }
			slogger_.info("Loading file :" + file);
			Document_ document = load(dir.getPath() + File.separator , file);
			// if(!document.getId().contains("wikipedia"))
			// continue;

			document.setFile_name(file);
			if (document.getTables() == null || document.getTables().size() <= 0) {
				return;
			}

			if (documents.containsKey(document.getId())) {// collisions
				documents.get(document.getId()).merge(document);
			} else
				documents.put(document.getId(), document);
		}

	}

	public Document_ load(Document doc_pojo) {
	
		Document_ document ;
		
		document = new WebDocument("", doc_pojo.getTitle(), doc_pojo.getContext(),"");
		if (doc_pojo.getContext().split(" ").length > 200 ) {
			int i = doc_pojo.getContext().indexOf(" ", 200);
			document.setShortContent(doc_pojo.getContext().substring(0,i));
		}
		Table_ table = new WebTable(doc_pojo.getTable_title(), "", doc_pojo.getTable_content());
		document.addTable(table);		
		return document ;
	}
}
