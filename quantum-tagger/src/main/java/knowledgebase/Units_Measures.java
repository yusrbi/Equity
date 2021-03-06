package knowledgebase;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Multimap;

import edu.stanford.nlp.util.Pair;
import utils.DatabaseAccess;
import knowledgebase.Candidate.TYPE;
import lsh.Common;
import lsh.LSHTable;
import resources.Resources;

/**
 * @author yusra
 *
 */
/**
 * @author yusra
 *
 */
public class Units_Measures {
	// private static String single_qunatitiy =
	// "\\d[\\d,]*\\.\\d+|\\d*\\.\\d+|\\d[\\d,]+|[\\d]+
	// |\\d*[.]?\\d*\\s?×\\s?\\d*[.]?\\d*|\\d*[.]?\\d*\\s?\u00B1\\s?\\d*[.]?\\d*";
	public static final Pattern numeric_pattern = Pattern.compile(
			"([^0-9\\s\\.]*)[\\.]?\\s*[\\-\\+]?(\\d[\\d,]*\\.\\d+|\\d*\\.\\d+|\\d[\\d,]+|[\\d]+)(\\s*[✕×±\\/\\+\\-\\—\\|\\–°”’′\\\\\"″']\\s*\\d*[\\.]?[,]*\\d*)?\\s*([^0-9\\s\\.\\/]*)[\\.]?((\\s?\\/\\s?|\\sper\\s)\\s?(\\d*)\\s?([^0-9\\.\\s\\)\\]\\[\\-\\(\\\\]*))?[\\.]?",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	public static final Pattern numeric_pattern_repeated = Pattern.compile(
			"([\\-\\+]?(\\d[\\d,]*\\.\\d+|\\d*\\.\\d+|\\d[\\d,]+|[\\d]+)(\\s?[✕×±\\/\\+\\-\\—\\|\\–°”’′\\\\\"″']?))+\\s?([^\\s\\(\\)\\-,\\.]*)((\\s?\\/|\\sper\\s)\\s?(\\d*)?\\s?[^\\s\\)\\]\\[\\-\\(\\\\,\\.]*)?",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	public static final Pattern header_unit = Pattern.compile("[\\.]*\\(([^0-9\\)\\(]*)\\)$|[\\.]*\\sin([^0-9]*)]$",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	public static final Pattern header_unit_mod = Pattern.compile(
			"\\(([^\\)\\(]*)\\)\\s*$|(?:in|In|IN)\\b[\\(]?([^\\(\\)]*)[\\)]?\\s*$",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	public static final Pattern year_regex = Pattern.compile(
			"^\\b((19|20|18)\\d{2}[^\\d]*|(19|20|18)\\d{2}[\\-\\—\\–|\\\\\\/](19|20|18)\\d{2}[^\\d]*)$",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	public static final Pattern header_unit_with_number = Pattern.compile(
			"([^\\(0-9\\s\\.\\)]*)\\s?\\d+\\s?([^0-9\\s\\(\\)]*)\\s?",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	public static final Pattern ordinal_numbers = Pattern.compile(
			"\\d*(?:1st|2nd|3rd|5th|4th|6th|8th|7th|9th|0th|12th|13th|11th)",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	public static final Pattern word_endding_with_s = Pattern.compile("^\\d*\\s*[^\\s]{3,}s\\b[^\\w]*$",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	public static final Pattern word_boundary = Pattern.compile("^\\s?\\d*\\s*\\b(\\w*)\\b[^\\w]*$",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
	@SuppressWarnings("unused")
	private static String numbers = "one,two,three,four,five,six,seven,eight,nine,ten,"
			+ "eleven,twelve,thirteen,fourteen,fifteen,sixteen,seventeen,eighteen,nineteen,twenty"
			+ "hundered,hundereds,thousand,thousands,millions,million,billion,billions,trillion,trillions";
	
	private static String increase = "+,increase,growth,rise,raise,increment,gain,maximize,addition,advance,promotion,boost";

	private static String decrease = "-,decrease,minimize,decline,loss,shrinkage,cutback,discount,reduction,cut";
	@SuppressWarnings("unused")
	private static String compound = "//,per";
	@SuppressWarnings("unused")
	private static String percentage = "percent,per cent,percentage,percnt";
	
	private static String terms = "less than,more than,up to,maximum of,minimum of,avg,average,exact,rate,of,current,in,change in,change,est.,est,aprox.";
	/**
	 * The Map of aliases and the unit name--> unique
	 */
	private static Multimap<String, String>  aliases;
	// private static Map<String, Set<String>> abbreviations;
	/**
	 * Map of class aliases and the corresponding class name
	 */
	private static Map<String, String> classes;
	/**
	 * list of the dimensionless classes names (they are already included in
	 * classes with their aliases)
	 */
	private static Set<String> dimensionless_classes;
	/**
	 * Map of unit name and the unit object
	 */
	private static Map<String, Unit> units;
	private static Units_Measures instance = null;
	// private static Map<String, String> dimensionless_measures;
	/**
	 * The map of dimension alias and pair of <dimension name, dimension
	 * Wikipedia Title>
	 */
	private static Map<String, Pair<String, String>> dimensions;
	/**
	 * The dimensions' aliases
	 */
	private static LSHTable lsh_dimensions;

	/**
	 * The classes aliases
	 */
	private static LSHTable lsh_classes;
	/**
	 * Statistical modifiers aliases and their names
	 */
	private static Map<String, String> modifiers;
	/**
	 * The cut off for Jaro Winkler Distance, set by the resources loader from
	 * the properties file
	 */
	private static Double sim_cut_off;

	private static Set<String> stop_words;
	private Map<String, Map<String, String>> domain_terms_expantion;

	public Map<String, Map<String, String>> getDomain_terms_expantion() {
		return domain_terms_expantion;
	}

	private Units_Measures() {
		aliases = null;
		units = new HashMap<String, Unit>();
		stop_words = new LinkedHashSet<String>();
		init_stopwords();
	}

	private void init_stopwords() {
		stop_words.addAll(Resources.getResources().getNumbers());
		for (String word : increase.split(",")) {
			stop_words.add(word);
		}
		for (String word : decrease.split(",")) {
			stop_words.add(word);
		}
		for (String word : terms.split(",")) {
			stop_words.add(word);
		}
	}

	public static Units_Measures getUnits_Measures() {
		if (instance == null) {
			instance = new Units_Measures();
		}
		return instance;
	}

	public void addUnit(Unit unit) {
		units.put(unit.getUnit_key(), unit);
	}

	public Multimap<String, String>  getAliases() {
		return aliases;
	}

	public void setAliases(Multimap<String, String> units_aliases) {
		Units_Measures.aliases = units_aliases;
	}

	// public Map<String, Set<String>> getAbbreviations() {
	// return abbreviations;
	// }

	/*
	 * public void setAbbreviations(Map<String, Set<String>> abbreviations) {
	 * Units_Measures.abbreviations = abbreviations; }
	 */
	private static List<Candidate> getCandidates(String mention, String column_header,
			List<String> header_annotations) {
		Candidate cand = null;
		String[] parts = split(mention);
		List<Candidate> candidates = new LinkedList<Candidate>();
	
		Set<String> match = null;
		Set<String> classes = getClassesNames(header_annotations);
		Matcher matcher = null;
		if (parts != null) {

			if (parts[0] != null && !parts[0].trim().isEmpty()) {
				// parts[0] = parts[0].toLowerCase().trim();
				if (aliases.containsKey(parts[0].toLowerCase().trim())) {
					match = (Set<String>) aliases.get(parts[0].toLowerCase().trim());
				}
				if (match != null) {
					candidates.addAll(getCandidatesScores(parts[0].toLowerCase().trim(), match, classes,mention, parts[1]));
					match = null;
				}
			}
			if (parts[4] != null && !parts[4].trim().isEmpty()) {
				if (aliases.containsKey(parts[3].toLowerCase().trim())) {
					match = (Set<String>) aliases.get(parts[3].toLowerCase().trim());
				} else {
					// composite unit
					// TODO
				}
				if (match != null) {
					candidates.addAll(getCandidatesScores(parts[3].toLowerCase().trim(), match, classes,mention, parts[1]));
					match = null;
				}
			}
			if (candidates.size() == 0 &&  parts[2] != null && !parts[2].trim().isEmpty()) {
				if (aliases.containsKey(parts[2].toLowerCase().trim())) {
					match = (Set<String>) aliases.get(parts[2].toLowerCase().trim());
				}
				if (match != null) {
					candidates.addAll(getCandidatesScores(parts[2].toLowerCase().trim(), match, classes,mention, parts[1]));
					match = null;
				} else if (parts[2].equals("%")) {
					candidates.add(new CandidateQuantity("Percentage(NULL,NULL)", TYPE.QUANTITY, 1.0,
							parts[1], "Percentage","NULL","NULL"));
				}
			}
			
			// search using the header
			if (candidates.size() == 0 && !column_header.isEmpty()) {

				String unit = getHeaderUnit(column_header);
				if (unit != null && !unit.isEmpty()) {
					match =(Set<String>) aliases.get(unit.toLowerCase().trim());
					if (match != null) {
						candidates.addAll(getCandidatesScores(unit.toLowerCase().trim(), match, null,mention, parts[1]));
						match = null;
					} else {
						for (String un : unit.split(" ")) {
							if (aliases.containsKey(un.trim().toLowerCase())) {
								match = (Set<String>)aliases.get(un.trim().toLowerCase());
							}
							if (match != null) {
								candidates.addAll(getCandidatesScores(un.toLowerCase().trim(), match, classes,mention, parts[1]));
								match = null;
							}
						}
					}

				}

				if (candidates.size() == 0) {// search for the full header day,
												// year, date, etc
					column_header = stripHeaderUnit(column_header);
					if (aliases.containsKey(column_header.toLowerCase().trim())) {
						match =(Set<String>) aliases.get(column_header.toLowerCase().trim());
					}
					if (match != null) {
						candidates.addAll(getCandidatesScores(column_header.toLowerCase().trim(), match, classes, mention, parts[1]));
						match = null;
					}
				}
				if (candidates.size() == 0 && column_header.toLowerCase().startsWith("% of")
						|| column_header.toLowerCase().startsWith("percentage of")
						|| column_header.toLowerCase().startsWith("percent of")) {
					// percentage
					candidates.add(new CandidateQuantity("Percentage(NULL,NULL)", TYPE.QUANTITY, 1.0,
							 parts[1], "Percentage","NULL","NULL"));
				}
				if (candidates.size() == 0 && header_annotations != null) { // if
																			// still
																			// equal
																			// zero
					// search for a match from the marked annotations of the
					// header
					for (String annotation : header_annotations) {
						match = (Set<String>)aliases.get(annotation.toLowerCase().trim());
						if (match != null) {
							candidates.addAll(getCandidatesScores(annotation.toLowerCase().trim(), match, classes,mention, parts[1]));
							match = null;
						}
					}

				}
			}
			if (candidates.size() == 0) {
				matcher = year_regex.matcher(mention);

				if (matcher.matches()) {
					// annotate as a year
					cand = new CandidateQuantity("Temporal(Time,Calendar_year)", TYPE.QUANTITY, 1.0,
							 parts[1], "Temporal","Time","Calendar_year");
					candidates.add(cand);
				} else {
					matcher = ordinal_numbers.matcher(mention.trim().toLowerCase());
					if (matcher.matches()) {
						// ordinal number rank
						cand = new CandidateQuantity("Rank/Order(NULL,NULL)", TYPE.QUANTITY, 1.0,
								 parts[1], "Rank/Order","NULL","NULL");
						candidates.add(cand);
					} else {
						matcher = word_boundary.matcher(mention.trim().toLowerCase());
						if (matcher.matches()) {
							if (aliases.containsKey(matcher.group(1))) {
								match = (Set<String>)aliases.get(matcher.group(1));
								candidates.addAll(getCandidatesScores(matcher.group(1), match, classes,mention, parts[1]));
							}
						}
					}
				}

				if ((column_header == null || column_header.isEmpty())) {
					// Temporal(time,Calendar_year)
					// detect year and dimensionless in text
					matcher = word_endding_with_s.matcher(mention.trim().toLowerCase());
					if (matcher.matches()) {
						cand = new CandidateQuantity("Count(NULL,NULL)", TYPE.QUANTITY, 1.0, 
								parts[1], "Count","NULL","NULL");
						candidates.add(cand);
					}

				}
			}

		} else {// nominal cases as: hour month annual TODO Nominal quantities
				// thousand etc
			if (aliases.containsKey(mention.trim().toLowerCase())) {
				match = (Set<String>)aliases.get(mention.trim().toLowerCase());
			}
			if (match != null) {
				candidates.addAll(getCandidatesScores(mention.toLowerCase().trim(), match, classes,mention, parts[1]));
				match = null;
			}
			return candidates;
		}
		return CandidatesSearch.removeDuplicates(candidates);
	}

	public static Collection<? extends Candidate> getCandidatesScores(String mention, Set<String> matching_units,
			Set<String> header_classes, String cell_content, String magnitude) {
		DatabaseAccess data_access;
		List<Candidate> candidates = new LinkedList<Candidate>();
		Matcher matcher;
		Candidate candidate = null;
		double count = 0;
		int max = 0;
		try {
			data_access = DatabaseAccess.getDatabaseAccess();
			max = data_access.getMaxMentionUnitCount();
			Unit unit = null;
			boolean dimensionless = isDimensionlessClasses(header_classes);
			boolean is_statistical = isStatistical(header_classes);
			for (String unit_key : matching_units) {
				unit = units.get(unit_key);
				if (unit == null)
					continue;
				if (header_classes != null && !header_classes.isEmpty()
						&& !header_classes.contains(unit.getClassName())) {
					if (dimensionless && !is_statistical)
						continue; // escape in all cases
					// classes does not match

					// if (dimensionless &&
					// isPhysicalClasses(unit.getClassName())) {
					// continue;
					// }else if(isMonetryClass()){
					//
					// }
				}
				count = (double) data_access.getCountUnitMention(mention, unit_key);
				if (count <= 0)
					count = 1;
				// if (!units.get(unit_key).getUnit_wiki_title().isEmpty())

				if(unit.getUnit_key().equals("date")){//check if it is a year
					//TODO find a better way to check on this
					matcher = year_regex.matcher(cell_content.trim());
					if(matcher.matches()){
						candidate = new CandidateQuantity("Temporal(Time,Calendar_year)", TYPE.QUANTITY, count / (double) max,
								magnitude, "Temporal","Time","Calendar_year");
					}else{
						candidate = new CandidateQuantity(unit.getfullyQualifiedName(), TYPE.QUANTITY, count / (double) max,
								magnitude, unit.getClassName(), unit.getDimension(), unit.getUnitID());
					}
				}else{
					candidate = new CandidateQuantity(unit.getfullyQualifiedName(), TYPE.QUANTITY, count / (double) max,
							magnitude, unit.getClassName(), unit.getDimension(), unit.getUnitID());
						
				}
				
				candidates.add(candidate);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return candidates;
	}

	private static boolean isStatistical(Set<String> header_classes) {
		if (header_classes == null || header_classes.size() == 0)
			return false;
		for (String class_ : header_classes) {
			if (!class_.equals("Statistical"))
				return false;
		}
		return true;
	}

//	private static boolean isPhysicalClasses(String className) {
//
//		return className.equals("physical qunatity") || className.equals("Monetary") || className.equals("Temporal")
//				|| className.equals("Duration");
//	}

	private static boolean isDimensionlessClasses(Set<String> header_classes) {
		if (header_classes == null || header_classes.size() == 0)
			return false;
		for (String class_ : header_classes) {
			if (!dimensionless_classes.contains(class_))
				return false;
		}
		return true;
	}

	public static String stripHeaderUnit(String column_header) {

		Matcher matcher = header_unit_mod.matcher(column_header);
		if (matcher.find()) {
			return column_header.substring(0, column_header.indexOf(matcher.group(0)));
		}
		return column_header;
	}

	public static String getHeaderUnit(String column_header) {
		Matcher matcher = header_unit_mod.matcher(column_header);
		String unit = null;
		if (matcher.find() && matcher.group(1) != null) {
			unit = stripModifiers(matcher.group(1));
			matcher = header_unit_with_number.matcher(unit);
			if (matcher.find()) {
				unit = stripModifiers(matcher.group(1).trim() + " " + matcher.group(2).trim());
			}
			unit = unit.trim();
		}

		return unit;
	}

	public static String stripModifiers(String text) {
		if (text == null || text.isEmpty())
			return text;
		StringBuilder filtered = new StringBuilder();
		for (String word : text.split("\\s+")) {
			if (!stop_words.contains(word)) {
				filtered.append(word + " ");
			}
		}
		return filtered.toString();
	}

	public static List<Candidate> getCandidateMonetryUnits(String mention) {
		// currency
		// TODO use locality sensitive hashing where the hash value is
		// calculated using LSH and then use approximate matching with filtering
		// threshold
		return getCandidates(mention, "", null);
	}

	private static String[] split(String mention) {
		Matcher matcher = numeric_pattern.matcher(mention);
		String[] parts = null;
		if (matcher.find()) {
			parts = new String[6];
			parts[0] = matcher.group(1).trim(); // $ / EMPTY
			parts[1] = matcher.group(2) + (matcher.group(3) == null ? "" : matcher.group(3)); // 50
																								// ✕
																								// 123.4
			if (matcher.group(3) != null && matcher.group(3).trim().equals("°"))
				parts[2] = matcher.group(3).trim() + matcher.group(4);
			else
				parts[2] = matcher.group(4); // lb

			parts[3] = matcher.group(4) + (matcher.group(5) == null ? "" : matcher.group(5)); // lb
			// per|/
			// 100
			// kg
			parts[4] = matcher.group(8); // kg
			parts[5] = matcher.group(7); // 100

		} else {
			return null;
		}
		return parts;
	}

	public static List<Candidate> getCandidateUnits(String mention, String column_header,
			List<String> header_annotations) {
		return getCandidates(mention, column_header, header_annotations);
	}

	public static List<Candidate> getCandidateUnits(String mention) {
		return getCandidates(mention, "", null);
	}

	public static List<Candidate> getCandidateMonetryUnits(String mention, String column_header,
			List<String> header_annotations) {
		return getCandidates(mention, column_header, header_annotations);
	}

	public static boolean is_qunatity(String mention) {
		Matcher matcher = numeric_pattern.matcher(mention);
		if (matcher.matches()) {
			return true;
		}
		return false;
	}

	// for(String alias: unit.getUnit_aliases()){
	// if(!alias.isEmpty()){
	// if(aliases.containsKey(alias)){
	// units = aliases.get(alias);
	// units.add(unit.getUnit_key());
	// }else{
	// units = new LinkedHashSet<String>();
	// units.add(unit.getUnit_key());
	// aliases.put( alias, units);
	// }
	// }
	// }
	// for(String abbreviation: unit.getUnit_abbreviations()){
	// if(!abbreviation.isEmpty()){
	// if(abbreviations.containsKey(abbreviation)){
	// units = abbreviations.get(abbreviation);
	// units.add(unit.getUnit_key());
	// }else{
	// units = new LinkedHashSet<String>();
	// units.add(unit.getUnit_key());
	// abbreviations.put( abbreviation, units);
	// }
	// }
	// }

	/*
	 * public void set_dimensionless_measures( Map<String, String>
	 * dimensionless_measures) { Units_Measures.dimensionless_measures =
	 * dimensionless_measures; lsh_dimensionless_measures = new LSHTable(5, 12,
	 * 100, 999999999, 0.5); // Set<String> unique_measures = new
	 * LinkedHashSet<String>(); // for(String measure :
	 * dimensionless_measures.values()){ // unique_measures.add(measure); // }
	 * // for(String measure : unique_measures){ //
	 * lsh_dimensionless_measures.put(Common.getCounter(measure)); // }
	 * for(String alias : dimensionless_measures.keySet()){
	 * lsh_dimensionless_measures.put(Common.getCounter(alias)); } }
	 */

	public void setDimensions(Map<String, Pair<String, String>> dimensions) {
		Units_Measures.dimensions = dimensions;
		lsh_dimensions = new LSHTable(5, 12, 100, 999999999, 0.5);
		for (String alias : dimensions.keySet()) {
			lsh_dimensions.put(Common.getCounter(alias));
		}
	}

	/*
	 * public Map<String, String> getDimensionless_measures() { return
	 * dimensionless_measures; }
	 */
	public Map<String, Pair<String, String>> getDimensions() {
		return dimensions;
	}

	public List<Candidate> getDimensions(String mention) {
		List<Candidate> candidates = new LinkedList<Candidate>();
		double score = 0.0;
		String wiki_title;
		List<String> matches = lsh_dimensions.deduplicate(Common.getCounter(mention));
		for (String match : matches) {
			if (dimensions.get(match) == null)
				continue;
			wiki_title = dimensions.get(match).second;
			score = StringUtils.getJaroWinklerDistance(match, mention);
			if (score > sim_cut_off) {
				candidates.add(
						new CandidateConcept("DIMENSION." + ((wiki_title == null || wiki_title.isEmpty()) ? "" : wiki_title),
								TYPE.DIMENSION, score, (wiki_title == null || wiki_title.isEmpty()) ? ""
										: "https://en.wikipedia.org/wiki/" + wiki_title));
			}
		}
		/*
		 * if(candidates.size()== 0 ){ if(mention.endsWith("date")){ score =
		 * (double)"date".length()/(double)mention.length(); candidates.add(new
		 * CandidateQuantity("Calendar_date",TYPE.DIMENSION, score)); }else
		 * if(mention.endsWith("time")){ score = (double)
		 * "time".length()/(double)mention.length(); candidates.add(new
		 * CandidateQuantity("Time",TYPE.DIMENSION, score)); }else if
		 * (mention.endsWith("rate")){ score =
		 * (double)"rate".length()/(double)mention.length(); candidates.add(new
		 * CandidateQuantity("Rate_(mathematics)",TYPE.DIMENSION, score)); }else if
		 * (mention.contains("cost")|| mention.contains("price") ||
		 * mention.contains("expenditure")){ score =
		 * (double)"cost".length()/(double)mention.length(); candidates.add(new
		 * CandidateQuantity("Currency",TYPE.DIMENSION, score)); } }
		 */
		return candidates;
	}

	/*
	 * public List<Candidate> getDimensionless_measures(String mention) {
	 * List<Candidate> candidates = new LinkedList<Candidate>(); double score
	 * =0.0; List<String> matches =
	 * lsh_dimensionless_measures.deduplicate(Common.getCounter(mention));
	 * for(String match : matches){ score =
	 * StringUtils.getJaroWinklerDistance(match, mention); if(score
	 * >sim_cut_off){ candidates.add(new
	 * Candidate(dimensionless_measures.get(match),TYPE.DIMENSIONLESS_MEASURE,
	 * score)); } } if(candidates.size()== 0 ){ if(mention.contains("count")){
	 * score = (double)"count".length()/(double)mention.length();
	 * candidates.add(new Candidate("count",TYPE.DIMENSIONLESS_MEASURE, score));
	 * }else if(mention.endsWith("ratio")){ score =
	 * (double)"ratio".length()/(double)mention.length(); candidates.add(new
	 * Candidate("ratio",TYPE.DIMENSIONLESS_MEASURE, score)); }else
	 * if(mention.contains("%")){ score =
	 * (double)"%".length()/(double)mention.length(); candidates.add(new
	 * Candidate("Percentage",TYPE.DIMENSIONLESS_MEASURE, score)); } } return
	 * candidates; }
	 *
	 */
	public void setStatisticalModifiers(Map<String, String> modifiers) {
		Units_Measures.modifiers = modifiers;

	}

	public Map<String, String> getModifiers() {
		return modifiers;
	}

	public static String stripStatisticalModifiers(String mention) {

		String new_mention = mention;
		for (String modifier : modifiers.keySet()) {
			new_mention = new_mention.replace(modifier, "");
		}

		return new_mention.trim();
	}

	public static void setSimCutoff(Double value) {
		sim_cut_off = value;

	}

	public void setClasses(Map<String, String> classes) {
		Units_Measures.classes = classes;
		lsh_classes = new LSHTable(5, 12, 100, 999999999, 0.5);
		for (String alias : classes.keySet()) {
			lsh_classes.put(Common.getCounter(alias));
		}

	}

	public List<Candidate> getClasses(String mention) {
		List<Candidate> candidates = new LinkedList<Candidate>();
		double score = 0.0;
		List<String> matches = lsh_classes.deduplicate(Common.getCounter(mention));
		for (String match : matches) {
			score = StringUtils.getJaroWinklerDistance(match, mention);
			if (score > sim_cut_off) {
				candidates.add(new CandidateClass("QUANTITY_CLASS." + classes.get(match), TYPE.QUANTITY_CLASS, score, "qkb/class/"+classes.get(match)));
			}
		}
		return CandidatesSearch.removeDuplicates(candidates);
	}

	public List<Candidate> getClasses(List<String> header_annotations) {
		if (header_annotations == null)
			return null;
		header_annotations.sort(new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				if (arg1.length() > arg0.length())
					return 1;
				if (arg1.length() < arg0.length())
					return -1;
				return 0;
			}

		});
		List<Candidate> candidates = new LinkedList<Candidate>();
		StringBuilder annotation_so_far = new StringBuilder();
		double score = 0.0;
		for (String annotation : header_annotations) {
			if (annotation_so_far.length() > 0 && annotation_so_far.toString().contains(annotation)) {
				continue;
			}
			List<String> matches = lsh_classes.deduplicate(Common.getCounter(annotation));
			for (String match : matches) {
				score = StringUtils.getJaroWinklerDistance(match, annotation);
				if (score >= sim_cut_off) {
					annotation_so_far.append(" / " + annotation);
					candidates
							.add(new CandidateClass("QUANTITY_CLASS." + classes.get(match), TYPE.QUANTITY_CLASS, score, "qkb/class/"+classes.get(match)));
				}
			}
		}
		return CandidatesSearch.removeDuplicates(candidates);
	}

	private static Set<String> getClassesNames(List<String> header_annotations) {
		if (header_annotations == null)
			return null;
		Set<String> candidates = new LinkedHashSet<String>();
		double score = 0.0;
		for (String annotation : header_annotations) {
			List<String> matches = lsh_classes.deduplicate(Common.getCounter(annotation));
			for (String match : matches) {
				score = StringUtils.getJaroWinklerDistance(match, annotation);
				if (score > 0.95) {
					candidates.add(classes.get(match));
				}
			}
		}
		return candidates;
	}

	public static Set<String> getClassesNames(String header) {
		if (header == null || header.isEmpty())
			return null;
		Set<String> candidates = new LinkedHashSet<String>();
		double score = 0.0;
		List<String> matches = lsh_classes.deduplicate(Common.getCounter(header));
		for (String match : matches) {
			score = StringUtils.getJaroWinklerDistance(match, header);
			if (score > 0.95) {
				candidates.add(classes.get(match));
			}
		}

		return candidates;
	}

	public static Set<String> getDimensionsForUnit(String unit) {
		Set<String> match = null;
		if (aliases.containsKey(unit)) {
			match = (Set<String>)aliases.get(unit);
		}
		return match;
	}

	public static List<Candidate> getDimensionsCandidatesForUnit(String unit) {
		Set<String> match = getDimensionsForUnit(unit);
		List<Candidate> candidates = new LinkedList<Candidate>();
		String wiki_title;
		if (match != null) {
			double score = 1.0 / (double) match.size();
			for (String dimension : match) {
				if (dimensions.get(dimension) == null)
					continue;
				wiki_title = dimensions.get(dimension).second;
				candidates.add(
						new CandidateConcept("DIMENSION." + ((wiki_title == null || wiki_title.isEmpty()) ? "" : wiki_title),
								TYPE.DIMENSION, score, (wiki_title == null || wiki_title.isEmpty()) ? ""
										: "https://en.wikipedia.org/wiki/" + wiki_title));
			}
		}
		return candidates;
	}

	public static Set<String> getDimensionless_classes() {
		return dimensionless_classes;
	}

	public static void setDimensionless_classes(Set<String> dimensionless_classes) {
		Units_Measures.dimensionless_classes = dimensionless_classes;
		dimensionless_classes.remove("Duration");
	}

	public boolean isClassStringDistanceSufficient(double score) {
		if (score > sim_cut_off)
			return true;
		else
			return false;
	}

	public boolean isDimensionless(String class_name) {
		return dimensionless_classes.contains(class_name);
	}

	public boolean isTemporalClass(String class_name) {
		return class_name.equals("Temporal");
	}

	public boolean isMonetryClass(String class_name) {
		return class_name.equals("Monetary");
	}

	public boolean isDurationClass(String class_name) {
		// TODO Auto-generated method stub
		return class_name.equals("Duration");
	}

	public static Pair<String, Double> getTheBestDimensionlessClass(List<Candidate> candidate_classes) {
		if (candidate_classes == null)
			return null;
		double score = 0;
		String selected = null;
		for (Candidate candidate : candidate_classes) {
			if (dimensionless_classes.contains(candidate.getSemanticTargetId())) {
				if (candidate.getScore() > score) {
					score = candidate.getScore();
					selected = candidate.getSemanticTargetId();
				}
			}
		}
		return new Pair<String, Double>(selected, score);
	}

	public static boolean isNumber(String mention) {
		if (mention.equals(".") || mention.equals("+") || mention.equals("-") || mention.equals("%")
				|| mention.equals(","))
			return false;
		Pattern number_pattern = Pattern.compile(
				"\\s*[-+%]?\\s?(\\d[\\d,]*\\.\\d*|\\d*\\.\\d*|\\d[\\d,]+|[\\d]*)\\s?[%|°|”|’|′|\"|″|']?\\s*",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
		Matcher matcher = number_pattern.matcher(mention);
		return matcher.matches();
	}

	public static boolean isLetteralNumber(String word) {

		return Resources.getResources().getNumbers().contains(word);
	}

	public void setDomain_terms_expansion(Map<String, Map<String, String>> domain_terms_expantion) {
		this.domain_terms_expantion = domain_terms_expantion;

	}

	public boolean isNoUnit(String class_name) {

		return isTemporalClass(class_name) || isDimensionless(class_name) || isDurationClass(class_name);
	}

	public static List<Candidate> getClassesOfTableTitle(String table_title) {
		if (table_title == null || table_title.isEmpty())
			return null;
		List<Candidate> candidates = new LinkedList<Candidate>();
		double score = 0.0;
		for (String word : table_title.split(" ")) {
			List<String> matches = lsh_classes.deduplicate(Common.getCounter(word));
			for (String match : matches) {
				score = StringUtils.getJaroWinklerDistance(match, word);
				if (score > sim_cut_off) {
					candidates
							.add(new CandidateClass("QUANTITY_CLASS." + classes.get(match), TYPE.QUANTITY_CLASS, score, "qkb/class/"+classes.get(match)));
				}
			}
		}
		return CandidatesSearch.removeDuplicates(candidates);
	}

	public static boolean isNumeric(String string) {
		Matcher matcher = numeric_pattern_repeated.matcher(string);
		return matcher.matches();
	}

	public boolean isPhysicalClass(String class_name) {
		
		return class_name.equals("physical qunatity");
	}

	public String getExactDimension(String text) {
		String dimension ="";
		List<Candidate> candidates = getDimensions(text);
		for(Candidate cand : candidates){
			if(cand.getScore() > 0.95){
				dimension = cand.getSemanticTargetId();
			}
		}
		return dimension;
	}

	

}
