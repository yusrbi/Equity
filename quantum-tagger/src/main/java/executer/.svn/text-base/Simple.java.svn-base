package executer;

import java.util.HashMap;

import loader.DocumentLoader;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import resources.Resources;
import resources.ResourcesLoader;
import annotators.Annotator;
import data.Document_;

public class Simple {
	final static Options options = new Options();
	final static int nPages = 10;

	private static Logger slogger_ = LoggerFactory.getLogger(Simple.class);
	public static void main(String arg[]) {

		final CommandLineParser parser = new BasicParser();
		CommandLine cmd;
		Options options = createCommandLineOptions();
		HashMap<String, Document_> documents = new HashMap<String, Document_>(); // use HashTable for Thread safe execution
		ResourcesLoader resources_loader = new ResourcesLoader();
		try {
			Resources resources = resources_loader.load();
			cmd = parser.parse(options, arg);
			if (cmd.hasOption("h"))
				help();
			if (!cmd.hasOption("i")) {
				slogger_.info("input file name is required");
				return;
			}
			DocumentLoader document_loader = DocumentLoader.getLoader(resources);
			Document_ document = document_loader.load(cmd.getOptionValue("i"),"");//TODO : FIXME
			if (documents.containsKey(document.getId())){// collisions
				documents.get(document.getId()).merge(document);
				
			}else
				documents.put(document.getId(), document); 
														
			Annotator annotator = new Annotator( resources);
			annotator.annotate(document);
			annotator.printAnnotations(document, "all");
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	private static Options createCommandLineOptions() {

		options.addOption("h", "help", false, "show help.");
		options.addOption("i", "input file name", true, "json input file");
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
