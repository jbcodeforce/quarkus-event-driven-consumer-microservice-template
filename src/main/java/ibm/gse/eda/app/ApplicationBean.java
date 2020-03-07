package ibm.gse.eda.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import ibm.gse.eda.app.infrastructure.kafka.KafkaConfiguration;
import ibm.gse.eda.app.infrastructure.kafka.MainEventsRunner;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * As each consumer acts as an agent, continuously listening to
 * events, we need to start them when the encapsulating app / microservice is successfully
 * started.
 *  
 * This class needs to start the business event agent.
 * 
 *  When the application stops we need to kill the consumers.
 *  
 *  This class use java.util.concurrent.Executor to better work with Thread.
 *  
 * @author jerome boyer
 *
 */
@ApplicationScoped
public class ApplicationBean {
	   private static final Logger LOGGER = Logger.getLogger("ApplicationBean"); 
	   
	   @Inject
	   @ConfigProperty(name="app.version")
	   String version;
	   
	   @Inject
	   public KafkaConfiguration kafkaConfiguration;
	   
	   private MainEventsRunner mainEventRunner;
	    
	   private ExecutorService executorService;

	   public void onStart(@Observes StartupEvent ev) {               
	    	LOGGER.info("The application v" + version + " is starting...");
	    	// With ExecutorService we can start a kafka consumer thread,
	        // but we need to inject configuration data instantiated by the main thread into
	    	// the new thread 
	        executorService = Executors.newFixedThreadPool(1);
	        mainEventRunner = new MainEventsRunner(kafkaConfiguration);
	        // ADD here any other consumer runner 
	        executorService.execute(mainEventRunner);
	    }

	    public void onStop(@Observes ShutdownEvent ev) {               
	    	LOGGER.info("The application is stopping...");
	    	mainEventRunner.stop();
	        // ADD here any runner.stop() call to nicely close consumers
	        executorService.shutdownNow();
	        try {
	            executorService.awaitTermination(KafkaConfiguration.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
	        } catch (InterruptedException ie) {
	        	LOGGER.warn("awaitTermination( interrupted", ie);
	        }
	    }

}
