package data;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;

import annotations.Annotation;
import edu.stanford.nlp.util.Pair;
import knowledgebase.Candidate;
import utils.DatabaseAccess;

public class WebTable extends Table_ {

	// private static int count=0;
	String table_caption;

	public WebTable(String title, String caption, String content) {
		super(title, content);
		this.table_caption = caption;
		String mod_content = content;
//		if (mod_content.startsWith("\"")) {
//			mod_content = mod_content.substring(1);
//		}
//		if (mod_content.endsWith("\"")) {
//			mod_content = mod_content.substring(0, mod_content.length() - 1);
//		}

		mod_content = StringEscapeUtils.unescapeJson(mod_content);
		mod_content = StringEscapeUtils.unescapeXml(mod_content);
		// mod_content = mod_content.replace("\"\"", "\"");
		String[] rows = mod_content.split("\\r\\n"); // \\r --> as it is in a
														// string inside the
														// json object, so it is
														// written with 2 \
														// instead of one as
														// usual
		if (rows.length == 1) {
			rows = mod_content.split("\\n"); // a single \ is written as \\
		}

		// ArrayList<String> rows = new ArrayList<String>();
		// try {
		// BufferedReader reader = new BufferedReader(
		// new StringReader(content));
		// String line_s;
		// while ((line_s = reader.readLine()) != null) {
		// rows.add(line_s);
		// }
		// } catch (Exception e) {
		//
		// }

		// CSVParser csv_parser = null;
		// List<CSVRecord> rows = null;
		// try {
		// csv_parser = new CSVParser(reader, CSVFormat.DEFAULT);
		// rows = csv_parser.getRecords();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		if (rows != null) {
			super.setRows(rows);
			super.setNrow(rows.length);
			int nCol = countColumns(rows[0]);// .split(",(?!\\A)").length;//did
												// not work as th Java 8
												// implementation ignores empty
												// strings
			super.setNcol(nCol);
		}

		String[] columns = null;

		int row_indx = 0;
		for (String row : rows) {
			columns = null;
			// split the row on , and take care of the strings
			if (row.isEmpty())
				continue;
			// if(row.indexOf('"') >0){
			columns = get_columns_string_delm(row);
			// }else{
			// columns = get_columns_comma_delm(row);
			// if(columns.length > super.getNcol() ){
			// //count++;
			// continue;
			// }
			// }
			if (columns != null && columns.length == super.getNcol() && !isEmpty(columns))// only
																							// add
																							// columns
																							// that
																							// have
																							// same
																							// length
				super.setColumns(columns, row_indx++);
		}
		if (row_indx != super.getNrow())
			super.setNrow(row_indx);// set the effective number of rows
		// System.out.println(count);
		/*
		 * Did not work as of special char Reader in = new
		 * StringReader(content); try { CSVParser csv_reader = new CSVParser(in,
		 * CSVFormat.DEFAULT); List<CSVRecord> records =
		 * csv_reader.getRecords(); for(CSVRecord record : records){
		 * record.get(0); } } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */
		super.updateNumericColumns();
	}

	private int countColumns(String row) {
		int count = 1;
		boolean string_bgn = false;
		boolean char_before = false;
		for (char c : row.toCharArray()) {
			switch (c) {
			case ',':
				if (string_bgn) {
					continue;
				} else {
					count++;
					char_before = false;
				}
				break;
			case '"':
				if (!string_bgn && char_before) {
					break;// ignore when it starts in the middle of the column
				}
				if (string_bgn) {
					// end of string
					string_bgn = false;
				} else {// Beginning of a string
					string_bgn = true;
				}
				break;
			default:
				char_before = true;
				break;
			}
		} // end inner loop for single row
		return count;
	}

	private boolean isEmpty(String[] columns) {
		boolean empty = true;
		for (String column : columns) {
			if (column != null) {
				empty = false;
				break;
			}
		}
		return empty;
	}

	@SuppressWarnings("unused")
	private String[] get_columns_comma_delm(String row) {
		String[] columns = row.split(",");
		return columns;
	}

	private String[] get_columns_string_delm(String row) {
		StringBuilder column = new StringBuilder();
		String[] columns = new String[super.getNcol()];
		int col_indx = 0;
		boolean string_bgn = false;
		for (char c : row.trim().toCharArray()) {
			if (col_indx >= super.getNcol())
				return columns;
			switch (c) {
			case ',':
				if (string_bgn) {
					column.append(c);// add the comma to the string
					continue;
				} else {
					columns[col_indx++] = column.toString();
					column = new StringBuilder();
				}
				break;
			case '"':
				if (!string_bgn && column.length() > 0) {
					column.append(c); // if it starts in the middle of the data
										// then ignore
					break;
				}
				if (string_bgn) {
					// end of string
					string_bgn = false;
				} else {// Beginning of a string
					string_bgn = true;
				}
				break;
			case '\\':// skip all the escape sequence char
				break;
			default:
				column.append(c);
			}
		} // end inner loop for single row
			// add the last column
		if (col_indx < super.getNcol())
			columns[col_indx++] = column.toString();
		return columns;
	}

	public void writeTableWithAnnotationsToDB() {
		try {
			DatabaseAccess db_access = DatabaseAccess.getDatabaseAccess();
			db_access.insertTableData(id, document_id, super.getNrow(), super.getNcol(), getContentForDB());
			writeAnnotations();

		} catch (Exception e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}

		// pages.append(page_id).append(",\"")
		// .append(page_title.replace("'", "''").replace(" ",
		// "_")).append("\"\n");
	}

	private String getContentForDB() {
		StringBuilder table_content = new StringBuilder();
		for (int i = 0; i < super.getNrow(); i++) {
			for (int j = 0; j < super.getNcol(); j++) {
				table_content.append(super.getCell(i, j) + "\t"); // .replace("'",
																	// "''")
			}
			table_content.append("\n");
		}
		return table_content.toString();
	}

	public void writeResultstoDB() {
		StringBuilder results_sb = new StringBuilder();
		int count = 0;
		int annotation_id = 0;
		try {
			DatabaseAccess db_access = DatabaseAccess.getDatabaseAccess();
			if (super.getResult() == null)
				return;
			for (Entry<String, Pair<Candidate, Double>> result : super.getResult().entrySet()) {
				annotation_id = Integer.parseInt(result.getKey().split("_")[1]);
				results_sb.append(super.id + ",\"" + super.document_id + "\"," + annotation_id + ",\""
						+ super.experiement_id + "\", \""
						+ StringEscapeUtils.unescapeJava(result.getValue().first.getFullSemanticTargetId()).trim()// .replace("'",
																													// "''")
						+ "\"," + result.getValue().second + "\n");
				count++;
			}
			db_access.copyToDB("evaluation.table_results", results_sb, count);
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
			if (super.annotations == null)
				return;

			for (Annotation annotation : super.annotations.values()) {
				annotations_sb.append(annotation.getUniqueID() + "," + super.id + ",\"" + this.document_id.trim()
						+ "\"," + annotation.getRowIndx() + "," + annotation.getColIndx() + ","
						+ annotation.getStartOffset() + "," + annotation.getEndOffset() + ",\""
						+ annotation.getAnnotation() + "\"\n");
				count++;
			}

			db_access.copyToDB("evaluation.table_annotations", annotations_sb, count);
		} catch (Exception e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void writeHTMLResultstoDB(String html_content) {
		try {
			DatabaseAccess db_access = DatabaseAccess.getDatabaseAccess();
			db_access.insertTableHTMLResults(super.id, super.document_id, super.experiement_id, html_content);// .replace("'",
																												// "''")

		} catch (Exception e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public List<Integer> calculatePrecision() {
		return null;

	}
}
