package ibm.gse.eda.tmp.infrastructure;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ibm.gse.eda.tmp.infrastructure.kafka.KafkaConfiguration;
import ibm.gse.eda.tmp.infrastructure.kafka.MainEventsRunner;

/**
 * Servlet context listener to start the different messaging consumers of 
 * the application. As each consumer acts as an agent, continuously listening to
 * events, we need to start them when the encapsulating app / microservice is successfully
 * started, which is why we have to implement a servlet context listener.
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
@WebListener
public class AgentsInitializer implements ServletContextListener{
    private static final Logger logger = LoggerFactory.getLogger(AgentsInitializer.class);

    private MainEventsRunner mainEventRunner;
    private ExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("@@@ Order Command contextInitialized v0.0.8, start agents");
        executor = Executors.newFixedThreadPool(2);
        mainEventRunner = new MainEventsRunner();
        // ADD here any other consumer runner 
        executor.execute(mainEventRunner);
    }

   
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info(" context destroyed");
        mainEventRunner.stop();
        // ADD here any runner.stop() call to nicely close consumers
        executor.shutdownNow();
        try {
            executor.awaitTermination(KafkaConfiguration.TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            logger.warn("awaitTermination( interrupted", ie);
        }
    }

}