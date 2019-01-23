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

public class NoContextExperiment {

	final static Options options = new Options();
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	static Resources resources = null;
	static MentionsFilter filter = null;
	static DocumentLoader documentLoader ;
	static Annotator annotator ;
	static ResourcesLoader resources_loader = null;
	static Experiment[] experiments = {
			new Experiment(SETTINGS_ID.DEFAULT, EXPERIMENT_ID.part_src, "0308_NoContext_exp_part_src")
			};
				
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
		for (String file : files) {

			start_time2 = System.nanoTime();
			try {
				run_experiemnt(dir,file);
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

	private static void run_experiemnt(File dir, String file)
			throws ClassCastException, ClassNotFoundException, IOException, SQLException {
		// load document first
		int id = 1;
		
		slogger_.info("Loading file :" + file);
		Annotation.reset_counter();
		Candidate.reset_counter();
		Document_ document = documentLoader.load(dir.getPath() + File.separator , file);
		document.setFile_name(file);
		slogger_.info("processing document: " + document.getFile_name() + ", url: " + document.getId());
		annotator.annotate(document);
		slogger_.info("Done with annotations");
		CandidatesSearch.findCandidates(document);
		slogger_.info("Done with candidates selection");
		filter.filterTableMentions(document);
		slogger_.info("Done with candidates filtering");
		
		document.deleteAllMentions();
	//	document.writeDocumentWithAnnotationsToDB();
	//	document.getTable(0).writeTableWithAnnotationsToDB();
		boolean initDB=true;
		for (Experiment experiemnt : experiments) {
			try {
				resources_loader.loadHyperparams(experiemnt.experiment, experiemnt.settings);
			
				document.setExperiement_id(experiemnt.id);
				Node.reset_counter();
				Edge.reset_counter();
				document.deleteResults();
				if (id > 1)// only init db once
					initDB = false;
				if(experiemnt.experiment.equals(EXPERIMENT_ID.full_src) 
						|| experiemnt.experiment.equals(EXPERIMENT_ID.full_dest)){
						GraphBuilder.buildGraph(document, 0, true, resources.is_general_relatedness(), initDB);
						slogger_.info("Done with Full graph construction for:" + experiemnt.id);		
				}
				
				if(experiemnt.experiment.equals(EXPERIMENT_ID.part_src)
						||experiemnt.experiment.equals(EXPERIMENT_ID.part_dest_cohen)
						||experiemnt.experiment.equals(EXPERIMENT_ID.part_dest_cohen_p)
						||experiemnt.experiment.equals(EXPERIMENT_ID.part_dest_orig)){
					GraphBuilder.buildGraph(document, 0, false, resources.is_general_relatedness(), initDB);
					slogger_.info("Done with Partial graph construction for:" + experiemnt.id);
				}
			
				if(experiemnt.experiment.equals(EXPERIMENT_ID.part_src) 
						|| experiemnt.experiment.equals(EXPERIMENT_ID.full_src)){
					Solver.doRandomWalk_fromMentions(document, 0);
					slogger_.info("Done with RWR from mentions");
				}
				else if(experiemnt.experiment.equals(EXPERIMENT_ID.part_dest_orig) 
						|| experiemnt.experiment.equals(EXPERIMENT_ID.full_dest) ) {
					Solver.doRandomWalk_fromCandidates(document, 0);
					slogger_.info("Done with RWR from candidates");
				}else if(experiemnt.experiment.equals(EXPERIMENT_ID.part_dest_cohen_p) ){
					Solver.doRandomWalk_fromCandidates_cohen_plus(document, 0);
					slogger_.info("Cohen plus Done with RWR from candidates");
				}else if(experiemnt.experiment.equals(EXPERIMENT_ID.part_dest_cohen) ){
					Solver.doRandomWalk_fromCandidates_cohen(document, 0);
					slogger_.info("Cohen Done with RWR from candidates");
				}
				
				document.writeResultstoDB();
				document.getTable(0).writeResultstoDB();
				Solver.writeResults(document, Resources.getResources().getHtml_results_path() + "/mentions/", 0);
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
		options.addOption("s", "Start from the source (mentions) or distination(candidates) Mode", true, "true or false");
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
