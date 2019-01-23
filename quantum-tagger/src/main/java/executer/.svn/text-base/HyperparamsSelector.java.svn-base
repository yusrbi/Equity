package executer;

import graph.Edge;
import graph.GraphBuilder;
import graph.Node;
import graph.Solver;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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
import data.WebDocument;
import utils.FileUtils;

public class HyperparamsSelector {
	final static Options options = new Options();
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	static Resources resources = null;
	static MentionsFilter filter = null;
	static DocumentLoader documentLoader = null;
	static ResourcesLoader resources_loader = null;
	static Annotator annotator = null;

	public static void main(String arg[]) throws IOException {

		final CommandLineParser parser = new BasicParser();
		long start_time, end_time, start_time2, end_time2;
		CommandLine cmd;
		Options options = createCommandLineOptions();
		Document_ document = null;
		resources_loader = new ResourcesLoader();

		filter = new MentionsFilter();
		start_time = System.nanoTime();

		String[] files = null;
		File dir = null;
		boolean full = false, source = false;
		try {
			resources = resources_loader.load();
			documentLoader = DocumentLoader.getLoader(resources);
			annotator = new Annotator(resources);
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
		}
		HyperparamGenerator.getHyperparamGenerator().generate(500);
		HyperparamGenerator.getHyperparamGenerator()
				.writeHyperparamsToFile("/GW/D5data/yibrahim/misc/jars/hyperparams_500_1908.lst");
		for (String file : files) {

			start_time2 = System.nanoTime();
			try {
				
				slogger_.info("Loading file :" + file);
				Annotation.reset_counter();
				Candidate.reset_counter();
				document = documentLoader.load(dir.getPath() + File.separator, file);
				document.setFile_name(file);
				slogger_.info("processing document: " + document.getFile_name() + ", url: " + document.getId());
				annotator.annotate(document);
				slogger_.info("Done with annotations");
				CandidatesSearch.findCandidates(document);
				slogger_.info("Done with candidates selection");
				filter.filterTableMentions(document);
				slogger_.info("Done with candidates filtering");
				
				//run the experiment
				
//				full = true; source = true;
//				run_experiemnt(dir, document, full,source, true);
//				full = true; source = false;
//				run_experiemnt(dir, document, full,source, false);
				full = false; source = true;
				run_experiemnt(dir, document, full,source, true);
//				full = false; source = false;
//				run_experiemnt(dir, document, full,source, false);
				slogger_.info("Done!!!!");
				end_time2 = System.nanoTime();
				slogger_.info(
						"It took me : " + ((end_time2 - start_time2) / 1000000) + " ms to process this document!");

			} catch (Exception e) {
				slogger_.error(e.getMessage());
				e.printStackTrace();
			}
		}
		end_time = System.nanoTime();
		slogger_.info("It took me : " + ((end_time - start_time) / 1000000) + " ms to process these documents!");

	}

	private static void run_experiemnt(File dir, Document_ document, boolean full, boolean source, boolean initDB)
			throws ClassCastException, ClassNotFoundException, IOException, SQLException {
		int id = 1;
		
		
		for (double[] hyperparams : HyperparamGenerator.getHyperparamGenerator().getHyperparams()) {
			try {
				slogger_.info(String.format("Start experiment for Hyperparams %d:", id) + toString(hyperparams));
				resources_loader.setHyperparams(hyperparams);
				Node.reset_counter();
				Edge.reset_counter();
				document.deleteResults();
				if (id > 1)// only init db once
					initDB = false;
				GraphBuilder.buildGraph(document, 0, full, resources.is_general_relatedness(), initDB);
				slogger_.info("Done with graph construction");
				document.setExperiement_id(String.format("test2108_3_Hyperparams_Estimation_%d_%s_%s",id,  (full? "full":"red") , (source? "mn":"cand")));
				
				if (full && source) {
					Solver.doRandomWalk_fromMentions(document, 0);
					slogger_.info("Done with RWR from Mentions on Full graph");
				} else if(full && !source){
					Solver.doRandomWalk_fromCandidates(document, 0);
					slogger_.info("Done with RWR from Candidates on Full graph");
				}else if(!full && source){
					Solver.doRandomWalk_fromMentions(document, 0);
					slogger_.info("Done with RWR from Mentions on partial graph");
				}else{// !full && ! source
					Solver.doRandomWalk_fromCandidates_cohen_plus(document, 0);
					slogger_.info("Done with RWR from Candidates on partial graph");
				}

				document.writeResultstoDB();
				document.getTable(0).writeResultstoDB();
				id++;
			} catch (Exception e) {
				id++;
				slogger_.error(e.getMessage());
			}
		}
	}

	private static String toString(double[] hyperparams) {
		StringBuilder str = new StringBuilder();
		for (double hyperparam : hyperparams) {
			str.append(String.valueOf(hyperparam) + ",");
		}
		return str.toString();
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
