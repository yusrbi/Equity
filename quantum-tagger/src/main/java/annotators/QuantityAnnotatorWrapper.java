package annotators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import knowledgebase.Units_Measures;
import annotations.Annotation;
import annotations.Annotation.ANNOTATION;
import data.Document_;
import data.Table_;
import resources.Resources;

public class QuantityAnnotatorWrapper implements AnnotatorWrapper {

	Resources resources;
	private final Pattern numeric_pattern = Units_Measures.numeric_pattern;// Pattern.compile("\\s*([^0-9\\s]*)\\s*[-+]?(\\d[\\d,]*\\.\\d+|\\d*\\.\\d+|\\d[\\d,]+|[\\d]+)\\s*([^0-9\\s]*((\\/|\\sper\\s)\\s?\\d*\\s?[^0-9\\s]*)?)",
																			// Pattern.CASE_INSENSITIVE
																			// |
																			// Pattern.DOTALL
																			// |
																			// Pattern.MULTILINE);
	private final Pattern numeric_pattern_repeated = Units_Measures.numeric_pattern_repeated;
	Pattern year = Pattern.compile("^\\d{4}$");

	public QuantityAnnotatorWrapper() {
		resources = Resources.getResources();
	}

	public void process(Document_ document) {
		boolean flag = true, found_matches = false;
		
		Matcher matcher = null;
		if(document.getContent() != null)
			process_context(document);
		for (Table_ table : document.getTables()) {
			String[][] cells = table.getCellsAsArray();
			boolean isNoUnitColumn = false;
			for (int j = 0; j < table.getNcol(); j++) {
				if (table.isNumericColumn(j)) {
					// annotate all the cells as quantities
					flag = false;
					if (cells[0][j].toLowerCase().equals("notes") || cells[0][j].toLowerCase().equals("details")
							|| cells[0][j].toLowerCase().equals("references")
							|| cells[0][j].toLowerCase().equals("ref"))
						continue;
					isNoUnitColumn = checkIsNoUnitColumn(cells[0][j].trim());						
					
					for (int i = 1; i < table.getNrow(); i++) {
						if (cells[i][j] != null && !cells[i][j].isEmpty()) {
							if(isNoUnitColumn){
								//just add the whole cell content in case of dates and dimensionless classes 
								table.addAnnotations(cells[i][j].trim(), i, j, 0, cells[i][j].trim().length(),
										Annotation.ANNOTATION.OTHER_QUANTITY);
								continue; // a shortcut do not go further 
							}
							found_matches = false;
							matcher = this.numeric_pattern.matcher(cells[i][j].trim());
							if (matcher.matches()) {
								// found a match no need to break it down
								found_matches = true;
								table.addAnnotations(cells[i][j].trim(), i, j, 0, cells[i][j].trim().length(),
										Annotation.ANNOTATION.OTHER_QUANTITY);
							} else {
								matcher = this.numeric_pattern_repeated.matcher(cells[i][j].trim());
								if (matcher.matches()) {
									// found a match no need to break it down
									found_matches = true;
									table.addAnnotations(cells[i][j].trim(), i, j, 0, cells[i][j].trim().length(),
											Annotation.ANNOTATION.OTHER_QUANTITY);
								}
							}
//							if (!found_matches) {									
//								List<String> np_l = stanford.getNounPhrases(cells[i][j]);
//								if (np_l != null && np_l.size() > 0) {
//									// use the noun phrases to find numerical
//									// quantities
//									// full match of the string
//									for (String np : np_l) {
//
//										matcher = this.numeric_pattern.matcher(np.trim());
//										if (matcher.matches()) {
//											found_matches = true;
//											start = cells[i][j].indexOf(np);
//											if (start >= 0)
//												table.addAnnotations(np, i, j, start, start + np.length(),
//														Annotation.ANNOTATION.OTHER_QUANTITY);
//											else {
//												indx = find_space_variant_position(np, cells[i][j]);
//												if (indx != null) {
//													table.addAnnotations(cells[i][j].substring(indx[0], indx[1]), i, j,
//															indx[0], indx[1], Annotation.ANNOTATION.OTHER_QUANTITY);
//												} else {
//													System.out
//															.println("Could not find: " + np + "in cell" + i + "," + j);
//												}
//											}
//										}
//									}
//								}
//							}
							if (!found_matches) {
								// use only pattern matching on the whole cell
								// content
								matcher = this.numeric_pattern_repeated.matcher(cells[i][j]);
								if (matcher.find()) {
									table.addAnnotations(matcher.group(0), i, j, matcher.start(), matcher.end(),
											Annotation.ANNOTATION.OTHER_QUANTITY);
									while (matcher.find()) {// add more if any
															// was found
										table.addAnnotations(matcher.group(0), i, j, matcher.start(), matcher.end(),
												Annotation.ANNOTATION.OTHER_QUANTITY);
									}
								} else {
									table.addAnnotations(cells[i][j], i, j, 0, cells[i][j].length() - 1,
											Annotation.ANNOTATION.OTHER_QUANTITY);
									//here do something if it is not a quantity at all
								}
							}
							flag = true;
						}
					}
					if (flag) {// annotate the header as concept, if there was
								// non-empty cells
						/*
						 * matcher = year.matcher(cells[0][j].trim());
						 * if(matcher.matches()){
						 * table.addAnnotations(cells[0][j], 0, j, 0,
						 * cells[0][j].length() - 1,
						 * Annotation.ANNOTATION.DATE); }else{
						 */
						table.addAnnotations(cells[0][j], 0, j, 0, cells[0][j].length() - 1,
								Annotation.ANNOTATION.CONCEPT);
						// }

					}
				} else {
					// annotate header as class
					// String[][] cells = table.getCells();
					// annotate the header as concept
					table.addAnnotations(cells[0][j], 0, j, 0, cells[0][j].length() - 1, Annotation.ANNOTATION.CLASS);
				}
			}
		}
	}
	
	private boolean checkIsNoUnitColumn(String mention) {
		boolean noUnit = true;
		Set<String> classes =  Units_Measures.getClassesNames(mention);
		if(classes != null && classes.size() >0){
			for(String class_name : classes){
				if(!isNoUnit(class_name)){
					noUnit = false;
					break;
				}
			}
		}else{
			noUnit = false;
		}
			
		
		return noUnit;
	}

	private boolean isNoUnit(String class_name) {
		
		return Units_Measures.getUnits_Measures().isNoUnit(class_name);
	}

	private void process_context(Document_ document) {
		Pattern quantity = Units_Measures.numeric_pattern_repeated;
		Matcher matcher = quantity.matcher(document.getContent());
		int start=-1, end=-1;
		String mention ;
		while(matcher.find()){
			start = matcher.start();
			end = matcher.end();
			mention =document.getContent().substring(start, end);
			if(mention.startsWith(" ")){
				start++;
			}
			if (mention.endsWith(" ")){
				end--;
			}			
			document.addAnnotations(mention.trim(), -1, -1, start, end
						, ANNOTATION.OTHER_QUANTITY);
			
		}		
	}
	

	

}
