package executer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Annotation;
import annotators.Annotator;
import data.Document_;
import executer.Hyperparams.EXPERIMENT_ID;
import executer.Hyperparams.SETTINGS_ID;
import graph.Edge;
import graph.GraphBuilder;
import graph.Node;
import graph.Solver;
import knowledgebase.Candidate;
import knowledgebase.CandidatesSearch;
import knowledgebase.MentionsFilter;
import loader.DocumentLoader;
import resources.Resources;
import resources.ResourcesLoader;

public class RunTimeAnalysis {

	public static long start_time, end_time, start_time2, end_time2, mile_stone_end, mile_stone_start;
	public static long initial_load_time, document_load_time, annotation_time, annotatiion_enrichment_time,
			candidate_selection_time, candidate_filtering_time, graph_build_time, graph_solve_time, writing_bd_time;
	public static long loading_db_time;
	public static long avg_initial_load_time, avg_document_load_time, avg_annotation_time,
			avg_annotatiion_enrichment_time, avg_candidate_selection_time, avg_candidate_filtering_time,
			avg_graph_build_time, avg_graph_solve_time, avg_writing_db_time;
	public static long edge_count, node_count,total_edge_count, total_node_count;
	public static long avg_loading_db_time;
	public static int count_docs_processed = 0;
	public static long webservice_call_time, avg_webservice_call_time;
	public static long document_total_process_time, avg_document_total_process_time;
	
}
