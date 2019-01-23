package annotators;


import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

import annotations.Annotation;
import annotations.Annotation.ANNOTATION;
import data.Document_;
import data.Table_;
import edu.stanford.nlp.util.Pair;
import executer.RunTimeAnalysis;
import knowledgebase.Units_Measures;
import loader.DocumentLoader;
import utils.DatabaseAccess;

public class AnnotationEnrichment{

	private static Set<String> stopwords;
	private static Set<Character> delimeters;
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	static public final String WITH_DELIMITER = "(?<=%1$s)";
	public static final String SPLIT_WITH = String.format(WITH_DELIMITER, "[\\s,;.\\/\\?\\-\\]\\[\\{\\}\\(\\)%$&@!]");
	// (?=;)lookahead//(?<=;)lookbehind//((?<=;)|(?=;)) equals to select an empty character before ; or after ;.
	 private static Set<String> notBreakable;
	public static void setStopwords(Set<String> stopwords) {
		AnnotationEnrichment.stopwords = stopwords;
	}

	public AnnotationEnrichment() {
		if (notBreakable == null) {
			notBreakable = new LinkedHashSet<String>();
			notBreakable.add("Name");
			notBreakable.add("title");
			notBreakable.add("song title");
			notBreakable.add("address");
			notBreakable.add("e-mail");
			notBreakable.add("round");
			notBreakable.add("mailing & website");
			notBreakable.add("website");
			notBreakable.add("mailing address & website");
			notBreakable.add("mailing address");
			notBreakable.add("round");
			notBreakable.add("Album title");
			notBreakable.add("book title");
		}
		if(delimeters == null){
			delimeters = new LinkedHashSet<Character>();
			for(char c: new char[]{ ' ', '\t', '\n', ',', ';', '.', '/', '?', '-', ']', '[', '{', '}', '(', ')', '\\', '%',
					'$', '&', '@', '!','"', '\'' }){
				delimeters.add(c);
			}
					
			
		}
		

		
	}

	
	
	public void process(Document_ document) {
		String[][] cells = null;
		Multimap<Pair<Integer, Integer>, Pair<String, Annotation>> inverted_annotations;
		Pair<Integer, Integer> temp;
		Set<String> np_ls;
		Set<String> added_np = new LinkedHashSet<String>();
		int start = 0, end = 0;
		int[] indx;
		String mention;
		try {
			//StanfordWrapper stanford = StanfordWrapper.getStanfordWrapper();
			for (Table_ table : document.getTables()) {
				StringBuilder mentions_list = new StringBuilder();
				cells = table.getCellsAsArray();
				inverted_annotations = table.getInverted_annotations();
				mentions_list.append(process_header(table));
				for (int i = 1; i < table.getNrow(); i++) {
					for (int j = 0; j < table.getNcol(); j++) {
						if(cells[i][j]== null)
							continue;
						temp = Pair.makePair(i, j);
						if ( table.isNumericColumn(j)&& !table.isMixed(j))
							continue;
						if(Units_Measures.isNumeric(cells[i][j]))
							continue;
						if(cells[0][j].toLowerCase().equals("notes") 
								|| cells[0][j].toLowerCase().equals("details")
								|| cells[0][j].toLowerCase().equals("references")
								|| cells[0][j].toLowerCase().equals("ref")
								|| cells[0][j].toLowerCase().equals("information")
								|| cells[0][j].toLowerCase().equals("info"))
							continue;// do not process notes cell further
						if(cells[i][j].length() > 100){
							continue;
						}
						else if( notBreakable.contains( cells[0][j].toLowerCase()) && !inverted_annotations.containsKey(temp)){
							table.addAnnotations(cells[i][j], i, j, 0, cells[i][j].length(), ANNOTATION.ENTITY);
							//continue; TODO check this condition  
						}
						
						// if (!inverted_annotations.containsKey(temp)) {
						//TODO  check over this condition
						//TODO check this, it should be faster
						np_ls = new LinkedHashSet<String>(); //stanford.getNounPhrases(cells[i][j].replace("\"", ""));
						np_ls.add(cells[i][j]);//.replace("\"", "")
						decompose(np_ls);						
						added_np.clear();
						for (String np : np_ls) {
							np = np.trim();
							if(isStopwords(np)){
								continue;
							}
							start = cells[i][j].indexOf(np);
							if (start != -1) {
								end = start + np.length();
							} else {
								indx = find_space_variant_position(np, cells[i][j]);
								if (indx != null) {
									start = indx[0];
									end = indx[1];
								}
							}

							if (start != -1) {
								mention = cells[i][j].substring(start, end);
								if (added_np.contains(mention)) {
									continue;
								}
								table.addAnnotations(mention, i, j, start, end,
										ANNOTATION.ENTITY);
								mentions_list.append("E'" + mention.replace("'", "''") + "',");
								added_np.add(mention);
							} else {// do not add
								slogger_.info("Annotation Enrichment, Could not find: " + np + "in cell" + i + "," + j);
							}

						}
						// }
					}
				}
				table.createAnnotatedNERText();
				RunTimeAnalysis.mile_stone_end = System.nanoTime();
				RunTimeAnalysis.annotatiion_enrichment_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
				RunTimeAnalysis.mile_stone_start = System.nanoTime();
				DatabaseAccess data_access = DatabaseAccess.getDatabaseAccess();
				if(mentions_list != null && mentions_list.length() >0)
					data_access.loadCandidatesFor(mentions_list);
				RunTimeAnalysis.mile_stone_end = System.nanoTime();
				RunTimeAnalysis.loading_db_time += RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
				
			}
			
		} catch (Exception exc) {
			exc.printStackTrace();
		}

	}

	private boolean isStopwords(String np) {
		for(String word : np.split(" ")){
			if(!stopwords.contains(word.trim().toLowerCase()))
				return false;
		}
		return true;
	}

	private StringBuilder process_header(Table_ table) {
		int row = 0;
		String mention;
		String[][] cells = null;
		Multimap<Pair<Integer, Integer>, Pair<String, Annotation>> inverted_annotations;
		Pair<Integer, Integer> temp;
		Set<String> np_ls;
		Set<String> added_np = new LinkedHashSet<String>();
		int start = 0, end = 0;
		int[] indx;
		cells = table.getCellsAsArray();
		inverted_annotations = table.getInverted_annotations();
		Set<Pair<String, Annotation>> annotations;
		StringBuilder header_mentions = new StringBuilder();
		String header = null;
		try {
		//	StanfordWrapper stanford = StanfordWrapper.getStanfordWrapper();
			for (int j = 0; j < table.getNcol(); j++) {
				if (table.isNumericColumn(j)) {
					// concepts
					temp = Pair.makePair(row, j);					
					header = cells[row][j];
					
					np_ls = new LinkedHashSet<String>();//stanford.getNounPhrases(header);
					np_ls.add(header);
					decompose(np_ls);					
					added_np.clear();
					if (!inverted_annotations.containsKey(temp)) {
						annotations = (Set<Pair<String, Annotation>>) inverted_annotations.get(temp);
						if (annotations != null) {
							for (Pair<String, Annotation> annotation : annotations) {
								added_np.add(annotation.first);
							}
						}

					}
					for (String np : np_ls) {
						np = np.trim();
						if(isStopwords(np))
							continue;
						start = cells[row][j].indexOf(np);
						if (start != -1) {
							end = start + np.length();
						} else {
							indx = find_space_variant_position(np, cells[row][j]);
							if (indx != null) {
								start = indx[0];
								end = indx[1];
							}
						}

						if (start != -1) {
							mention = cells[row][j].substring(start, end);
							if (added_np.contains(mention)) {
								continue;
							}
							table.addAnnotations(mention, row, j, start, end, ANNOTATION.CONCEPT);
							header_mentions.append("E'" + mention.replace("'", "''") + "',");
							added_np.add(mention);
						} else {// do not add
							slogger_.info(
									"Annotation Enrichment, Could not find: " + np + "in cell" + row + "," + j);
						}
					}

				} else {
					//TODO categories--> do nothing for now,
					continue;
				}
			}

			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return header_mentions;
	}	
	public void decompose(Set<String> np_ls) {
		Set<String> new_np_ls = new LinkedHashSet<String>();
		String[] temp_np_ls = null;
		String trimed ;
		if(np_ls == null)
			return;
		StringBuilder new_np = new StringBuilder();
		for (String np : np_ls) {
			temp_np_ls = np.split(SPLIT_WITH);	
			if(temp_np_ls.length <=1)
				return;
			for(int i=0; i < temp_np_ls.length; i++){
				new_np = new StringBuilder();
				trimed = trim(temp_np_ls[i]);
				if(trimed != null && !trimed.isEmpty())
					new_np_ls.add(trimed);
				new_np.append(temp_np_ls[i]);
				for(int j=i+1; j< temp_np_ls.length; j++){
					new_np.append(temp_np_ls[j]);
					trimed = trim(new_np.toString());
					if(trimed != null && !trimed.isEmpty())
						new_np_ls.add(trimed);
				}
			}
		}
		np_ls.addAll(new_np_ls);
	}		 
	private String trim(String string) {
		if(string.isEmpty())
			return null;
		int start=0, end=0;
		string= string.trim();
		if(string.startsWith("(") && string.endsWith(")")){
			string = string.substring(1,string.length()-1);
		}
		end = string.length()-1;
		char[] characters = string.toCharArray();
		while(start < end && is_delimeter(characters[start])&& characters[start] !='(' ){
			start++;
		}
		while(end > start && is_delimeter(characters[end]) && characters[end] !=')'){
			end--;
		}
		if(start == end)
			return "";
		return string.substring(start,end+1);
	}

	private boolean is_delimeter(char c) {
		
		if(delimeters.contains(c))
			return true;
		return false;
	}

	private int[] find_space_variant_position(String words, String cell) {
		char[] words_a = words.toCharArray();
		char[] cell_a = cell.toCharArray();
		int[] pos = null;
		int w, c;
		boolean match = true;
		for (int i = 0; i < cell_a.length; i++) {
			if (words_a[0] == cell_a[i]) {
				w = 1;
				c = w + i;
				match = true;
				while (match && w < words_a.length) {
					if(c >= cell_a.length){
						pos=null;
						break;
					}
					if (words_a[w] == cell_a[c]) {
						w++;
						c++;
					} else if (words_a[w] == ' ') {
						w++;
					} else {
						match = false;
					}
				}
				if (match) {
					pos = new int[2];
					pos[0] = i;
					pos[1] = c;
					break;
				} else {
					pos = null;
				}
			}
		}
		return pos;
	}

}
