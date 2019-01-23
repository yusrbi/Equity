package evaluation;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.*;

import data.EvaluationTable;
import data.Table_;

public class SAXTableHandler extends DefaultHandler2 {
	Table_ table;
	boolean b_cell, b_html, b_cdata;//, b_text;
	List<String> row;
	int ncol = 0, nrows = 0, indx_row = 0, indx_col = 0;
	String data = "";

	public void init(int nrows, int ncol, String file_name) {
		this.nrows = nrows;
		this.ncol = ncol;
		table = new EvaluationTable(file_name, "");
		table.setNrow(nrows);
		if (ncol != 0)
			table.setNcol(ncol);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("cell")) {
			b_cell = true;
		} else if (qName.equalsIgnoreCase("row")) {
			// create a new row
			// .out.println("ROW-START");
			row = new LinkedList<String>();
			indx_col = 0;
		} else if (qName.equalsIgnoreCase("header")) {
			// create a new row
			// slogger_.info("header-START");
			row = new LinkedList<String>();
			indx_col = 0;
		} else if (qName.equalsIgnoreCase("html")) {
			b_html = true;
		}/*else if (qName.equalsIgnoreCase("text")) {
			b_text = true;
		}*/ else if (qName.equalsIgnoreCase("coltype")) {
			ncol++;
		}
	}

	// public void startCDATA() {
	// b_cdata = true;
	// }
	//
	// public void endCDATA() {
	// b_cdata = false;
	// }

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( qName.equalsIgnoreCase("html") ) {//qName.equalsIgnoreCase("text") ||
			if (data != null) {
				if (data.startsWith("<"))
					data = data.substring(4);
				if (data.endsWith(">"))
					data = data.substring(0, data.length() - 5);
				if(row.size() == indx_col+1)
					row.set(indx_col,data);
				else
					row.add(indx_col, data);
				data = null;
				b_html = false;
				//b_text = false;
			}
			
			// slogger_.info("Cell: " + row[indx_col]);
		}
		if (qName.equalsIgnoreCase("cell")) {
			b_cell = false;
			indx_col++;
		}
		if (qName.equalsIgnoreCase("row") || qName.equalsIgnoreCase("header")) {
			// write the old row;
			if (table.getNcol() == 0){
				ncol = row.size();
				table.setNcol(ncol);
			}
			table.setColumns((String[]) row.toArray(new String[row.size()]), indx_row);
			indx_row++;
			// slogger_.info("ROW-END");
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {

		if (b_cell && b_html && ch != null ) {
			if (data == null || data.isEmpty()) {
				data = (new String(ch, start, length)).trim();
			} else {
				data += (new String(ch, start, length)).trim();
			}

		}
//		else if (b_cell && b_text && ch != null ) {
//			if (data == null || data.isEmpty()) {
//				data = (new String(ch, start, length)).trim();
//			} else {
//				data += (new String(ch, start, length)).trim();
//			}
//
//		}

	}

	public Table_ getTable() {
		return this.table;
	}

}
