package knowledgebase;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import utils.DatabaseAccess;
import knowledgebase.Candidate.TYPE;
import annotations.Annotation;
import data.Document_;
import data.Table_;
import edu.stanford.nlp.util.Pair;
import executer.RunTimeAnalysis;

public class CandidatesSearch {

	public static void findCandidates(HashMap<String, Document_> documents) throws SQLException {
		// TODO Auto-generated method stub
		for (Document_ document : documents.values()) {
			findCandidates(document);
		}
	}

	public static void findCandidates(Document_ document) throws SQLException {
		Entities.reset_count();		
		DatabaseAccess data_access = DatabaseAccess.getDatabaseAccess();
		data_access.load_units_mentions();
		RunTimeAnalysis.mile_stone_end = System.nanoTime();
		RunTimeAnalysis.loading_db_time += RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
		RunTimeAnalysis.mile_stone_start = System.nanoTime();
		// find the measures and the units
		Matcher matcher ;
		Multimap<String, Annotation> mentions = document.getMentions();
		if (mentions != null && mentions.size() >0) {

			Multimap<String, Candidate> candidates = HashMultimap.create();
			List<Candidate> candidates_a;
			Candidate cand;
			for (String mention : mentions.keySet()) {
				if (mention == "") {
					continue;
				}
				for (Annotation annotation : mentions.get(mention)) {
					switch (annotation.getAnnotation()) {// for each different
															// annotation
					case MONEY:
						candidates_a = Units_Measures.getCandidateMonetryUnits(mention);
						if (candidates_a != null && candidates_a.size() > 0) {
							normalize(candidates_a);
							candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
						}
						break;
					case PERCENT:
						cand = new CandidateQuantity("Percentage(NULL,NULL)", TYPE.QUANTITY, 1.0,
								strip(mention), "Percentage", "NULL","NULL");
						candidates_a = new LinkedList<Candidate>();
						candidates_a.add(cand);
						candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
						break;
					case DATE:
						matcher = Units_Measures.year_regex.matcher(mention.trim());
						if (matcher.matches()) {
							// annotate as a year
							cand = new CandidateQuantity("Temporal(Time,Calendar_year)", TYPE.QUANTITY, 1.0,
									mention, "Temporal", "Time","Calendar_year" );
							
						} else {
						cand = new CandidateQuantity("Temporal(Calendar_date,Calendar_date)", TYPE.QUANTITY, 1.0,
								mention, "Temporal", "Calendar_date","Calendar_date");
						}
						candidates_a = new LinkedList<Candidate>();
						candidates_a.add(cand);
						candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
						break;
					case TIME:
						cand = new CandidateQuantity("Temporal(Time,time)", TYPE.QUANTITY, 1.0,
								mention, "Temporal", "Time","time");
						candidates_a = new LinkedList<Candidate>();
						candidates_a.add(cand);
						candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
						break;
					case OTHER_QUANTITY:
						candidates_a = Units_Measures.getCandidateUnits(mention);
						if (candidates_a != null && candidates_a.size() > 0) {
							normalize(candidates_a);
							candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
						}
						break;
					case LOCATION:
					case ORGANIZATION:
					case PERSON:
					case ENTITY:
						break;
					default:
						break;
					}
				}
			}
			document.setCandidates(candidates);
			// Find candidate entities
			
			Map<String, List<Candidate>> entities = Entities.getCandidates(document.getAnnotatedContents(), "LOCAL");
			
			document.addCandidateEntitie(entities);
		}
		// find candidates in the table
		for (Table_ table : document.getTables()) {
			findCandidates(table, document);

		}

	}

	private static void findCandidates(Table_ table, Document_ document) {
		Multimap<String, Annotation> mentions = table.getMentions();
		if (mentions == null) {
			return;
		}
		Multimap<String, Candidate> candidates = table.getCandidates();
		List<Candidate> candidates_a;
		Candidate cand;
		List<String> expanded_annotations = null;
		Matcher matcher;
		// first thing to do is to get entities	
		// Find candidate entities
		Map<String, List<Candidate>> entities = Entities.getCandidates(table.getAnnotatedContents(), "PRIOR");
		if (entities != null)
			table.addCandidateEntities(entities);

		for (String mention : mentions.keySet()) {
			candidates_a = null;
			for (Annotation annotation : mentions.get(mention)) {
				expanded_annotations = expand(table.getAnnotations(0, annotation.getColIndx()), document);
				switch (annotation.getAnnotation()) {
				case MONEY:
					candidates_a = Units_Measures.getCandidateMonetryUnits(mention,
							expand(table.getCell(0, annotation.getColIndx()), document), expanded_annotations);
					if (candidates_a != null && candidates_a.size() > 0) {
						normalize(candidates_a);
						candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
					}
					break;
				case PERCENT:
					cand = new CandidateQuantity("Percentage(NULL,NULL)", TYPE.QUANTITY, 1.0,
							strip(mention), "Percentage", "NULL","NULL");
					candidates_a = Units_Measures.getCandidateUnits(mention,
							expand(table.getCell(0, annotation.getColIndx()), document), null);
					if (!candidates_a.contains(cand)) {
						candidates_a.add(cand);
					}
					candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
					break;
				case DATE:
					matcher = Units_Measures.year_regex.matcher(mention.trim());
					if (matcher.matches()) {
						// annotate as a year
						cand = new CandidateQuantity("Temporal(Time,Calendar_year)", TYPE.QUANTITY, 1.0,
								mention, "Temporal", "Time","Calendar_year");
						
					} else {
					cand = new CandidateQuantity("Temporal(Calendar_date,Calendar_date)", TYPE.QUANTITY, 1.0,
							mention, "Temporal", "Calendar_date","Calendar_date");
					}
				
					candidates_a = Units_Measures.getCandidateUnits(mention,
							expand(table.getCell(0, annotation.getColIndx()), document), null);
					if (!candidates_a.contains(cand)) {
						candidates_a.add(cand);
					}
					candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
					break;
				case TIME:
					cand = new CandidateQuantity("Temporal(Time,time)", TYPE.QUANTITY, 1.0,
							mention, "Temporal", "Time","time");
					candidates_a = Units_Measures.getCandidateUnits(mention,
							expand(table.getCell(0, annotation.getColIndx()), document), null);
					if (!candidates_a.contains(cand)) {
						candidates_a.add(cand);
					}
					candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
					break;
				case OTHER_QUANTITY:
					candidates_a = Units_Measures.getCandidateUnits(mention,
							expand(table.getCell(0, annotation.getColIndx()), document), expanded_annotations);
					if (candidates_a != null && candidates_a.size() > 0) {
						normalize(candidates_a);
						candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
					}
					break;
				case CLASS:
					candidates_a = Classes.getCandidates(mention);
					if (candidates_a != null && candidates_a.size() > 0) {
						normalize(candidates_a);
						candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
					}
					break;
				case CONCEPT:
					// String expanded_mention = expand(mention,document);
					candidates_a = Concepts.getCandidates(mention);
					if (candidates_a != null && candidates_a.size() > 0) {
						normalize(candidates_a);
						candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
					}
					break;
				case LOCATION:
				case ORGANIZATION:
				case PERSON:
				case ENTITY:
					//TODO 
//					if (!candidates.containsKey(mention + "_" + annotation.getUniqueID())) {
//						// only check form mentions if no candidates are found
//						// in AIDA
//						candidates_a = Entities.findInDB(mention);
//						if (candidates_a != null && candidates_a.size() > 0) {
//							normalize(candidates_a);
//							candidates.putAll(mention + "_" + annotation.getUniqueID(), candidates_a);
//						}
//					}
					break;
				default:
					break;
				}
			}
		}
		propagateQuantitiyCandidates(table, document);
		return;
	}

	private static String strip(String mention) {
		String number = mention;
		Pattern pattern = Pattern.compile(
				"([^0-9\\s\\.\\-\\+]*)?[\\.]?\\s*([\\-\\+]?(\\d[\\d,]*\\.\\d+|\\d*\\.\\d+|\\d[\\d,]+|[\\d]+))\\s*([^0-9\\s\\.]*)[\\.]?",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(mention);
		if (matcher.matches()) {
			number = matcher.group(2);
		}
		return number;
	}

	private static String expand(String mention, Document_ document) {
		String expanded_header = null;
		Map<String, Map<String, String>> domain_terms_expansion = Units_Measures.getUnits_Measures()
				.getDomain_terms_expantion();
		Map<String, String> term_expansion = null;

		for (String domain_keywords : domain_terms_expansion.keySet()) {
			for (String keyword : domain_keywords.split(",")) {
				if (document.continsKeyword(keyword)) {
					// assign this domain term expansion
					term_expansion = domain_terms_expansion.get(domain_keywords);
					expanded_header = term_expansion.get(mention);
					if (expanded_header != null)
						return expanded_header;
				}

			}
		}
		// if(term_expansion != null){
		//
		// }

		return mention;
	}

	// TODO propagate Horizontally
	private static void propagateQuantitiyCandidates(Table_ table, Document_ document) {
		Set<Pair<String, Annotation>> annotations;
		String annotation_id;
		Set<Candidate> cands = null;
		List<Candidate> header_cand_classes = null;
		List<Candidate> title_classes = null;
		Units_Measures units_measures = Units_Measures.getUnits_Measures();
		Set<Pair<String, Annotation>> header_annotations;
		String header_text;
		Matcher matcher = null;
		Candidate cand = null;
		String table_title;
		for (int j = 0; j < table.getNcol(); j++) {
			if (!table.isNumericColumn(j) || table.isMixed(j))
				continue;
			cands = null;
			header_text = table.getCell(0, j);
			header_text = expand(header_text, document);
			header_annotations = table.getAnnotations(0, j);
			header_cand_classes = Units_Measures.getUnits_Measures().getClasses(expand(header_annotations, document));
			for (int i = 1; i < table.getNrow(); i++) {
				annotations = table.getAnnotations(i, j);
				if (annotations == null)
					continue;
				for (Pair<String, Annotation> annotation : annotations) {
					switch (annotation.second.getAnnotation()) {
					case PERCENT:
					case DATE:
					case TIME:
					case OTHER_QUANTITY:
					case MONEY:
						annotation_id = annotation.first + "_" + annotation.second.getUniqueID();
						if (table.isMixed(j) && !Units_Measures.isNumeric(annotation.first)) {
							continue;
						}
						if (table.getCandidates(annotation_id) != null)
							cands = table.getCandidates(annotation_id);

						else {
							if (cands != null)
								table.CopyCandidates(i, j, annotation, cands);
							else {
								double score;
								// check if the header matches any Class
								cands = new HashSet<Candidate>();
								String class_name;
								if (header_cand_classes != null) {
									for (Candidate class_ : header_cand_classes) {
										class_name = class_.getSemanticTargetId();
										score = class_.getScore();
										if (units_measures.isDimensionless(class_name)) {
											cands.add(new CandidateQuantity(class_name + "(NULL,NULL)", TYPE.QUANTITY, score,
													annotation.first, class_name,"NULL","NULL"));
										} else if (units_measures.isTemporalClass(class_name)) {
											cands.add(new CandidateQuantity(class_name + "(Time,NULL)", TYPE.QUANTITY, score,
													annotation.first, class_name,"Time","NULL"));
										} else if (units_measures.isMonetryClass(class_name)) {
											cands.add(new CandidateQuantity(class_name + "(Currency,NULL)", TYPE.QUANTITY,
													score, annotation.first, class_name,"Currency","NULL"));
										} else if (units_measures.isDurationClass(class_name)) {
											cands.add(new CandidateQuantity(class_name + "(Time,NULL)", TYPE.QUANTITY, score,
													annotation.first, class_name,"Time","NULL"));
										}else if (units_measures.isPhysicalClass(class_name)) {
											List<Candidate> dimensions = units_measures.getDimensions(header_text);
											for(Candidate dimension : dimensions){
												cands.add(new CandidateQuantity(String.format("%s(%s,NULL)", class_name, dimension.getSemanticTargetId()),
														TYPE.QUANTITY, dimension.getScore(),
														annotation.first, class_name,dimension.getSemanticTargetId(),"NULL"));
											}
											
										}
									}
								}
								// add the matching units of the title
								if (units_measures.getAliases().containsKey(header_text)) {
									cands.addAll(Units_Measures.getCandidatesScores(header_text,
											(Set<String>) units_measures.getAliases().get(header_text), null,annotation.first, annotation.first));
								}

								if (cands.size() == 0) {
									// check regex for years /
									matcher = Units_Measures.year_regex.matcher(annotation.first);
									if (matcher.matches()) {
										cand = new CandidateQuantity("Temporal(Time,Calendar_year)", TYPE.QUANTITY, 1.0,
												annotation.first, "Temporal","Time","Calendar_year");
										cands.add(cand);
									}
								}
								if (cands.size() == 0 && Units_Measures.isNumber(annotation.first)
										&& table.getTitle_s() != null && !table.getTitle_s().isEmpty()) {
									table_title = Units_Measures.stripModifiers(header_text);
									// only do it for pure numbers
									table_title = table.getTitle_s().trim();
									for (String word : table_title.split("[\\s.,()\\|\\?:!&\"]")) {
										if (units_measures.getAliases().containsKey(word.toLowerCase())) {
											cands.addAll(Units_Measures.getCandidatesScores(word,
													(Set<String>) units_measures.getAliases().get(word.toLowerCase()), null,annotation.first, annotation.first));
										}
									}
									if (cands.size() == 0) {
										// search a dimensionless class name
										title_classes = Units_Measures.getClassesOfTableTitle(table_title);
										for (Candidate class_cand : title_classes) {
											class_name = class_cand.getSemanticTargetId();
											score = class_cand.getScore();
											if (units_measures.isDimensionless(class_name)) {
												cands.add(new CandidateQuantity(class_name + "(NULL,NULL)", TYPE.QUANTITY,
														score, 
														annotation.first, class_name,"NULL","NULL"));
											} else if (units_measures.isTemporalClass(class_name)) {
												cands.add(new CandidateQuantity(class_name + "(Time,NULL)", TYPE.QUANTITY,
														score, annotation.first, class_name,"Time","NULL"));
											} else if (units_measures.isMonetryClass(class_name)) {
												cands.add(new CandidateQuantity(class_name + "(Currency,NULL)", TYPE.QUANTITY,
														score,annotation.first, class_name,"Currency","NULL"));
											} else if (units_measures.isDurationClass(class_name)) {
												cands.add(new CandidateQuantity(class_name + "(Time,NULL)", TYPE.QUANTITY,
														score,annotation.first, class_name,"Time","NULL"));
											}											
										}
									}
								}
								if (cands.size() == 0) {// if still no matches
									cands = null;
								} else {
									table.CopyCandidates(i, j, annotation, cands);
								} // keep candidates to be used for the
									// following cells
							}
						}
						break;
					default:
						break;
					}
				}
			}
		}
	}

	private static List<String> expand(Set<Pair<String, Annotation>> header_annotations, Document_ document) {
		List<String> new_mentions = new LinkedList<String>();
		String expanded_mention;
		if (header_annotations == null)
			return new_mentions;
		for (Pair<String, Annotation> annotation : header_annotations) {
			expanded_mention = expand(annotation.first, document);
			new_mentions.add(expanded_mention);
		}
		return new_mentions;
	}

	public static void normalize(List<Candidate> candidates_a) {
		double sum_weights = 0;
		for (Candidate candidate : candidates_a) {
			sum_weights += candidate.getScore();
		}
		for (Candidate candidate : candidates_a) {
			candidate.normalizeScore(sum_weights);
		}
	}

	public static List<Candidate> removeDuplicates(List<Candidate> candidates) {
		if (candidates.size() <= 1)
			return candidates;
		Map<String, Candidate> recorder = new HashMap<String, Candidate>();
		List<Candidate> new_candidates = new LinkedList<Candidate>();
		Candidate old = null;
		for (Candidate candidate : candidates) {
			if (!recorder.containsKey(candidate.getSemanticTargetId())) {
				new_candidates.add(candidate);
				recorder.put(candidate.getSemanticTargetId(), candidate);
			} else {
				old = recorder.get(candidate.getSemanticTargetId());
				if (old.getScore() < candidate.getScore())
					old.setScore(candidate.getScore());
			}
		}
		return new_candidates;
	}

	// public static boolean contains(List<Candidate> candidates, String
	// semanticTargetId){
	// for (Candidate candidate : candidates) {
	// }
	public static List<Candidate> merge(List<Candidate> existing_candidates, List<Candidate> additional_candidates) {
		List<Candidate> new_candidates = new LinkedList<Candidate>();
		Map<String, Candidate> recorder = new HashMap<String, Candidate>();
		Candidate added_cand;
		for (Candidate candidate : existing_candidates) {
			if (!recorder.containsKey(candidate.getSemanticTargetId())) {
				recorder.put(candidate.getSemanticTargetId(), candidate);
				new_candidates.add(candidate);
			}
		}
		for (Candidate candidate : additional_candidates) {
			if (!recorder.containsKey(candidate.getSemanticTargetId())) {
				recorder.put(candidate.getSemanticTargetId(), candidate);
				new_candidates.add(candidate);
			} else {
				// pick the largest score out of them
				added_cand = recorder.get(candidate.getSemanticTargetId());
				if (added_cand.getScore() < candidate.getScore()) {
					added_cand.setScore(candidate.getScore());
				}
			}
		}
		return new_candidates;
	}

}
