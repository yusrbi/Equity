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
import annotators.StanfordWrapper;
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

public class RunTimeAnalysis_full {

	final static Options options = new Options();
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	static Resources resources = null;
	static MentionsFilter filter = null;
	static DocumentLoader documentLoader;
	static Annotator annotator;
	static ResourcesLoader resources_loader = null;
	static Experiment[] experiments = {
			new Experiment(SETTINGS_ID.DEFAULT, EXPERIMENT_ID.full_src, "0708_runtime_full_src"), 
			//new Experiment(SETTINGS_ID.DEFAULT, EXPERIMENT_ID.part_src, "1508_3_runtime_part_src"),
			};

	public static void main(String arg[]) throws IOException {

		final CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		Options options = createCommandLineOptions();
		Document_ document = null;
		resources_loader = new ResourcesLoader();
		filter = new MentionsFilter();
		RunTimeAnalysis.start_time = System.nanoTime();
		RunTimeAnalysis.mile_stone_start = System.nanoTime();
		String[] files = null;
		File dir = null;
		try {

			resources = resources_loader.load();
			 StanfordWrapper.getStanfordWrapper();
			// load document first
			documentLoader = DocumentLoader.getLoader(resources);
			annotator = new Annotator( resources);
			cmd = parser.parse(options, arg);

			if (cmd.hasOption("h"))
				help();
			if (!cmd.hasOption("i")) {
				slogger_.info("input folder name is required");
				return;
			}
			
			dir = new File((String) cmd.getOptionValue("i"));
			files = dir.list();

		} catch (IOException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		} catch (ParseException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RunTimeAnalysis.mile_stone_end = System.nanoTime();
		RunTimeAnalysis.initial_load_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
		for (String file : files) {
			RunTimeAnalysis.start_time2 = System.nanoTime();
			RunTimeAnalysis.loading_db_time =0;
			RunTimeAnalysis.webservice_call_time =0;
			try {
				run_experiemnt(dir, file);
				slogger_.info("Done!!!!");
				RunTimeAnalysis.end_time2 = System.nanoTime();
				RunTimeAnalysis.document_total_process_time = RunTimeAnalysis.end_time2 - RunTimeAnalysis.start_time2;
				slogger_.info(
						"It took me :\t " + (RunTimeAnalysis.document_total_process_time/ 1000000) + " ms to process this document!");
				RunTimeAnalysis.count_docs_processed++;
				print_stats();
				add_time_to_average();
			} catch (Exception e) {
				slogger_.error(e.getMessage());
				e.printStackTrace();
			}
		}
		RunTimeAnalysis.end_time = System.nanoTime();
		slogger_.info("It took me :\t " + ((RunTimeAnalysis.end_time - RunTimeAnalysis.start_time) / 1000000) + " ms to process these documents!");
		print_average_time();
	}

	private static void print_average_time() {
		slogger_.info("Stats:\t");
		//slogger_.info("Stats:\t");
		String msg;
		msg = String.format("Average Total Processing Time:\t %d", RunTimeAnalysis.avg_document_total_process_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		
		msg = String.format("Average initial_load_time:\t %d", RunTimeAnalysis.avg_initial_load_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
	//	slogger_.info(msg);
		msg = String.format("Average document_load_time:\t %d",  RunTimeAnalysis.avg_document_load_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
	///	slogger_.info(msg);
		msg = String.format("Average annotation_time:\t %d",  RunTimeAnalysis.avg_annotation_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
	//	slogger_.info(msg);
		msg = String.format("Average annotatiion_enrichment_time:\t %d",  RunTimeAnalysis.avg_annotatiion_enrichment_time / RunTimeAnalysis.count_docs_processed);
		//slogger_.info(msg);
		slogger_.info(msg);
		msg = String.format("Average candidate_selection_time:\t %d",  RunTimeAnalysis.avg_candidate_selection_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("Average candidate_filtering_time:\t %d",  RunTimeAnalysis.avg_candidate_filtering_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		msg = String.format(" Average web service call time:\t %d", RunTimeAnalysis.avg_webservice_call_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("Average graph_build_time:\t %d",  RunTimeAnalysis.avg_graph_build_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("Average graph_solve_time:\t %d",  RunTimeAnalysis.avg_graph_solve_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("Average writing_bd_time:\t %d",  RunTimeAnalysis.avg_writing_db_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("Average loading_db_time:\t %d",  RunTimeAnalysis.avg_loading_db_time / RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("Average n Nodeds:\t %d",   RunTimeAnalysis.total_node_count/ RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);
		
		msg = String.format("Average n Edges:\t %d",   RunTimeAnalysis.total_edge_count/ RunTimeAnalysis.count_docs_processed);
		slogger_.info(msg);
		//slogger_.info(msg);


	}

	private static void add_time_to_average() {
		RunTimeAnalysis.avg_initial_load_time += RunTimeAnalysis.initial_load_time / 1000000;
		RunTimeAnalysis.avg_document_load_time += RunTimeAnalysis.document_load_time / 1000000;
		RunTimeAnalysis.avg_annotation_time += RunTimeAnalysis.annotation_time / 1000000;
		RunTimeAnalysis.avg_annotatiion_enrichment_time += RunTimeAnalysis.annotatiion_enrichment_time / 1000000;
		RunTimeAnalysis.avg_candidate_selection_time += RunTimeAnalysis.candidate_selection_time / 1000000;
		RunTimeAnalysis.avg_candidate_filtering_time += RunTimeAnalysis.candidate_filtering_time / 1000000;
		RunTimeAnalysis.avg_graph_build_time += RunTimeAnalysis.graph_build_time / 1000000;
		RunTimeAnalysis.avg_graph_solve_time += RunTimeAnalysis.graph_solve_time / 1000000;
		RunTimeAnalysis.avg_writing_db_time += RunTimeAnalysis.writing_bd_time / 1000000;
		RunTimeAnalysis.avg_loading_db_time += RunTimeAnalysis.loading_db_time / 1000000;
		RunTimeAnalysis.avg_webservice_call_time += RunTimeAnalysis.webservice_call_time / 1000000;
		RunTimeAnalysis.avg_document_total_process_time += RunTimeAnalysis.document_total_process_time / 1000000;

	}

	private static void print_stats() {
		slogger_.info("Stats:\t");
		//slogger_.info("Stats:\t");
		String msg;
		msg = String.format("Total Processing Time:\t %d", RunTimeAnalysis.document_total_process_time / 1000000);
		slogger_.info(msg);
		msg = String.format("initial_load_time:\t %d", RunTimeAnalysis.initial_load_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("document_load_time:\t %d", RunTimeAnalysis.document_load_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("annotation_time:\t %d", RunTimeAnalysis.annotation_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("annotatiion_enrichment_time:\t %d", RunTimeAnalysis.annotatiion_enrichment_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("candidate_selection_time:\t %d", RunTimeAnalysis.candidate_selection_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("candidate_filtering_time:\t %d", RunTimeAnalysis.candidate_filtering_time / 1000000);
		slogger_.info(msg);
		msg = String.format("web service call time:\t %d", RunTimeAnalysis.webservice_call_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("graph_build_time:\t %d", RunTimeAnalysis.graph_build_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("graph_solve_time:\t %d", RunTimeAnalysis.graph_solve_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("writing_bd_time:\t %d", RunTimeAnalysis.writing_bd_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		msg = String.format("loading_db_time:\t %d", RunTimeAnalysis.loading_db_time / 1000000);
		slogger_.info(msg);
		//slogger_.info(msg);
		
		msg = String.format("n Nodeds:\t %d",  RunTimeAnalysis.node_count);
		slogger_.info(msg);
		slogger_.info(msg);
		
		msg = String.format("n Edges:\t %d",  RunTimeAnalysis.edge_count);
		slogger_.info(msg);
		//slogger_.info(msg);

	}

	private static void run_experiemnt(File dir, String file)
			throws ClassCastException, ClassNotFoundException, IOException, SQLException {
		RunTimeAnalysis.mile_stone_start = System.nanoTime();
		// load document first
		int id = 1;

		slogger_.info("Loading file :\t" + file);
		Annotation.reset_counter();
		Candidate.reset_counter();
		Document_ document = documentLoader.load(dir.getPath() + File.separator, file);
		document.setFile_name(file);
		RunTimeAnalysis.mile_stone_end = System.nanoTime();
		RunTimeAnalysis.document_load_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
		slogger_.info("processing document:\t " + document.getFile_name() + ", url:\t " + document.getId());
		RunTimeAnalysis.mile_stone_start = System.nanoTime();
		annotator.annotate(document);
		slogger_.info("Done with annotations");
		
		RunTimeAnalysis.mile_stone_start = System.nanoTime();
		CandidatesSearch.findCandidates(document);
		RunTimeAnalysis.mile_stone_end = System.nanoTime();
		RunTimeAnalysis.candidate_selection_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
		slogger_.info("Done with candidates selection");
		RunTimeAnalysis.mile_stone_start = System.nanoTime();
		filter.filterTableMentions(document);
		RunTimeAnalysis.mile_stone_end = System.nanoTime();
		RunTimeAnalysis.candidate_filtering_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;

		slogger_.info("Done with candidates filtering");
		// document.writeDocumentWithAnnotationsToDB();
		// document.getTable(0).writeTableWithAnnotationsToDB();
		boolean initDB = true;
		for (Experiment experiemnt : experiments) {
			try {
				document.setExperiement_id(experiemnt.id);
				Node.reset_counter();
				Edge.reset_counter();
				document.deleteResults();
				if (id > 1)// only init db once
					initDB = false;
				RunTimeAnalysis.mile_stone_start = System.nanoTime();
				GraphBuilder.buildGraph(document, 0, experiemnt.isFull(), resources.is_general_relatedness(), initDB);
				RunTimeAnalysis.node_count= document.getGraph().getNNodes();
				RunTimeAnalysis.edge_count = document.getGraph().getNEdges();
				RunTimeAnalysis.total_edge_count+=RunTimeAnalysis.edge_count;
				RunTimeAnalysis.total_node_count+=RunTimeAnalysis.node_count;
				RunTimeAnalysis.mile_stone_end = System.nanoTime();
				RunTimeAnalysis.graph_build_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
				slogger_.info("Done with graph construction");
				RunTimeAnalysis.mile_stone_start = System.nanoTime();
				
				if(experiemnt.isSource()){
					Solver.doRandomWalk_fromMentions(document, 0);
					slogger_.info("Done with RWR from mentions");
				}
				else{
					Solver.doRandomWalk_fromCandidates(document, 0);
					slogger_.info("Done with RWR from candidates");
				}			
				RunTimeAnalysis.mile_stone_end = System.nanoTime();
				RunTimeAnalysis.graph_solve_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
				RunTimeAnalysis.mile_stone_start = System.nanoTime();
				document.writeResultstoDB();
				document.getTable(0).writeResultstoDB();
				Solver.writeResults(document, Resources.getResources().getHtml_results_path() + "/mentions/", 0);
				RunTimeAnalysis.mile_stone_end = System.nanoTime();
				RunTimeAnalysis.writing_bd_time = RunTimeAnalysis.mile_stone_end - RunTimeAnalysis.mile_stone_start;
				id++;
			} catch (Exception e) {
				id++;
				e.printStackTrace();
				slogger_.error(e.getMessage());
			}
		}
	}

	private static Options createCommandLineOptions() {

		options.addOption("h", "help", false, "show help.");
		options.addOption("i", "input file name", true, "json input file");
		options.addOption("f", "Full Graph Mode", true, "true or false");
		options.addOption("s", "Start from the source (mentions) or distination(candidates) Mode", 
				true, "true or false");
		
		// options.addOption("o","output file",true,"output directory");
		return options;
	}

	private static void help() {
		// This prints out some help
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	}
	
}
