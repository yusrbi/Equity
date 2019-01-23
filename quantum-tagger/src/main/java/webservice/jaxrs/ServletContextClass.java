package webservice.jaxrs;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.core.Context;

import annotators.Annotator;
import knowledgebase.MentionsFilter;
import loader.DocumentLoader;
import resources.Resources;
import resources.ResourcesLoader;

public class ServletContextClass implements ServletContextListener{

	static Resources resources = null;
	static MentionsFilter filter = null;
	static DocumentLoader documentLoader ;
	static Annotator annotator ;
	static ResourcesLoader resources_loader = null;
	

	
	public static Resources getResources() {
		return resources;
	}

	public static void setResources(Resources resources) {
		ServletContextClass.resources = resources;
	}

	public static MentionsFilter getFilter() {
		return filter;
	}

	public static void setFilter(MentionsFilter filter) {
		ServletContextClass.filter = filter;
	}

	public static DocumentLoader getDocumentLoader() {
		return documentLoader;
	}

	public static void setDocumentLoader(DocumentLoader documentLoader) {
		ServletContextClass.documentLoader = documentLoader;
	}

	public static Annotator getAnnotator() {
		return annotator;
	}

	public static void setAnnotator(Annotator annotator) {
		ServletContextClass.annotator = annotator;
	}

	public static ResourcesLoader getResources_loader() {
		return resources_loader;
	}

	public static void setResources_loader(ResourcesLoader resources_loader) {
		ServletContextClass.resources_loader = resources_loader;
	}

	
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		String resources_path =  event.getServletContext().getRealPath("/resources")+"/";
		System.out.println("resources Path is : "+resources_path);
		resources_loader = new ResourcesLoader();		
		filter = new MentionsFilter();		
		
		try {
			resources = resources_loader.load(resources_path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		documentLoader = DocumentLoader.getLoader(resources);
		annotator = new Annotator(resources);
		System.out.println("Classes initialized Successfully! :-)");
	}

}
