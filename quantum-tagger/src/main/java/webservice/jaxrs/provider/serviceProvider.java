package webservice.jaxrs.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
 
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
* Jackson JSON processor could be controlled via providing a custom Jackson ObjectMapper instance. 
* This could be handy if you need to redefine the default Jackson behavior and to fine-tune how 
* your JSON data structures look like (copied from Jersey web site). * 
* @see https://jersey.java.net/documentation/latest/media.html#d0e4799
*/

@Provider
//@Produces({MediaType.APPLICATION_JSON})
//@Consumes(MediaType.APPLICATION_JSON)
//@Singleton
public class serviceProvider implements ContextResolver<ObjectMapper> {
   
   private static final ObjectMapper MAPPER = new ObjectMapper();
   
   static {
     MAPPER.setSerializationInclusion(Include.NON_EMPTY);
     MAPPER.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
     //MAPPER.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
   }

   public serviceProvider() {
       System.out.println("Instantiate serviceProvider");
   }
    
   @Override
   public ObjectMapper getContext(Class<?> type) {
       System.out.println("serviceProvider.getContext() called with type: "+type);
       return MAPPER;
   } 
}