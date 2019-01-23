package webservice.jaxrs.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import webservice.model.Quantity;

@Path("/QKB")
public class QuantityResources {

	 @GET
	 @Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well 
	 //(don't forget @XmlRootElement)
	 @Path("class/{class_name}/value/{value}")
	 public Quantity getQuantity(@PathParam("class_name") String class_name, 
			 @PathParam("value") String value) throws Exception{
	    Quantity quantity= new Quantity();
	    quantity.setClass_(class_name);
	    quantity.setMagnitude(value);
	
		 return quantity;
	 }
	 
	 @GET
	 @Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well 
	 //(don't forget @XmlRootElement)
	 @Path("class/{class_name}/measure/{measure_name}/value/{value}")
	 public Quantity getQuantity(@PathParam("class_name") String class_name, 
			 @PathParam("value") String value,
			 @PathParam("measure_name") String measure_name) throws Exception{
		Quantity quantity = new Quantity();
		quantity.setClass_(class_name);
		quantity.setMeasure(measure_name);
		quantity.setMagnitude(value);
	
		 return quantity;
	 }
	 
	 
	 @GET
	 @Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well 
	 //(don't forget @XmlRootElement)
	 @Path("class/{class_name}/measure/{measure_name}/unit/{unit_name}/value/{value}")
	 public Quantity getQuantity(@PathParam("class_name") String class_name, 
			 @PathParam("value") String value,
			 @PathParam("measure_name") String measure_name,
			 @PathParam("unit_name") String unit_name) throws Exception{
		Quantity quantity = new Quantity();
		quantity.setClass_(class_name);
		quantity.setMeasure(measure_name);
		quantity.setUnit(unit_name);
		quantity.setMagnitude(value);
	
		 return quantity;
	 }
}
