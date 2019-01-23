package evaluation;

import graph.Edge;
import graph.GraphBuilder;
import graph.Node;
import graph.Solver;

//import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.stream.JsonParsingException;

import knowledgebase.MentionsFilter;
import knowledgebase.Candidate;
import knowledgebase.CandidatesSearch;
import loader.DocumentLoader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotations.Annotation;
import resources.Resources;
import resources.ResourcesLoader;
import annotators.Annotator;
import data.Document_;
import data.Table_;
import data.WebDocument;
import executer.Experiment;
import executer.Hyperparams.EXPERIMENT_ID;
import executer.Hyperparams.SETTINGS_ID;
import utils.FileUtils;

public class Evaluator_src {
	final static Options options = new Options();
	final static int nPages = 10;
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	static Resources resources = null;
	static ResourcesLoader resources_loader = null;
	static List<Integer> results = null;
	static Map<String, List<Double>> all_results;
	static Map<String, Double> precision = null;
	static Annotator annotator;
	private static int mentions_count = 0;
	static Experiment[] experiments = {
		new Experiment(SETTINGS_ID.DEFAULT, EXPERIMENT_ID.full_src, "sarwagy_downey_exp_full_src_2108"),
			new Experiment(SETTINGS_ID.DEFAULT, EXPERIMENT_ID.part_src, "sarwagy_downey_exp_part_src_2108"),
			new Experiment(SETTINGS_ID.NO_CAND_CAND, EXPERIMENT_ID.part_src,
					"sarwagy_downey_exp_NO_CAND_CAND_part_src_2108"),
			new Experiment(SETTINGS_ID.NO_TABLE_STRUCTURE, EXPERIMENT_ID.part_src,
					"sarwagy_downey_exp_NO_TABLE_STRUCTURE_part_src_2108"),
			new Experiment(SETTINGS_ID.NO_SAME_SURFACE, EXPERIMENT_ID.part_src,
					"sarwagy_downey_exp_NO_SAME_SURFACE_part_src_2108"),
			new Experiment(SETTINGS_ID.NO_CAND_CAND, EXPERIMENT_ID.full_src,
					"sarwagy_downey_exp_NO_CAND_CAND_full_src_2108"),
			new Experiment(SETTINGS_ID.NO_TABLE_STRUCTURE, EXPERIMENT_ID.full_src,
					"sarwagy_downey_exp_NO_TABLE_STRUCTURE_full_src_2108"),
			new Experiment(SETTINGS_ID.NO_SAME_SURFACE, EXPERIMENT_ID.full_src,
					"sarwagy_downey_exp_NO_SAME_SURFACE_full_src_2108"),
			 };

	public static void main(String arg[]) throws IOException {

		DowneyTabEL.load_valid_wiki_ids_in_yago();

		precision = new HashMap<String, Double>();
		all_results = new HashMap<String, List<Double>>();
		resources_loader = new ResourcesLoader();
		resources = resources_loader.load();
		annotator = new Annotator(resources);
		// sunita sarwagi web_manual
		String sarwagi_main_path = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/";
		String downey_main_path = "/GW/D5data/yibrahim/misc/data/TabEL/tables_wiki_random.json";

		String tables_path = sarwagi_main_path + "tablesForAnnotation/",
				gs_path = sarwagi_main_path + "workspace/WWT_GroundTruth/";
		String web_tables_fixed = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/fixedWebTables.txt";
		String web_tables = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/webtables_flat.txt";
		String wiki_links = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/wikilinks_flat_2.txt";
		String wiki_tables = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/wikitables_flat.txt";
		Sarwagi sunita;
		// webtables
		try {
			sunita = new Sarwagi(tables_path, gs_path + "annotation/", web_tables, false);
			slogger_.info("Loaded web_manual corpus from Sarwagi, starting experiments");
			slogger_.info("Loaded web_manual corpus from S Sarwagi, starting experiments");
			mentions_count = 0;
			while (sunita.hasNext()) {
				try {
					doAllExperiments(sunita.next(), "Sunita web_manual");
					slogger_.info("Done " + sunita.current_index +" documents");
					printUpdatedResults("Sunita web_manual");
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			calculate_precision("Sunita web_manual");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
//		try {
//			// fixed webtables
//			sunita = new Sarwagi("/GW/D5data/yibrahim/misc/data/SunitaSarawagi/fixedWebTables/",
//					"/GW/D5data/yibrahim/misc/data/SunitaSarawagi/fixedWebTablesAnnotations/", web_tables_fixed, false);
//			slogger_.info("Loaded web_manual_fixed corpus from Sarwagi, starting experiments");
//			slogger_.info("Loaded web_manual corpus from S Sarwagi, starting experiments");
//			results = null;
//			mentions_count = 0;
//			while (sunita.hasNext()) {
//				try {
//					doAllExperiments(sunita.next(), "Sunita web_manual_fixed");
//					printUpdatedResults("Sunita web_manual_fixed");
//				} catch (Exception exc) {
//					exc.printStackTrace();
//				}
//			}
//			calculate_precision("Sunita web_manual_fixed");
//
//		} catch (Exception exc) {
//			exc.printStackTrace();
//		}
//
		try {
			// sunita wiki_links
			sunita = new Sarwagi(tables_path, gs_path, wiki_links, true);
			slogger_.info("Loaded wiki_links corpus from Sarwagi, starting experiments");
	//		slogger_.info("Loaded wiki_links corpus from S Sarwagi, starting experiments");
			results = null;
			mentions_count = 0;
			int doc_count=0;
			while (sunita.hasNext()) {
				try {
					doAllExperiments(sunita.next(), "Sunita wiki_links");
					slogger_.info("Done " + sunita.current_index +" documents");
					printUpdatedResults("Sunita wiki_links");
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			System.out.println(doc_count);
			calculate_precision("Sunita wiki_links");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		// sunita wiki_manual
//		try {
//			sunita = new Sarwagi(tables_path, gs_path + "annotation/", wiki_tables, true);
//			results = null;
//			slogger_.info("Loaded wiki_tables corpus from Sarwagy, starting experiments");
//			slogger_.info("Loaded wiki_tables corpus from S Sarwagi, starting experiments");
//			mentions_count = 0;
//			while (sunita.hasNext()) {
//				try {
//					doAllExperiments(sunita.next(), "Sunita wiki_manual");
//					printUpdatedResults("Sunita wiki_manual");
//				} catch (Exception exc) {
//					exc.printStackTrace();
//				}
//			}
//			calculate_precision("Sunita wiki_manual");
//		} catch (Exception exc) {
//			exc.printStackTrace();
//		}
		try {
			// downey wiki_random
			DowneyTabEL downey = new DowneyTabEL(downey_main_path);

			results = null;
			slogger_.info("Loaded wiki_random corpus from D Downey, starting experiments");
			slogger_.info("Loaded wiki_random corpus from D Downey, starting experiments");
			mentions_count = 0;
			while (downey.hasNext()) {
				try {
					doAllExperiments(downey.next(), "Downey wiki_random");
					slogger_.info("Done " + downey.current_index +" documents");
					printUpdatedResults("Downey wiki_random");
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			calculate_precision("Downey wiki_random");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		printAllPrecision();
	}

	private static void printAllPrecision() {
		for (Entry<String, Double> entry : precision.entrySet()) {
			slogger_.info(entry.getKey() + "," + entry.getValue());
			//slogger_.info(entry.getKey() + " :" + entry.getValue());
		}
	}
	private static void printUpdatedResults(String dataset) {
		String key;
		slogger_.info("Updated Results for :"+dataset);
		for (int i = 0; i < experiments.length; i++) {
			key = String.format("%s,%s", dataset, experiments[i].id);
			if (precision.containsKey(key)) {
				slogger_.info(key + "," + (double) precision.get(key) / (double) mentions_count);
				slogger_.info(String.format("total mentions evaluated:%d, Total correct:%f", mentions_count, (double) precision.get(key)));
			}
		}
		
	}
	private static void calculate_precision(String dataset) {
		String key;
		slogger_.info("Results for :"+dataset);
		for (int i = 0; i < experiments.length; i++) {
			key = String.format("%s,%s", dataset, experiments[i].id);
			if (precision.containsKey(key)) {
				precision.put(key, (double) precision.get(key) / (double) mentions_count);
				slogger_.info(key + "," + precision.get(key));
				slogger_.info(String.format("total mentions evaluated:%d, Total correct:%f", mentions_count, (double) precision.get(key)));
			}
		}
		mentions_count = 0;

	}
	private static void printAllResults() {
		for (Entry<String, List<Double>> entry : all_results.entrySet()) {
			slogger_.info(entry.getKey() + " recall= " + entry.getValue().get(0));
			slogger_.info(entry.getKey() + " precision=  " + entry.getValue().get(1));
		}

	}

	private static void print_results() {
		System.out
				.println("total precision for this collection:" + ((double) results.get(0) / (double) results.get(1)));

	}

	private static void doAllExperiments(Document_ document, String data_set) throws SQLException {
		if (document== null || document.getTable(0).getInverted_annotations() == null) {
			slogger_.info("Empty annotations for :" + document.getFile_name() == null ? document.getTitle()
					: document.getFile_name());
			return;
		}
		slogger_.info(
				"table file Name :" + document.getFile_name() == null ? document.getTitle() : document.getFile_name());

		boolean isInitDB = true;
		boolean source = true, full = true;

		document.getTable(0).createAnnotatedNERText();
		annotator.annotateTableHeader(document.getTable(0));
		CandidatesSearch.findCandidates(document);
		List<Integer> new_results = null;
		for (int i = 0; i < experiments.length; i++) {
			Node.reset_counter();
			Edge.reset_counter();
			document.deleteResults();
			if (i > 0)
				isInitDB = false;
			try {
				full = experiments[i].isFull();
				source = experiments[i].isSource();
				document.setExperiement_id(experiments[i].id);
				resources_loader.loadHyperparams(experiments[i].experiment, experiments[i].settings);
				GraphBuilder.buildGraph(document, 0, full, true, isInitDB);
				if (source) {
					Solver.doRandomWalk_fromMentions(document, 0);
					slogger_.info("Done with RWR from Mentions");
				} else if (experiments[i].experiment.equals(EXPERIMENT_ID.full_dest)) {
					Solver.doRandomWalk_fromCandidates(document, 0);
					slogger_.info("Done with RWR from Candidates on Full graph");
				} else if (experiments[i].experiment.equals(EXPERIMENT_ID.part_dest_cohen_p)) {
					Solver.doRandomWalk_fromCandidates_cohen_plus(document, 0);
					slogger_.info("Done with RWR from Candidates on partial graph");
				}
				new_results = document.getTable(0).calculatePrecision();
				String key = String.format("%s,%s", data_set, experiments[i].id);
				if (precision.containsKey(key))
					precision.put(key, precision.get(key) + (double) new_results.get(0));
				else
					precision.put(key, (double) new_results.get(0));

				

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		mentions_count += new_results.get(1);
	}

	private static void doExperiment(Document_ document, boolean full, boolean source) {

		if (document.getTable(0).getInverted_annotations() == null) {
			slogger_.info("Empty annotations for :" + document.getFile_name() == null ? document.getTitle()
					: document.getFile_name());
			return;
		}
		slogger_.info(
				"table file Name :" + document.getFile_name() == null ? document.getTitle() : document.getFile_name());
		boolean initDB = true;
		// find candidates
		try {

			document.getTable(0).createAnnotatedNERText();
			annotator.annotateTableHeader(document.getTable(0));
			CandidatesSearch.findCandidates(document);

			document.setExperiement_id(
					String.format("Sarwagi_Dawney_%s_%s", (full ? "full" : "part"), (source ? "src" : "dest")));
			// solve
			if (full && source) {
				resources_loader.loadHyperparams(EXPERIMENT_ID.full_src, SETTINGS_ID.DEFAULT);

				// build the graph
				GraphBuilder.buildGraph(document, 0, full, true, initDB);
				slogger_.info("Done with graph construction");
				Solver.doRandomWalk_fromMentions(document, 0);
				slogger_.info("Done with RWR from Mentions on Full graph");
			} else if (full && !source) {
				resources_loader.loadHyperparams(EXPERIMENT_ID.full_dest, SETTINGS_ID.DEFAULT);

				// build the graph
				GraphBuilder.buildGraph(document, 0, full, true, initDB);
				slogger_.info("Done with graph construction");
				Solver.doRandomWalk_fromCandidates(document, 0);
				slogger_.info("Done with RWR from Candidates on Full graph");
			} else if (!full && source) {
				resources_loader.loadHyperparams(EXPERIMENT_ID.part_src, SETTINGS_ID.DEFAULT);

				// build the graph
				GraphBuilder.buildGraph(document, 0, full, true, initDB);
				slogger_.info("Done with graph construction");

				Solver.doRandomWalk_fromMentions(document, 0);
				slogger_.info("Done with RWR from Mentions on partial graph");
			} else {
				resources_loader.loadHyperparams(EXPERIMENT_ID.part_dest_cohen_p, SETTINGS_ID.DEFAULT);

				// build the graph
				GraphBuilder.buildGraph(document, 0, full, true, initDB);
				slogger_.info("Done with graph construction");
				Solver.doRandomWalk_fromCandidates_cohen_plus(document, 0);
				slogger_.info("Done with RWR from Candidates on partial graph");
			}

			// get the precision
			List<Integer> new_results = document.getTable(0).calculatePrecision();
			if (results == null) {
				results = new_results;
			} else {
				results.set(0, results.get(0) + new_results.get(0));
				results.set(1, results.get(1) + new_results.get(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		slogger_.info("Updated Precision:" + ((double) results.get(0) / (double) results.get(1)));
		Candidate.reset_counter();
		Node.reset_counter();
		Edge.reset_counter();
	}

	private static Options createCommandLineOptions() {

		options.addOption("h", "help", false, "show help.");
		options.addOption("f", "Full Graph Mode", true, "true or false");
		options.addOption("s", "Start from the source (mentions) or distination(candidates) Mode", true,
				"true or false");
		// options.addOption("o","output file",true,"output directory");
		return options;
	}

	private static void help() {
		// This prints out some help
		HelpFormatter formater = new HelpFormatter();
		formater.printHelp("Main", options);
		System.exit(0);
	}

	public static void main_old(String arg[]) throws IOException {
		final CommandLineParser parser = new BasicParser();
		boolean source = false, full = false;
		CommandLine cmd;
		double recall, precision;
		try {
			createCommandLineOptions();
			cmd = parser.parse(options, arg);

			if (cmd.hasOption("h"))
				help();
			if (!cmd.hasOption("f")) {
				slogger_.info("please enter full mode");
				return;
			}
			full = Boolean.valueOf((String) cmd.getOptionValue("f"));

			if (!cmd.hasOption("s")) {
				slogger_.info("please enter source/destination start RWR mode: -s source or -s destination ");
				return;
			}
			if (cmd.getOptionValue("s").equals("source")) {
				source = true;
			} else {
				source = false;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DowneyTabEL.load_valid_wiki_ids_in_yago();
		all_results = new HashMap<String, List<Double>>();
		resources_loader = new ResourcesLoader();
		resources = resources_loader.load();
		annotator = new Annotator(resources);
		// sunita sarwagi web_manual
		String sarwagi_main_path = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/";
		String downey_main_path = "/GW/D5data/yibrahim/misc/data/TabEL/tables_wiki_random.json";

		String tables_path = sarwagi_main_path + "tablesForAnnotation/",
				gs_path = sarwagi_main_path + "workspace/WWT_GroundTruth/";
		String web_tables_fixed = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/fixedWebTables.txt";
		String web_tables = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/webtables_flat.txt";
		String wiki_links = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/wikilinks_flat2.txt";
		String wiki_tables = "/GW/D5data/yibrahim/misc/data/SunitaSarawagi/TablesannotationData/wikitables_flat.txt";
		Sarwagi sunita;
		// webtables
		try {
			sunita = new Sarwagi(tables_path, gs_path + "annotation/", web_tables, false);
			slogger_.info("Loaded web_manual corpus from S Sarwagi, starting experiments");
			results = null;
			while (sunita.hasNext()) {
				try {
					doExperiment(sunita.next(), full, source);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			recall = (double) results.get(1) / (double) sunita.getTotal_annotations();
			precision = (double) results.get(0) / (double) results.get(1);
			all_results.put("Sunita WebTables: ", Arrays.asList(recall, precision));
			print_results();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			// fixed webtables
			sunita = new Sarwagi("/GW/D5data/yibrahim/misc/data/SunitaSarawagi/fixedWebTables/",
					"/GW/D5data/yibrahim/misc/data/SunitaSarawagi/fixedWebTablesAnnotations/", web_tables_fixed, false);
			slogger_.info("Loaded web_manual corpus from S Sarwagi, starting experiments");
			results = null;
			while (sunita.hasNext()) {
				try {
					doExperiment(sunita.next(), full, source);
				} catch (Exception exc) {
					exc.printStackTrace();
				}

			}
			recall = (double) results.get(1) / (double) sunita.getTotal_annotations();
			precision = (double) results.get(0) / (double) results.get(1);
			all_results.put("Sunita FixedWebTables: ", Arrays.asList(recall, precision));
			print_results();
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		try {
			// sunita wiki_links
			sunita = new Sarwagi(tables_path, gs_path, wiki_links, true);
			slogger_.info("Loaded wiki_links corpus from S Sarwagi, starting experiments");
			results = null;
			while (sunita.hasNext()) {
				try {
					doExperiment(sunita.next(), full, source);
				} catch (Exception exc) {
					exc.printStackTrace();
				}

			}
			recall = (double) results.get(1) / (double) sunita.getTotal_annotations();
			precision = (double) results.get(0) / (double) results.get(1);
			all_results.put("Sunita WikiLinks: ", Arrays.asList(recall, precision));
			print_results();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		// sunita wiki_manual
		try {
			sunita = new Sarwagi(tables_path, gs_path + "annotation/", wiki_tables, true);
			results = null;
			slogger_.info("Loaded wiki_tables corpus from S Sarwagi, starting experiments");
			while (sunita.hasNext()) {
				try {
					doExperiment(sunita.next(), full, source);
				} catch (Exception exc) {
					exc.printStackTrace();
				}

			}
			recall = (double) results.get(1) / (double) sunita.getTotal_annotations();
			precision = (double) results.get(0) / (double) results.get(1);
			all_results.put("Sunita WikiTables: ", Arrays.asList(recall, precision));
			print_results();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		try {
			// downey wiki_random
			DowneyTabEL downey = new DowneyTabEL(downey_main_path);

			results = null;
			slogger_.info("Loaded wiki_random corpus from D Downey, starting experiments");
			while (downey.hasNext()) {
				try {
					doExperiment(downey.next(), full, source);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
			recall = (double) results.get(1) / (double) downey.getTotal_annotations();
			precision = (double) results.get(0) / (double) results.get(1);
			all_results.put("Downey WikiRandom: ", Arrays.asList(recall, precision));
			print_results();
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		printAllResults();
	}

}
