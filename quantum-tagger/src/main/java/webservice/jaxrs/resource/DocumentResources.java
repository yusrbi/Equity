package webservice.jaxrs.resource;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import executer.Equity;
import webservice.model.Document;

@Path("/document")
public class DocumentResources {

	@GET
    @Path("/ping")
    public String getServerTime() {
        System.out.println("RESTful Service 'MessageService' is running ==> ping");
        return "received ping on "+new Date().toString();
    }
    
   /* @GET
    @Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public Document getAllMessages() throws Exception{
    
    }*/
	
//	@POST
//	@Consumes({ MediaType.APPLICATION_JSON })
//	@Produces({ MediaType.TEXT_PLAIN })
//	@Path("/post")
//	public String postDocument(Document document) throws Exception {
//		System.out.println("Title = " + document.getTitle());
//		System.out.println("Content = " + document.getContent());
//
//		return "ok";
//	}
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("/disambiguate")
	public Document disambiguateDocument(Document document) {
		System.out.println("in disambiguate with document");
		Equity equity;
		try {
			equity = Equity.getInstance();
			equity.disambiguate(document);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return document;
	}
}
