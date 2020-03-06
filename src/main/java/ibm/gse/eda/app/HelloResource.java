package ibm.gse.eda.app;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Path("/hello")
public class HelloResource {
	
	 private static final Logger LOGGER = Logger.getLogger("HelloResource"); 
	@Inject
	@ConfigProperty(name="message")
	private String message;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
    	LOGGER.debug("In hello GET resource");
        return message;
    }
}