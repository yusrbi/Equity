package graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;


import com.google.common.collect.Multimap;

import annotations.Annotation;
import resources.Resources;
import knowledgebase.Candidate;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.SparseVector;
import data.Document_;
import data.Table_;
import edu.stanford.nlp.util.Pair;

public class Solver {

	
	public static void doRandomWalk_fromCandidates(HashMap<String, Document_> documents, int table_id) {
		for (Document_ document : documents.values()) {
			doRandomWalk_fromCandidates(document, table_id);
		}

	}

	public static void doRandomWalk_fromMentions(HashMap<String, Document_> documents, int table_id) {

		for (Document_ document : documents.values()) {
			doRandomWalk_fromMentions(document, table_id);
		}

	}

	

	public static void doRandomWalk_fromMentions(Document_ document, int table_id) {
		Graph graph = document.getGraph();
		String node_id;
		Set<Candidate> node_cands;
		double[] results;
		int cand_id;
		double top_score = 0;
		Candidate winner = null;
		Multimap<String, Annotation> mentions = document.getMentions();
		Annotation annotation;
		if (mentions != null) {
			for (Entry<String, Annotation> mention : mentions.entries()) {
				annotation = mention.getValue();
				top_score = Integer.MIN_VALUE;
				winner = null;
				node_id = mention.getKey() + "_" + annotation.getUniqueID();
				node_cands = document.getCandidates(node_id);
				if (node_cands == null) {
					continue;
				}
				results = graph.startRWRFromMention(node_id);
				for (Candidate cand : node_cands) {
					cand_id = graph.getNodeId(cand.getSemanticTargetIdForGraph());
					if (top_score < results[cand_id]) {
						top_score = results[cand_id];
						winner = cand;
					}
				}
				if (winner != null) {
					document.setResult(node_id, winner, top_score);
				} else {
					System.out.println("NULL mention: " + mention.getKey());
				}
				

			}
		}
		// do the same for the table
		mentions = document.getTable(table_id).getMentions();
		if (mentions != null) {
			for (Entry<String, Annotation> mention : mentions.entries()) {
				annotation = mention.getValue();
				top_score = Integer.MIN_VALUE;
				winner = null;
				if (mention.getKey().equals("GM")) {
					winner = null;
				}
				node_id = mention.getKey() + "_" + annotation.getUniqueID();
				node_cands = document.getTable(table_id).getCandidates(node_id);
				if (node_cands == null) {
					continue;
				}
				results = graph.startRWRFromMention(node_id);
				for (Candidate cand : node_cands) {
					cand_id = graph.getNodeId(cand.getSemanticTargetIdForGraph());
					if (top_score < results[cand_id]) {
						top_score = results[cand_id];
						winner = cand;
					}
				}
				if (winner != null) {
					document.getTable(table_id).setResult(node_id, winner, top_score);
				} else {
					System.out.println("NULL mention: " + mention.getKey());
				}
				

			}
		}
	}

	public static void doRandomWalk_fromCandidates(Document_ document, int table_id) {
		Graph graph = document.getGraph();
		Multimap<String, Candidate> candidates_map = document.getAllCandidates();
		
		Set<Candidate> candidates_set;
		double max = 0.0, sum = 0.0, result = 0.0;
		Candidate winner = null;
		String sem_id;
		
		if (candidates_map != null) {
			for (String mention: candidates_map.keys()) {			
				winner = null;
				sum = 0.0;
				candidates_set = (Set<Candidate>) candidates_map.get(mention);
				max = Integer.MIN_VALUE;
				for (Candidate candidate : candidates_set) {
					sem_id = candidate.getSemanticTargetIdForGraph();
					result = graph.startRWRFromCandidates(sem_id);
					sum += result;
					if (result > max) {
						max = result;
						winner = candidate;
					}
				}
				if (winner != null) {
					document.setResult(mention, winner, max / sum);
				} else {
					System.out.println("NULL mention: " + mention);
				}
			}
		}
		// for the table

		// now do it for the tables candidates that was not resolved yet
		candidates_map = document.getTable(table_id).getCandidates();
		if (candidates_map != null) {
			for (String mention : candidates_map.keys()) {				
				max = Integer.MIN_VALUE;
				sum = 0.0;
				winner = null;
				candidates_set = (Set<Candidate>) candidates_map.get(mention);
				for (Candidate candidate : candidates_set) {
					result = graph.startRWRFromCandidates(candidate.getSemanticTargetIdForGraph());
					sum += result;
					if (result > max) {
						max = result;
						winner = candidate;
					}
				}
				if (winner != null) {
					document.getTable(table_id).setResult(mention, winner, max / sum);
				} else {
					System.out.println("NULL mention: " + mention);
				}
			}
		}

	}

	public static void doRandomWalk_fromCandidates_cohen_plus(Document_ document, int table_id) {
		Graph graph = document.getGraph();
		Multimap<String, Candidate> candidates_map = document.getAllCandidates();

		Set<Candidate> candidates_set;
		double max = 0.0, sum = 0.0, max_infinity_counts = 0;
		Pair<Double, Integer> result;
		Candidate winner = null;
		String sem_id;
		Vector d;
		if (candidates_map != null) {
			for (String mention : candidates_map.keys()) {
				
				winner = null;
				sum = 0.0;
				candidates_set = (Set<Candidate>) candidates_map.get(mention);
				d = get_AllCandidatesTeleportVector(candidates_set, graph);
				max = Double.MIN_VALUE;
				max_infinity_counts = Integer.MAX_VALUE;
				for (Candidate candidate : candidates_set) {
					sem_id = candidate.getSemanticTargetIdForGraph();
					result = graph.startRWRFromCandidatesDirected(sem_id, d);
					sum += result.first;

					if (result.second < max_infinity_counts) {// prefer the node
																// connected to
																// most of the
																// anchors over
																// all the other
																// nodes
						max = result.first;
						max_infinity_counts = result.second;
						winner = candidate;
					} else if (result.second == max_infinity_counts && result.first > max) { // check
																								// the
																								// belief
																								// value
																								// if
																								// the
																								// number
																								// of
																								// -infinity
																								// are
																								// similar
						max = result.first;
						max_infinity_counts = result.second;
						winner = candidate;
					} // other wise the new node has n -infinity more than the
						// current max, so it is not better
				}
				if (winner != null)
					document.setResult(mention, winner, max / sum);
			}
		}
		// for the table

		// now do it for the tables candidates that was not resolved yet
		candidates_map = document.getTable(table_id).getCandidates();
		if (candidates_map != null) {
			for (String mention : candidates_map.keys()) {
				
				sum = 0.0;
				winner = null;
				candidates_set = (Set<Candidate>) candidates_map.get(mention);
				d = get_AllCandidatesTeleportVector(candidates_set, graph);
				max = Double.MIN_VALUE;
				max_infinity_counts = Integer.MAX_VALUE;
				for (Candidate candidate : candidates_set) {
					result = graph.startRWRFromCandidatesDirected(candidate.getSemanticTargetIdForGraph(), d);
					sum += result.first;
					if (result.second < max_infinity_counts) {// prefer the node
																// connected to
																// most
																// of the
																// anchors
																// over all the
																// other nodes
						max = result.first;
						max_infinity_counts = result.second;
						winner = candidate;
					} else if (result.second == max_infinity_counts && result.first > max) { // check
																								// the
																								// belief
																								// value
																								// if
																								// the
																								// number
																								// of
																								// -infinity
																								// are
																								// similar
						max = result.first;
						max_infinity_counts = result.second;
						winner = candidate;
					} // other wise the new node has n -infinity more than the
						// current max, so it is not better
				}
				if (winner != null)
					document.getTable(table_id).setResult(mention, winner, max / sum);
			}
		}
	}

	public static void doRandomWalk_fromCandidates_cohen(Document_ document, int table_id) {
		Graph graph = document.getGraph();
		Multimap<String, Candidate> candidates_map = document.getAllCandidates();
		
		Set<Candidate> candidates_ls;
		double max = 0.0, sum = 0.0, max_infinity_counts = 0;
		Pair<Double, Integer> result;
		Candidate winner = null;
		String sem_id;
		Vector d;
		if (candidates_map != null) {
			for (String mention : candidates_map.keys()) {
			
				winner = null;
				sum = 0.0;
				candidates_ls = (Set<Candidate>) candidates_map.get(mention);
				d = get_AllCandidatesTeleportVector(candidates_ls, graph);
				max = Double.MIN_VALUE;
				max_infinity_counts = Integer.MAX_VALUE;
				for (Candidate candidate : candidates_ls) {
					sem_id = candidate.getSemanticTargetIdForGraph();
					result = graph.startRWRFromCandidatesDirected(sem_id, d);
					sum += result.first;
					if (result.first > max) {
						max = result.first;
						winner = candidate;
					}

				}
				if (winner != null)
					document.setResult(mention, winner, max / sum);
			}
		}
		// for the table

		// now do it for the tables candidates that was not resolved yet
		candidates_map = document.getTable(table_id).getCandidates();
		if (candidates_map != null) {
			for (String mention : candidates_map.keys()) {
			
				sum = 0.0;
				winner = null;
				candidates_ls = (Set<Candidate>) candidates_map.get(mention);
				d = get_AllCandidatesTeleportVector(candidates_ls, graph);
				max = Double.MIN_VALUE;
				max_infinity_counts = Integer.MAX_VALUE;
				for (Candidate candidate : candidates_ls) {
					result = graph.startRWRFromCandidatesDirected(candidate.getSemanticTargetIdForGraph(), d);
					sum += result.first;
					if (result.second < max_infinity_counts) {// prefer the node
																// connected to
																// most
																// of the
																// anchors
																// over all the
																// other nodes
						max = result.first;
						max_infinity_counts = result.second;
						winner = candidate;
					} else if (result.second == max_infinity_counts && result.first > max) { // check
																								// the
																								// belief
																								// value
																								// if
																								// the
																								// number
																								// of
																								// -infinity
																								// are
																								// similar
						max = result.first;
						max_infinity_counts = result.second;
						winner = candidate;
					} // other wise the new node has n -infinity more than the
						// current max, so it is not better
				}
				if (winner != null)
					document.getTable(table_id).setResult(mention, winner, max / sum);
			}
		}
	}

	private static Vector get_AllCandidatesTeleportVector(Set<Candidate> candidates_set, Graph graph) {
		Vector d = new SparseVector(graph.getNNodes());

		for (Candidate candidate : candidates_set) {
			d.set(graph.getNodeId(candidate.getSemanticTargetIdForGraph()), 1);
		}
		return d;
	}

	
	public static void writeResults(HashMap<String, Document_> documents, String path, int table_id)
			throws IOException {
		for (Document_ document : documents.values()) {
			writeResults(document, path, table_id);
		}

	}

	public static void writeResults(Document_ document, String output_path, int table_id) throws IOException {
		/*
		 * if(document.getResults() == null ||
		 * document.getTable(table_id).getResult() == null) return;
		 */
		Table_ table = document.getTable(table_id);
		File template_file = new File(Resources.getResources().getHtml_template_path());
		String template = FileUtils.readFileToString(template_file, "utf-8");
		// add the document title and url
		String table_header, table_body;
		template = template.replace("#DOC_URL#", document.getId());
		template = template.replace("#DOC_TITLE#", document.getTitle());
		template = template.replace("#TABLE_ID#", String.valueOf(table_id));
		template = template.replace("#CONTENT_PLACE_HOLDER#", document.getAnnotatedContentsWithResults2());
		table_header = table.getAnnotatedHeaderWithResults().toString();
		table_body = table.getAnnotatedBodyWithResults().toString();
		template = template.replace("#TABLE_HEADER#", table_header);
		template = template.replace("#TABLE_BODY#", table_body);
		table.writeHTMLResultstoDB(String.format(
				"<table class=\"table table-striped table-bordered table-hover table-condensed\"> <thead> %s</thead> <tbody> %s </tbody></table>",
				table_header, table_body));

		String out_file = document.getFile_name();
		out_file = out_file.substring(0, out_file.indexOf("."));
		out_file = output_path + out_file + "_" + String.valueOf(table_id) + document.getExperiement_id() + ".html";
		FileUtils.writeStringToFile(new File(out_file), template, "utf-8");
	}

	public static boolean already_done(String file, String output_path) {
		String out_file = file.substring(0, file.indexOf("."));
		out_file = output_path + out_file + "_0.html";
		File result_file = new File(out_file);
		return result_file.exists();
	}

}
