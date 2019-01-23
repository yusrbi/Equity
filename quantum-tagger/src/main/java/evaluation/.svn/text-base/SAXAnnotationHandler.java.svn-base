package evaluation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import annotations.Annotation;
import annotations.Annotation.ANNOTATION;
import annotations.Entity;
import data.Table_;

public class SAXAnnotationHandler extends DefaultHandler {
	int row_index = -1;
	int col_index = -1;
	Table_ table;
	boolean b_entity = false;
	String KBID = "";
	String surface = "";

	boolean isHeader = false;
	public SAXAnnotationHandler(Table_ table, boolean isHeader) {
		this.table = table;
		this.isHeader = isHeader;
		row_index = isHeader?0:-1;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

		if (qName.equalsIgnoreCase("row")) {
			row_index++;
			col_index = -1;
		} else if (qName.equalsIgnoreCase("entity")) {
			b_entity = true;
			col_index++;
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("entity")) {
			if (KBID != null && !KBID.isEmpty() && !KBID.equals("NULL") ) {
				surface = table.getCell(row_index, col_index);
//				if(surface==null)
//					surface="";
				Annotation annotation = new Entity(row_index, col_index, 0, surface.length() - 1,
						ANNOTATION.ENTITY);
				annotation.setGold_standard(1, KBID);
				table.addAnnotation(surface,row_index, col_index, annotation);
				KBID = null;
				surface= null;
			}
			b_entity = false;
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {

		if (b_entity && ch != null && col_index < table.getNcol()) {
			if (KBID == null || KBID.isEmpty()) {
				KBID = (new String(ch, start, length)).trim();
			} else
				KBID += (new String(ch, start, length)).trim();

		}

	}

}
