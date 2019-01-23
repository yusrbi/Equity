package executer;

import graph.Edge;
import graph.GraphBuilder;
import graph.Node;
import graph.Solver;

import java.io.File;
import java.io.IOException;
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

public class Evaluator {
	final static Options options = new Options();
	final static int nPages = 10;
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);

	public static void main(String arg[]) throws IOException {

		final CommandLineParser parser = new BasicParser();
		long start_time, end_time, start_time2, end_time2;
		CommandLine cmd;
		Options options = createCommandLineOptions();
		Document_ document = null;
		ResourcesLoader resources_loader = new ResourcesLoader();
		DocumentLoader documentLoader = null;
		Annotator annotator = null;
		MentionsFilter filter = new MentionsFilter();
		start_time = System.nanoTime();
		Resources resources = null;
		String[] files = null;
		File dir = null;
		boolean full = false;
		try {
			resources = resources_loader.load();
			documentLoader = DocumentLoader.getLoader(resources);
			annotator = new Annotator( resources);
			cmd = parser.parse(options, arg);

			if (cmd.hasOption("h"))
				help();
			if (!cmd.hasOption("i")) {
				slogger_.info("input folder name is required");
				return;
			}
			if (!cmd.hasOption("f")) {
				slogger_.info("please enter full mode: ");
				return;
			}
			full = Boolean.valueOf((String) cmd.getOptionValue("f"));
			dir = new File((String) cmd.getOptionValue("i"));
			files = dir.list();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		List<String> selected = new LinkedList<String>();
		for (String file : files) {
			try {

				slogger_.info("Loading file :" + file);
				 if(Solver.already_done(file, Resources.getResources().getHtml_results_path() + "/mentions/")){
					 continue;
				 }
				Annotation.reset_counter();
				Candidate.reset_counter();
				document = documentLoader.load(dir.getPath() + File.separator , file);
				if (document == null || document.getTables() == null || document.getTables().size() <= 0)
					continue;
				document.setFile_name(file);
//				if (((WebDocument)document).isWikipediaPage())
//					continue;
//				if (!document.getTable(0).isInteresting2())
//					continue;// first check rows/columns and qunatities
//				FileUtils.appendToFile("/GW/D5data/yibrahim/misc/data/selected_general_more300.txt", file);
				//slogger_.info(file);
				
				start_time2 = System.nanoTime();
				slogger_.info("processing document: " + document.getFile_name() + ", url: " + document.getId());
				annotator.annotate(document);
				slogger_.info("Done with annotations");
				CandidatesSearch.findCandidates(document);
				slogger_.info("Done with candidates selection");
				filter.filterTableMentions(document);
				slogger_.info("Done with candidates filtering");
				document.writeDocumentWithAnnotationsToDB();
				if (!document.getTable(0).isStillInteresting()) {
					continue; // check on annotations and entities
				
				}
//				selected.add(file);
//				FileUtils.appendToFile("/GW/D5data/yibrahim/misc/data/selected_general.txt", file);
//				slogger_.info(file);
				
				for (int i = 0; i < document.getTables().size(); i++) {
					document.getTable(i).writeTableWithAnnotationsToDB();
					Node.reset_counter();
					Edge.reset_counter();
					document.deleteResults();
					
					GraphBuilder.buildGraph(document, i, full, resources.is_general_relatedness(),true);
					slogger_.info("Done with graph construction");
					/*document.setExperiement_id("Filtered:Full Graph Candidates");
					Solver.doRandomWalk_fromCandidates(document, i);
					slogger_.info("Done with RWR from candidates");
					Solver.writeResults(document, Resources.getResources().getHtml_results_path() + "/cands/", i);
					document.writeResultstoDB();
					document.getTable(i).writeResultstoDB();
					document.deleteResults(); */
					document.setExperiement_id("Filtered:Full Graph Mention");
					Solver.doRandomWalk_fromMentions(document, i);
					slogger_.info("Done with RWR from mentions");
					Solver.writeResults(document, Resources.getResources().getHtml_results_path() + "/mentions/", i);
					document.writeResultstoDB();
					document.getTable(i).writeResultstoDB();
					slogger_.info("Done!!!!");
					end_time2 = System.nanoTime();
					slogger_.info(
							"It took me : " + ((end_time2 - start_time2) / 1000000) + " ms to process this document!");
				}

			} catch (Exception exc) {
				exc.printStackTrace();
			}
		}
//		FileUtils.writeListToFile(selected, "/GW/D5data/yibrahim/misc/data/selected_general3.txt");
//		
		end_time = System.nanoTime();
		slogger_.info("It took me : " + ((end_time - start_time) / 1000000) + " ms to process these documents!");
		/*try {
			FileUtils.writeListToFile(list_files, "wiki_tables_extra.txt");
		} catch (IOException e) {
			slogger_.error(e.getMessage());
			e.printStackTrace();
		}*/

	}

	private static Options createCommandLineOptions() {

		options.addOption("h", "help", false, "show help.");
		options.addOption("i", "input file name", true, "json input file");
		options.addOption("f", "Full Graph Mode", true, "true or false");
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
/*
 * public static void main_old(String arg[]) {
 * 
 * 
 * final CommandLineParser parser = new BasicParser(); long start_time,
 * end_time, start_time2, end_time2; CommandLine cmd; Options options =
 * createCommandLineOptions(); HashMap<String, Document_> documents = new
 * HashMap<String, Document_>(); // use HashTable for Thread safe execution
 * ResourcesLoader resources_loader = new ResourcesLoader(); try { start_time =
 * System.nanoTime(); Resources resources = resources_loader.load(); cmd =
 * parser.parse(options, arg); if (cmd.hasOption("h")) help(); if
 * (!cmd.hasOption("i")) { slogger_.info("input folder name is required");
 * return; } if (!cmd.hasOption("f")) { slogger_.info(
 * "please enter full mode: "); return; } DocumentLoader document_loader =
 * DocumentLoader.getLoader();
 * document_loader.load((String)cmd.getOptionValue("i"), documents); boolean
 * full = Boolean.valueOf((String)cmd.getOptionValue("f")); slogger_.info(
 * "loaded :" +documents.size()); Annotator annotator = new
 * Annotator(Annotator.TYPE.plus, resources); MentionsFilter filter = new
 * MentionsFilter(); for(Document_ document: documents.values()){
 * if(!document.isWikipediaPage()) continue;
 * if(!document.getTable(0).isInteresting()) continue;// first check
 * rows/columns and qunatities start_time2 = System.nanoTime();
 * slogger_.info("processing document: " + document.getFile_name() +
 * ", url: "+ document.getId()); annotator.annotate(document);
 * slogger_.info("Done with annotaions");
 * CandidatesSearch.findCandidates(document); slogger_.info(
 * "Done with candidates selection"); filter.filterTableMentions(document);
 * slogger_.info("Done with candidates filtering"); for(int i =0; i <
 * document.getTables().size(); i++){
 * if(!document.getTable(i).isInteresting())// second check entities and
 * annotaions continue; Node.reset_counter(); Edge.reset_counter();
 * 
 * document.deleteResults(); GraphBuilder.buildGraph(document,i,full,
 * resources.is_general_relatedness()); slogger_.info(
 * "Done with graph construction");
 * Solver.doRandomWalk_fromCandidates(document,i); slogger_.info(
 * "Done with RWR from candidates"); Solver.writeResults(document,
 * Resources.getResources().getHtml_results_path()+"/cands/",i);
 * document.deleteResults(); Solver.doRandomWalk_fromMentions(document, i);
 * slogger_.info("Done with RWR from mentions");
 * Solver.writeResults(document,
 * Resources.getResources().getHtml_results_path()+"/mentions/",i);
 * slogger_.info("Done!!!!"); end_time2 = System.nanoTime();
 * slogger_.info("It took me : " + ((end_time2-start_time2)/1000000) +
 * " ms to process this document!"); } } end_time = System.nanoTime();
 * slogger_.info("It took me : " + ((end_time-start_time)/1000000) +
 * " ms to process these documents!"); } catch (Exception exc) {
 * exc.printStackTrace(); } }
 */