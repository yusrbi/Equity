package graph;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import utils.DatabaseAccess;
import annotations.Annotation;
import knowledgebase.Candidate;
import knowledgebase.WeightsCalculator;
import data.Document_;
import data.Table_;
import edu.stanford.nlp.util.Pair;
import executer.RunTimeAnalysis;
import graph.Edge.TYPE;

public class GraphBuilder {

	private static Double cand_rel_sim_cut_off =0.00;

	private static boolean isCohenNormalization = false;

	public static void buildGraph(HashMap<String, Document_> documents, int table_indx, boolean full_graph,
			boolean general_relatedness, boolean initDB) throws SQLException {
		// TODO Auto-generated method stub
		for (Document_ document : documents.values()) {
			buildGraph(document, table_indx, full_graph, general_relatedness, initDB);
		}
	}

	public static Graph buildGraph(Document_ document, int tableID, boolean full, boolean general_relatedness,
			boolean initDB) throws SQLException {
		DatabaseAccess data_access = DatabaseAccess.getDatabaseAccess();
		Table_ table = document.getTable(tableID);
		Multimap<String, Candidate> all_candidates = HashMultimap.create();
		if (table.getCandidates() != null)
			all_candidates.putAll(table.getCandidates());
		if (document.getAllCandidates() != null)
			all_candidates.putAll(document.getAllCandidates());
		
		if (initDB) {
			data_access.loadDBFor(document.getTable(tableID), all_candidates, general_relatedness);
			RunTimeAnalysis.mile_stone_end = System.nanoTime();
			RunTimeAnalysis.loading_db_time += RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
			RunTimeAnalysis.mile_stone_start = System.nanoTime();
		}
		if (!full) {
			return buildPartialGraph(document, table, general_relatedness, initDB);
		}
		
		// init variables	
		Multimap<String, Annotation> doc_mentions = document.getMentions();
		Multimap<String, Annotation> table_mentions = table.getMentions();
		Multimap<Pair<Integer, Integer>, Pair<String, Annotation>> annotations_map = table.getInverted_annotations();
		Set<Pair<String, Annotation>> header_annotations, cell_annotations, other_cell_annotaions;
		Set<Candidate> candidates;
		Graph graph = new AdjListsGraph(document.mentionCount() * 2);
		double similarity = 0.0;
		

		// add document mentions
		
		
		if (doc_mentions != null) {
			for (String mention : doc_mentions.keySet()) {
				for (Annotation annotation : doc_mentions.get(mention)) {
					candidates = document.getCandidates(mention + "_" + annotation.getUniqueID());
					graph.addMentionNode(mention + "_" + annotation.getUniqueID(), candidates);
				}
			}
		}
		// add table mentions
		for (String mention : table_mentions.keySet()) {
			for (Annotation annotation : table_mentions.get(mention)) {
				candidates = table.getCandidates(mention + "_" + annotation.getUniqueID());

				graph.addMentionNode(mention + "_" + annotation.getUniqueID(), candidates);
			}
		}

//		if (doc_mentions != null && table_mentions != null) {
//			for (String doc_mention : doc_mentions.keySet()) {
//				for (String table_mention : table_mentions.keySet()) {
//					similarity = StringUtils.getJaroWinklerDistance(doc_mention, table_mention);
//					if (similarity > 0.9)
//						addSameSurfaceEdges(table_mention, doc_mention, table_mentions.get(table_mention), 
//							doc_mentions.get(doc_mention), similarity, graph);
//					
//
//				}
//			}
//		}
		// check text table mentions similarties

		// Edges between same mentions from text and document are not needed as
		// the 2 (or more) mentions will be represented with the same node
		// CYCLES-PREVENTION The edges between same row entities are not added
		// for now
		// CYCLES-PREVENTION the edges between the same column entities are not
		// added for now
		// CYCLES-PREVENTION the edges between 2 semantic targets in the context
		// are not added for now
		// add edges from header to the column cells
		// weight from category matching and co-occurrences
		// go by the inverted list of mentions in the table


//		for (int j = 0; j < table.getNcol(); j++) {
//			header_annotations = (Set<Pair<String, Annotation>>) annotations_map.get(new Pair<Integer, Integer>(0, j));
//			for (int i = 1; i < table.getNrow(); i++) {
//				cell_annotations = (Set<Pair<String, Annotation>>) annotations_map.get(new Pair<Integer, Integer>(i, j));
//				processHeaderToCellEdges(header_annotations, cell_annotations, table, graph, general_relatedness,
//						false);
//			}
//		}

		if (full) { // in case we need to build the full graph and not the
					// cyclic graph
			
			// here we add links between all candidates in doc and table
			double sim = 0.0;
			String mention1, mention2;
			Set<String> done = new LinkedHashSet<String>(document.mentionCount());
			for (String mention_key1 : all_candidates.keys()) {
				for (String mention_key2 : all_candidates.keys()) {
					if(mention_key1.equals(mention_key2))
						continue;
					if (done.contains(mention_key1+ mention_key2)
							|| done.contains(mention_key2+ mention_key1))
						continue;				
					
					mention1 = mention_key1.split("_")[0];
					mention2 = mention_key2.split("_")[0];			
					similarity = StringUtils.getJaroWinklerDistance(mention1, mention2);
					if (similarity > 0.9)
						addSameSurfaceEdges(mention_key1, mention_key2,similarity, graph);
					
					done.add(mention_key1 + mention_key2);
					
					if (mention1.equals(mention2))
						continue;
					
					
					for (Candidate cand : all_candidates.get(mention_key1)) {
						// prepare candidates
						for (Candidate cand2 :  all_candidates.get(mention_key2)) {

							if (done.contains(cand.getSemanticTargetIdForGraph() + cand2.getSemanticTargetIdForGraph())
									|| done.contains(
											cand2.getSemanticTargetIdForGraph() + cand.getSemanticTargetIdForGraph()))
								continue;
							
							if (cand.getSemanticTargetId().equals(cand2.getSemanticTargetId()))
								sim = 1;
							else
								sim = WeightsCalculator.getCandCandSimilarity(cand, cand2);
							if (sim > cand_rel_sim_cut_off)
								graph.addEdge(cand.getSemanticTargetIdForGraph(), cand2.getSemanticTargetIdForGraph(),
										sim, Edge.TYPE.CANDIDATE_CANDIDATE);
							done.add(cand.getSemanticTargetIdForGraph() + cand2.getSemanticTargetIdForGraph());

						}
					}
					
				}
			}
						
			// connect mentions based on the table structure
			
			for (int j = 0; j < table.getNcol(); j++) {
				header_annotations = (Set<Pair<String, Annotation>>) annotations_map
						.get(new Pair<Integer, Integer>(0, j));

				for (int i = 1; i < table.getNrow(); i++) {
					cell_annotations = (Set<Pair<String, Annotation>>) annotations_map
							.get(new Pair<Integer, Integer>(i, j));
					//  header-cell here
					processHeaderToCellEdges(header_annotations, cell_annotations, table, graph, general_relatedness,
							false);
					for (int k = j + 1; k < table.getNcol(); k++) {
						// same row
						other_cell_annotaions = (Set<Pair<String, Annotation>>) annotations_map
								.get(new Pair<Integer, Integer>(i, k));
						processCellAnnotationsOnSameRow(cell_annotations, other_cell_annotaions, table, graph,
								general_relatedness, false);
					}
					
					for (int k = i + 1; k < table.getNrow(); k++) {
						// same column
						other_cell_annotaions = (Set<Pair<String, Annotation>>) annotations_map
								.get(new Pair<Integer, Integer>(k, j));
						processCellAnnotationsOnSameColumn(cell_annotations, other_cell_annotaions, table, graph,
								general_relatedness, false);
					}
				}
			}
			// this part add cand cand edges based on table localities
//			// add edges for the mentions/semantic items in the same row/column
//			for (int i = 1; i < table.getNrow(); i++) {
//				for (int j = 0; j < table.getNcol(); j++) {
//					cell_annotations = annotations_map.get(new Pair<Integer, Integer>(i, j));
//					// now consider all the other columns in the same rows
//					for (int k = j + 1; k < table.getNcol(); k++) {
//						other_cell_annotaions = annotations_map.get(new Pair<Integer, Integer>(i, k));
//						processCandidateCandidate(cell_annotations, other_cell_annotaions, table, graph,
//								general_relatedness);
//					}
//					// now consider all the other rows in the same column
//					for (int k = i + 1; k < table.getNrow(); k++) {
//						other_cell_annotaions = annotations_map.get(new Pair<Integer, Integer>(k, j));
//						processCandidateCandidate(cell_annotations, other_cell_annotaions, table, graph,
//								general_relatedness);
//					}
//				}
//			}
		}
		// I think we should not normalize , it should be left as is and the
		// hyper parameters should know the difference
		
		graph.normalizeEdgeWeights();//by type
		graph.adjastEdgeWeightsUsingHyperparams();
		graph.renormalize();
//		graph.normalizeEdgeWeightsByConstant();
		document.setGraph(graph);
//		try {
//		//	document.saveGraph("_full", false);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return graph;
	}

	private static void addSameSurfaceEdges(String mention1, String mention2, Collection<Annotation> annotations1,
			Collection<Annotation> annotations2, double similarity, Graph graph) {
		for (Annotation annotation1 : annotations1) {
			for (Annotation annotation2 : annotations2) {
				graph.addEdge(mention1 + "_" + annotation1.getUniqueID(), mention2 + "_" + annotation2.getUniqueID(),
						similarity, Edge.TYPE.SIMILAR_SURFACE);
			}
		}

	}
	private static void addSameSurfaceEdges(String mention1_key, String mention2_key, double similarity, Graph graph) {
		graph.addEdge(mention1_key, mention2_key,
						similarity, Edge.TYPE.SIMILAR_SURFACE);
			

	}
//TODO cand-cand on table locality ?
//	private static void processCandidateCandidate(List<Pair<String, Annotation>> cell_annotations,
//			List<Pair<String, Annotation>> other_cell_annotaions, Table_ table, Graph graph,
//			boolean general_relatedness) {
//		if (other_cell_annotaions == null || cell_annotations == null) {
//			return;
//		}
//		Set<Candidate> cell_candidates, other_cell_candidates;
//		String cell_id, other_id;
//		double sim = 0.0;
//		for (Pair<String, Annotation> cell_annotaion : cell_annotations) {
//			cell_id = cell_annotaion.first + "_" + cell_annotaion.second.getUniqueID();
//			cell_candidates = table.getCandidates(cell_id);
//			for (Pair<String, Annotation> other_cell_annotaion : other_cell_annotaions) {
//				other_id = other_cell_annotaion.first + "_" + other_cell_annotaion.second.getUniqueID();
//				// check for all the candidates
//				other_cell_candidates = table.getCandidates(other_id);
//				if (cell_candidates == null || other_cell_candidates == null) {
//					continue;
//				}
//				for (Candidate cell_candidate : cell_candidates) {
//					for (Candidate other_cell_candidate : other_cell_candidates) {
//						sim = WeightsCalculator.getCandCandSimilarity(cell_candidate, other_cell_candidate);
//						if (sim > cand_rel_sim_cut_off){
//						graph.addEdge(cell_candidate.getSemanticTargetIdForGraph(),
//								other_cell_candidate.getSemanticTargetIdForGraph(), sim, Edge.TYPE.CANDIDATE_CANDIDATE);
//						
//						}
//					}
//				}
//			}
//		}
//	}
//	private static void processCandidateCandidate_reduced(List<Pair<String, Annotation>> cell_annotations,
//			List<Pair<String, Annotation>> other_cell_annotaions, Table_ table, Graph compactGraph,
//			boolean general_relatedness) {
//		if (other_cell_annotaions == null || cell_annotations == null) {
//			return;
//		}
//		Set<Candidate> cell_candidates, other_cell_candidates;
//		String cell_id, other_id;
//		double sim = 0.0, aggregated_sim=0.0;
//		for (Pair<String, Annotation> cell_annotaion : cell_annotations) {
//			cell_id = cell_annotaion.first + "_" + cell_annotaion.second.getUniqueID();
//			cell_candidates = table.getCandidates(cell_id);
//			for (Pair<String, Annotation> other_cell_annotaion : other_cell_annotaions) {
//				other_id = other_cell_annotaion.first + "_" + other_cell_annotaion.second.getUniqueID();
//				// check for all the candidates
//				other_cell_candidates = table.getCandidates(other_id);
//				if (cell_candidates == null || other_cell_candidates == null) {
//					continue;
//				}
//				for (Candidate cell_candidate : cell_candidates) {
//					for (Candidate other_cell_candidate : other_cell_candidates) {
//						sim = WeightsCalculator.getCandCandSimilarity(cell_candidate, other_cell_candidate);
//						if (sim > cand_rel_sim_cut_off){
//							compactGraph.addCandidateCandidateEdgeWeight(cell_candidate.getSemanticTargetIdForGraph(),
//									other_cell_candidate.getSemanticTargetIdForGraph(), sim);
//							aggregated_sim += sim * cell_candidate.getScore() * other_cell_candidate.getScore();
//						
//						}
//					}
//				}
//				if (aggregated_sim > 0.0) {
//					compactGraph.addPotentialEdge(cell_id + "_cand", other_id + "_cand", aggregated_sim,
//							TYPE.CANDIDATE_CANDIDATE);
//					aggregated_sim = 0.0;
//				}
//			}
//		}
//	}
	private static Graph buildPartialGraph(Document_ document, Table_ table, boolean general_relatedness,
			boolean init) {
		Graph compact_graph = new AdjListsGraph((document.mentionCount() + table.getMentionsCount()) * 2);
		// add mentions to the graph and aggregate edge weights
		// add document mentions
		Multimap<String, Annotation> doc_mentions = document.getMentions();
		Multimap<String, Annotation> table_mentions = table.getMentions();
		double similarity = 0.0;
		Multimap<Pair<Integer, Integer>, Pair<String, Annotation>> annotations_map = table.getInverted_annotations();
		Set<Pair<String, Annotation>> header_annotations, cell_annotations, other_cell_annotaions;
		double sim = 0.0;
		double aggregated_sim = 0;
		String mention1, mention2;
		Multimap<String, Candidate> all_candidates = HashMultimap.create();
		if(table.getCandidates()!= null)
			all_candidates.putAll(table.getCandidates());
		if (document.getAllCandidates() != null)
			all_candidates.putAll(document.getAllCandidates());
		Set<String> done = new LinkedHashSet<String>(document.mentionCount());		
		Set<Candidate> candidates;
		
		
		if (doc_mentions != null) {
			for (String mention : doc_mentions.keySet()) {
				for (Annotation annotation : doc_mentions.get(mention)) {
					candidates = document.getCandidates(mention + "_" + annotation.getUniqueID());
					compact_graph.addMentionNodeAggregateCandidates(mention + "_" + annotation.getUniqueID(),
							candidates);
				}
			}
		}

		// add table mentions
		if (table_mentions != null) {
			for (String mention : table_mentions.keySet()) {
				for (Annotation annotation : table_mentions.get(mention)) {
					candidates = table.getCandidates(mention + "_" + annotation.getUniqueID());

					compact_graph.addMentionNodeAggregateCandidates(mention + "_" + annotation.getUniqueID(),
							candidates);
				}
			}
		}
		
//		if (table_mentions != null) {
//			// similar surface in table
//			for (String table_mention1 : table_mentions.keySet()) {
//				for (String table_mention2 : table_mentions.keySet()) {
//					similarity = StringUtils.getJaroWinklerDistance(table_mention1, table_mention2);
//					if (similarity > 0.9)
//						addSameSurfacePotentialEdges(table_mention1, table_mention2, table_mentions.get(table_mention1),
//								table_mentions.get(table_mention2), similarity, compact_graph);
//
//				}
//			}			
//			// similar surface between context and table
//			if (doc_mentions != null) {
//				for (String doc_mention : doc_mentions.keySet()) {
//					for (String table_mention : table_mentions.keySet()) {
//						similarity = StringUtils.getJaroWinklerDistance(doc_mention, table_mention);
//						if (similarity > 0.9)
//							addSameSurfacePotentialEdges(table_mention, doc_mention, table_mentions.get(table_mention),
//									doc_mentions.get(doc_mention), similarity, compact_graph);
//
//					}
//				}
//			}
//
//		}
//		
		
		if (annotations_map != null) {
//			for (int j = 0; j < table.getNcol(); j++) {
//				header_annotations = (Set<Pair<String, Annotation>>) annotations_map.get(new Pair<Integer, Integer>(0, j));
//				for (int i = 1; i < table.getNrow(); i++) {
//					cell_annotations = (Set<Pair<String, Annotation>>) annotations_map.get(new Pair<Integer, Integer>(i, j));
//					processHeaderToCellEdges(header_annotations, cell_annotations, table, compact_graph,
//							general_relatedness, true);
//				}
//			}

			// mention-mention based on table structure
			// connect mentions based on the table structure
			for (int j = 0; j < table.getNcol(); j++) {
				for (int i = 1; i < table.getNrow(); i++) {
					header_annotations = (Set<Pair<String, Annotation>>) annotations_map
							.get(new Pair<Integer, Integer>(0, j));

					cell_annotations = (Set<Pair<String, Annotation>>) annotations_map
							.get(new Pair<Integer, Integer>(i, j));
					processHeaderToCellEdges(header_annotations, cell_annotations, table, compact_graph,
							general_relatedness, true);
					for (int k = j + 1; k < table.getNcol(); k++) {
						// same row
						other_cell_annotaions = (Set<Pair<String, Annotation>>) annotations_map
								.get(new Pair<Integer, Integer>(i, k));
						processCellAnnotationsOnSameRow(cell_annotations, other_cell_annotaions, table, compact_graph,
								general_relatedness, true);
					} //
						// TODO header-cell merge here
					for (int k = i + 1; k < table.getNrow(); k++) {
						// same column
						other_cell_annotaions = (Set<Pair<String, Annotation>>) annotations_map
								.get(new Pair<Integer, Integer>(k, j));
						processCellAnnotationsOnSameColumn(cell_annotations, other_cell_annotaions, table,
								compact_graph, general_relatedness, true);
					}
				}
			}
		}
		
		// this part add cand cand edges based on table localities
		// add edges for the mentions/semantic items in the same row/column
//		for (int i = 1; i < table.getNrow(); i++) {
//			for (int j = 0; j < table.getNcol(); j++) {
//				cell_annotations = annotations_map.get(new Pair<Integer, Integer>(i, j));
//				// now consider all the other columns in the same rows
//				for (int k = j + 1; k < table.getNcol(); k++) {
//					other_cell_annotaions = annotations_map.get(new Pair<Integer, Integer>(i, k));
//					processCandidateCandidate_reduced(cell_annotations, other_cell_annotaions, table, compactGraph,
//							general_relatedness);
//				}
//				// now consider all the other rows in the same column
//				for (int k = i + 1; k < table.getNrow(); k++) {
//					other_cell_annotaions = annotations_map.get(new Pair<Integer, Integer>(k, j));
//					processCandidateCandidate_reduced(cell_annotations, other_cell_annotaions, table, compactGraph,
//							general_relatedness);
//				}
//			}
//		}
		
		for (String mention_key1 : all_candidates.keys()) {
			for (String mention_key2: all_candidates.keys()) {
				if(mention_key1.equals(mention_key2))
					continue;
				if (done.contains(mention_key1+ mention_key2) || done.contains(mention_key2+ mention_key1))
					continue;
				mention1 = mention_key1.split("_")[0];
				mention2 = mention_key2.split("_")[0];
				//TODO check mention same surface form
				similarity = StringUtils.getJaroWinklerDistance(mention1, mention2);
				if (similarity > 0.9)
					addSameSurfacePotentialEdges(mention_key1, mention_key2,similarity, compact_graph);
				
				done.add(mention_key1 + mention_key2);
				if (mention1.equals(mention2))
					continue;
				
				
				
				for (Candidate cand : all_candidates.get(mention_key1)) {
					// prepare candidates
					for (Candidate cand2 : all_candidates.get(mention_key2)) {
						if (done.contains(cand.getSemanticTargetIdForGraph() + cand2.getSemanticTargetIdForGraph())
								|| done.contains(
										cand2.getSemanticTargetIdForGraph() + cand.getSemanticTargetIdForGraph()))
							continue;
						
						if (cand.getSemanticTargetId().equals(cand2.getSemanticTargetId()))
							sim = 1;
						else
							sim = WeightsCalculator.getCandCandSimilarity(cand, cand2);
						if (sim > cand_rel_sim_cut_off) {
							compact_graph.addCandidateCandidateEdgeWeight(cand.getSemanticTargetIdForGraph(),
									cand2.getSemanticTargetIdForGraph(), sim);
							aggregated_sim += sim * cand.getScore() * cand2.getScore();
						}
						done.add(cand.getSemanticTargetIdForGraph() + cand2.getSemanticTargetIdForGraph());
					}
				}
				// add an aggregated edge
				if (aggregated_sim > 0.0) {
					compact_graph.addPotentialEdge(mention_key1 + "_cand", mention_key2+ "_cand", aggregated_sim,
							TYPE.CANDIDATE_CANDIDATE);
					aggregated_sim = 0.0;
				}
				
			}
		}
		
//		try {
			document.setGraph(compact_graph);
			// if (init)
		//	document.saveGraph("cyclic_MRF", true);
			compact_graph.removeCycles();
			// if (init)
		//	document.saveGraph("_acyclic_MRF", false);

			//compact_graph.addAnchorNodes();
//			// if (init)
		//	document.saveGraph("acyclic_MRF_added_Anchor_NODES", false);

			compact_graph.expandEdges();

			// I think we should not normalize , it should be left as is and the
			// hyper parameters should know the difference
			
			// the
			// hyperparams,
			// so each node
			// will sum to
//			// one at most
			if (isCohenNormalization)
				compact_graph.normalizeEdgeWeightsByConstant();
			else
				compact_graph.normalizeEdgeWeights(); // normalize based on the
//														// edge
//														// type
//														// weight
			compact_graph.adjastEdgeWeightsUsingHyperparams(); // rescale using
			compact_graph.renormalize();//each row sums to one

			// if (init)
			//document.saveGraph("_reduced", false);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return compact_graph;

	}
	private static void addSameSurfacePotentialEdges(String mention1, String mention2, Collection<Annotation> annotations1,
			Collection<Annotation> annotations2, double similarity, Graph compact_graph) {
		for (Annotation annotation1 : annotations1) {
			for (Annotation annotation2 : annotations2) {
				compact_graph.addPotentialEdge(mention1 + "_" + annotation1.getUniqueID(),
						mention2 + "_" + annotation2.getUniqueID(), similarity,
						Edge.TYPE.SIMILAR_SURFACE);
			}
		}

	}
	
	private static void addSameSurfacePotentialEdges(String mention_key1, String mention_key2,
			double similarity, Graph compact_graph) {
		compact_graph.addPotentialEdge(mention_key1,mention_key2, similarity, Edge.TYPE.SIMILAR_SURFACE);

	}
	public static void setCohenNormalization(boolean isCohenNormalization) {
		GraphBuilder.isCohenNormalization = isCohenNormalization;
	}

	private static void processCellAnnotationsOnSameColumn(Set<Pair<String, Annotation>> cell_annotations,
			Set<Pair<String, Annotation>> other_cell_annotaions, Table_ table, Graph graph,
			boolean general_relatedness, boolean addPotentialEdge) {
		if (other_cell_annotaions == null || cell_annotations == null) {
			return;
		}
		String cell_id, other_id;
		double sim = 0.0;
		for (Pair<String, Annotation> cell_annotaion : cell_annotations) {
			cell_id = cell_annotaion.first + "_" + cell_annotaion.second.getUniqueID();
			for (Pair<String, Annotation> other_cell_annotaion : other_cell_annotaions) {
				// check for all the candidates
				other_id = other_cell_annotaion.first + "_" + other_cell_annotaion.second.getUniqueID();
				// add an edge between the 2 mentions first --> full graph,
				// connected
				// from the table size
				sim = WeightsCalculator.getSameColumnMentionSimilarity(cell_annotaion.first,
						other_cell_annotaion.first);
				if (sim <= 0)
					continue;
				if (addPotentialEdge)
					graph.addPotentialEdge(cell_id, other_id, sim, Edge.TYPE.SAME_COLUMN);
				else
					graph.addEdge(cell_id, other_id, sim, Edge.TYPE.SAME_COLUMN);
			}
		}
	}

	private static void processCellAnnotationsOnSameRow(Set<Pair<String, Annotation>> cell_annotations,
			Set<Pair<String, Annotation>> other_cell_annotaions, Table_ table, Graph graph,
			boolean general_relatedness, boolean addPotentialEdge) {
		if (other_cell_annotaions == null || cell_annotations == null) {
			return;
		}

		String cell_id, other_id;
		double sim = 0.0;
		for (Pair<String, Annotation> cell_annotaion : cell_annotations) {
			cell_id = cell_annotaion.first + "_" + cell_annotaion.second.getUniqueID();
			for (Pair<String, Annotation> other_cell_annotaion : other_cell_annotaions) {
				other_id = other_cell_annotaion.first + "_" + other_cell_annotaion.second.getUniqueID();
				// check for all the candidates
				// add an edge between the 2 mentions first --> full graph,
				// connected
				// from the table size
				sim = WeightsCalculator.getSameRowMentionsSimilarity(cell_annotaion.first, other_cell_annotaion.first);
				if (sim <= 0)
					continue;
				if (addPotentialEdge)
					graph.addPotentialEdge(cell_id, other_id, sim, Edge.TYPE.SAME_ROW);
				else
					graph.addEdge(cell_id, other_id, sim, Edge.TYPE.SAME_ROW);
			}

		}
	}

	private static void processHeaderToCellEdges(Set<Pair<String, Annotation>> header_annotations,
			Set<Pair<String, Annotation>> cell_annotations, Table_ table, Graph graph, boolean general_relatedness,
			boolean addPotentialEdge) {
		if (header_annotations == null || cell_annotations == null) {
			return;
		}
		String header_id, cell_id;
		double sim;
		for (Pair<String, Annotation> header_annotaion : header_annotations) {
			header_id = header_annotaion.first + "_" + header_annotaion.second.getUniqueID();
			for (Pair<String, Annotation> cell_annotaion : cell_annotations) {
				cell_id = cell_annotaion.first + "_" + cell_annotaion.second.getUniqueID();
				// check for all the candidates
				sim = WeightsCalculator.getHeaderCellMentionsSimilarity(header_annotaion.first, cell_annotaion.first);
				if (sim <= 0)
					continue;
				if (addPotentialEdge)
					graph.addPotentialEdge(header_id, cell_id, sim, Edge.TYPE.HEADER_CELL);
				else
					graph.addEdge(header_id, cell_id, sim, Edge.TYPE.HEADER_CELL);
				/*
				 * if (header_candidates == null || cell_candidates == null) {
				 * continue; } for (Candidate header_candidate :
				 * header_candidates) { for (Candidate cell_candidate :
				 * cell_candidates) { sim =
				 * WeightsCalculator.getHeaderCellCandidatesSimilarity(
				 * header_candidate, cell_candidate, general_relatedness); if
				 * (sim > cand_rel_sim_cut_off)
				 * graph.addEdge(header_candidate.getSemanticTargetIdForGraph(),
				 * cell_candidate.getSemanticTargetIdForGraph(), sim,
				 * Edge.TYPE.CANDIDATE_CANDIDATE); }
				 * 
				 * }
				 */
			}

		}

	}

	public static void setCandRelSimCutOff(Double val) {

		cand_rel_sim_cut_off = val;
	}
	/*
	 * private static void processCellAnnotationsOnSameColumn(List<Pair<String,
	 * Annotation>> cell_annotations, List<Pair<String, Annotation>>
	 * other_cell_annotaions, Table_ table, Graph graph, boolean
	 * general_relatedness) { if (other_cell_annotaions == null ||
	 * cell_annotations == null) { return; } List<Candidate> cell_candidates,
	 * other_cell_candidates; String cell_id, other_id; double sim = 0.0; for
	 * (Pair<String, Annotation> cell_annotaion : cell_annotations) { cell_id =
	 * cell_annotaion.first + "_" + cell_annotaion.second.getUniqueID();
	 * cell_candidates = table.getCandidates(cell_id); for (Pair<String,
	 * Annotation> other_cell_annotaion : other_cell_annotaions) { // check for
	 * all the candidates other_id = other_cell_annotaion.first + "_" +
	 * other_cell_annotaion.second.getUniqueID(); other_cell_candidates =
	 * table.getCandidates(other_id); // add an edge between the 2 mentions
	 * first --> full graph, // connected // from the table size sim =
	 * WeightsCalculator.getSameColumnMentionSimilarity(cell_annotaion.first,
	 * other_cell_annotaion.first); graph.addEdge(cell_id, other_id, sim,
	 * Edge.TYPE.SAME_COLUMN);
	 * 
	 * if (cell_candidates == null || other_cell_candidates == null) { continue;
	 * } for (Candidate cell_candidate : cell_candidates) { for (Candidate
	 * other_cell_candidate : other_cell_candidates) { sim =
	 * WeightsCalculator.getSameColumnSimilarity(cell_candidate,
	 * other_cell_candidate, general_relatedness); // if (sim >
	 * cand_rel_sim_cut_off)
	 * graph.addEdge(cell_candidate.getSemanticTargetIdForGraph(),
	 * other_cell_candidate.getSemanticTargetIdForGraph(), sim,
	 * Edge.TYPE.CANDIDATE_CANDIDATE); }
	 * 
	 * } }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * private static void processCellAnnotationsOnSameRow(List<Pair<String,
	 * Annotation>> cell_annotations, List<Pair<String, Annotation>>
	 * other_cell_annotaions, Table_ table, Graph graph, boolean
	 * general_relatedness) { if (other_cell_annotaions == null ||
	 * cell_annotations == null) { return; } List<Candidate> cell_candidates,
	 * other_cell_candidates; String cell_id, other_id; double sim = 0.0; for
	 * (Pair<String, Annotation> cell_annotaion : cell_annotations) { cell_id =
	 * cell_annotaion.first + "_" + cell_annotaion.second.getUniqueID();
	 * cell_candidates = table.getCandidates(cell_id); for (Pair<String,
	 * Annotation> other_cell_annotaion : other_cell_annotaions) { other_id =
	 * other_cell_annotaion.first + "_" +
	 * other_cell_annotaion.second.getUniqueID(); // check for all the
	 * candidates other_cell_candidates = table.getCandidates(other_id); // add
	 * an edge between the 2 mentions first --> full graph, // connected // from
	 * the table size sim =
	 * WeightsCalculator.getSameRowMentionsSimilarity(cell_annotaion.first,
	 * other_cell_annotaion.first); graph.addEdge(cell_id, other_id, sim,
	 * Edge.TYPE.SAME_ROW);
	 * 
	 * if (cell_candidates == null || other_cell_candidates == null) { continue;
	 * } for (Candidate cell_candidate : cell_candidates) { for (Candidate
	 * other_cell_candidate : other_cell_candidates) { sim =
	 * WeightsCalculator.getSameRowSimilarity(cell_candidate,
	 * other_cell_candidate, general_relatedness); // if (sim >
	 * cand_rel_sim_cut_off)
	 * graph.addEdge(cell_candidate.getSemanticTargetIdForGraph(),
	 * other_cell_candidate.getSemanticTargetIdForGraph(), sim,
	 * Edge.TYPE.CANDIDATE_CANDIDATE); }
	 * 
	 * } }
	 * 
	 * } }
	 */
}
