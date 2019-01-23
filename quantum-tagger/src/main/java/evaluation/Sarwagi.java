package evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import data.Document_;
import data.EvaluationDocument;
import loader.DocumentLoader;
import utils.FileUtils;

public class Sarwagi implements Iterator<Document_> {

	/**
	 * This class to evaluate Sunita Sarwagi webtables, WikiLinks and Wikitables
	 * We will evaluate both the entities (and maybe classes)
	 */
	Pattern pattern1 = Pattern.compile("\\/c(\\d+)\\/r(\\d+)\\/.*");
	Pattern pattern2 = Pattern.compile("_rows(\\d+).xml");
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	HashMap<String, Document_> documents;
	String tables_path, gs_path, tables_list;
	List<String> tables;
	int current_index = 0;
	int total_annotations=0;
	public int getTotal_annotations() {
		return total_annotations;
	}

	boolean isHeader = false;
	public Sarwagi(String tables_path, String gs_path, String tables_list, boolean isHeader) {
		this.tables_list = tables_list;
		this.gs_path = gs_path;
		this.tables_path = tables_path;
		this.isHeader = isHeader;
		this.total_annotations =0;
		lazyLoadTables(tables_list);
	}

	public List<String> lazyLoadTables(String file_name) {
		documents = new HashMap<String, Document_>();
		List<String> files_ls = null;
		try {
			files_ls = FileUtils.loadFileToList(file_name, "", 0);
		} catch (IOException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}
		tables = files_ls;
		return files_ls;
	}

	public HashMap<String, Document_> loadTables(String file_name) {
		documents = new HashMap<String, Document_>();
		List<String> files_ls = null;
		try {
			files_ls = FileUtils.loadFileToList(file_name, "", 0);
			for (String file : files_ls) {
				slogger_.info("Loading file :" + file);
				Document_ document = load_xmlTable(file);

				document.setFile_name(file);
				document.setId(file);
				if (document.getTables() == null || document.getTables().size() <= 0) {
					continue;
				}

				if (documents.containsKey(document.getId())) {// collisions
					documents.get(document.getId()).merge(document);
				} else
					documents.put(document.getId(), document);
			}
		} catch (IOException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}
		return documents;
	}

	private Document_ load_xmlTable(String file) {
		Document_ table_doc = null;
		int ncol = 0;
		int nrow = 0;
		slogger_.info("processing file: " + file);
		slogger_.info("processing file: " + file);
		try {
			Matcher matcher = null;
			if (file.indexOf("/c") >= 0) {
				matcher = pattern1.matcher(file.substring(file.indexOf("/c")));
				if (matcher.matches()) {
					ncol = Integer.parseInt(matcher.group(1));
					nrow = Integer.parseInt(matcher.group(2));
					slogger_.info(String.format("nrows %d, no columns %d", nrow, ncol));
				}
			} else if (file.lastIndexOf("_row") > 0) {
				matcher = pattern2.matcher(file.substring(file.lastIndexOf("_row")));
				if (matcher.matches()) {
					nrow = Integer.parseInt(matcher.group(1));
					slogger_.info(String.format("nrows %d, ncolumns %d", nrow, 0));
				}
			} else {
				slogger_.error("Could not get nrows of:" + file);
				slogger_.info("Could not get nrows of " + file);
				return null;
			}
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			DefaultHandler handler = new SAXTableHandler();
			((SAXTableHandler) handler).init(nrow, ncol, file);
			File file_obj = new File(file);
			InputStream inputStream = new FileInputStream(file_obj);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			saxParser.parse(is, handler);
			table_doc = new EvaluationDocument();
			table_doc.addTable(((SAXTableHandler) handler).getTable());
		} catch (ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return table_doc;
	}

	@Override
	public boolean hasNext() {
		if (current_index < tables.size())//
			return true;
		else
			return false;
	}

	@Override
	public Document_ next() {
		Document_ document;
		String table_file = tables_path + tables.get(current_index);
		String table_gs = gs_path + tables.get(current_index);
		try{
			document = loadDocument(table_file, table_gs);
		}catch(Exception e){
			document = null;
			e.printStackTrace();
		}
		current_index++;
		if(document!=null)
			total_annotations+= document.getTable(0).getTotalAnnotationsCount();
		return document;
	}

	private Document_ loadDocument(String table_file, String table_gs) {
		slogger_.info("Loading file :" + table_file);
		Document_ document = load_xmlTable(table_file);
		annotate(document, table_gs);
		document.setFile_name(table_file);
		document.setId(table_file);
		if (document.getTables() == null || document.getTables().size() <= 0) {
			document = null;
		}
		return document;
	}

	private void annotate(Document_ document, String table_gs) {
		try {
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			DefaultHandler handler = new SAXAnnotationHandler(document.getTable(0),isHeader);
			File file_obj = new File(table_gs);
			InputStream inputStream = new FileInputStream(file_obj);
			Reader reader = new InputStreamReader(inputStream, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			saxParser.parse(is, handler);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

}
