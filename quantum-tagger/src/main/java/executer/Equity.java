package executer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import annotators.Annotator;
import data.Document_;
import executer.Hyperparams.EXPERIMENT_ID;
import executer.Hyperparams.SETTINGS_ID;
import graph.Edge;
import graph.GraphBuilder;
import graph.Node;
import graph.Solver;
import knowledgebase.CandidatesSearch;
import knowledgebase.MentionsFilter;
import loader.DocumentLoader;
import resources.Resources;
import resources.ResourcesLoader;
import webservice.jaxrs.ServletContextClass;
import webservice.model.Document;

public class Equity {
	private Experiment experiment;
	private static Logger slogger_ = LoggerFactory.getLogger(DocumentLoader.class);
	private static Equity equity= null;
	Resources resources = null;
	MentionsFilter filter = null;
	DocumentLoader documentLoader ;
	Annotator annotator ;
	ResourcesLoader resources_loader = null;
	
	private  Equity() throws IOException{
		resources = ServletContextClass.getResources();
		filter = ServletContextClass.getFilter();
		documentLoader = ServletContextClass.getDocumentLoader();
		annotator = ServletContextClass.getAnnotator();
		resources_loader = ServletContextClass.getResources_loader();
	}

	public void disambiguate(Document doc) throws ClassCastException, ClassNotFoundException, IOException, SQLException{
		
		Node.reset_counter();
		Edge.reset_counter();
		experiment = new Experiment(SETTINGS_ID.DEFAULT, EXPERIMENT_ID.part_src, "part_src");		
		Document_ document = documentLoader.load(doc);
		document.setFile_name(doc.getTitle());
		slogger_.info("processing document: " + document.getFile_name() + ", url: " + document.getId());
		annotator.annotate(document);
		slogger_.info("Done with annotations");
		CandidatesSearch.findCandidates(document);
		slogger_.info("Done with candidates selection");
		filter.filterTableMentions(document);
		slogger_.info("Done with candidates filtering");
		boolean initDB=true;
		resources_loader.loadSettings(doc);		
		document.setExperiement_id(experiment.id);
		GraphBuilder.buildGraph(document, 0, experiment.isFull(), resources.is_general_relatedness(), initDB);
		slogger_.info("Done with graph construction for:" + experiment.id);
		if(experiment.isSource()){
			Solver.doRandomWalk_fromMentions(document, 0);
			slogger_.info("Done with RWR from mentions");
		}
		else{
			Solver.doRandomWalk_fromCandidates(document, 0);
			slogger_.info("Done with RWR from candidates");
		}		
		document.writeHTMLResultstoPOJO(doc);
		document.writeAnnotationstoPOJO(doc);		
		document.deleteResults();
		
	}

	public static Equity getInstance() throws IOException {
		if(equity == null)
			equity = new Equity();
		return equity;
	}
}
