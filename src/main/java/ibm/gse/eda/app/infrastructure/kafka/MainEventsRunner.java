package ibm.gse.eda.app.infrastructure.kafka;

import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.Header;
import org.jboss.logging.Logger;

import com.google.gson.Gson;

import ibm.gse.eda.app.infrastructure.events.OrderEvent;

/**
 * Base runnable agent to continuously listen to events on the main topic.
 * It uses the Kafka consumer APIs. 
 *
 */
@ApplicationScoped
public class MainEventsRunner implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(MainEventsRunner.class.getName()); 
	
	
	protected KafkaConsumer<String, String> kafkaConsumer = null;
	private Gson jsonParser = new Gson();
	private boolean running = true;
	
	
	public KafkaConfiguration kafkaConfiguration;
	
	public MainEventsRunner() {
    }
	
	public MainEventsRunner(KafkaConfiguration kafkaConfiguration) {
		this.kafkaConfiguration = kafkaConfiguration;
    }
	
    
    public MainEventsRunner(KafkaConsumer<String, String> kafkaConsumer) {
    	this.kafkaConsumer = kafkaConsumer;
    }
    
    public boolean isRunning() {
    	return running;
    }
    
    
    private void init() {
    	// if we need to have multiple threads then the clientId needs to be different
    	// auto commit is set to true, and read from the last not committed offset
    	Properties properties = getKafkaConfiguration().getConsumerProperties(
          		"OrderEventsAgent",	
          		false,  
          		"earliest" );
    	// ADD any properties on top of the default ones here
    	properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        this.kafkaConsumer = new KafkaConsumer<String, String>(properties);
    	this.kafkaConsumer.subscribe(Collections.singletonList(getKafkaConfiguration().getMainTopicName()));
    }
    
    public KafkaConfiguration getKafkaConfiguration() {
    	if (kafkaConfiguration == null) kafkaConfiguration = new KafkaConfiguration();
    	return kafkaConfiguration;
    }
	
	@Override
	public void run() {
		LOGGER.info("Order event consumer loop thread started");
		init();
		while (this.running) {
			try {
				pollEventsFromKafkaTopic();
			} catch (KafkaException  ke) {
				ke.printStackTrace();
				// Treat a Kafka exception as unrecoverable
				stop();
			}
		}
		stop();
	}

	public void stop() {
		this.running = false;
		try {
			if (kafkaConsumer != null)
				kafkaConsumer.close(KafkaConfiguration.CONSUMER_CLOSE_TIMEOUT);
        } catch (Exception e) {
        	LOGGER.info("Failed closing Consumer " +  e.getMessage());
        }
	}

	
	/**
	 * The poll method returns fetched records based on current partition offset. The poll method is a
	 * blocking method waiting for specified time in seconds. If no records are available after the 
	 * time period specified, the poll method returns an empty ConsumerRecords.
	 * The poll method is not thread safe and is not meant to get called from multiple threads
	 * 
	 * The method commit offset manually
	 * @return
	 */
	private void pollEventsFromKafkaTopic(){
		ConsumerRecords<String, String> records = kafkaConsumer.poll(KafkaConfiguration.CONSUMER_POLL_TIMEOUT);
        for (ConsumerRecord<String, String> record : records) {
        	Iterator<Header> h = record.headers().iterator();
        	while (h.hasNext()) {
        		Header header = h.next();
        		LOGGER.info(header.key() + " " + header.value());
        	}
        			
        	
        	LOGGER.info("Consumer Record - key:" + record.key() + " value:" + record.value() + " partition:" +
                    record.partition() + " offset:" + record.offset() +"\n");
        	OrderEvent event = deserialize(record.value());
            if (handle(event)) {
            	kafkaConsumer.commitAsync();
            	// Or use kafkaConsumer.commitSync();
            };
        }
	}
	
	private OrderEvent deserialize(String eventAsString) {
		OrderEvent oe= null;
		try {
			oe = jsonParser.fromJson(eventAsString, OrderEvent.class);
		} catch (Exception e) {
			// if the message is not serializable hidde the exception return null
		}
		return oe;
	}
	
	private boolean handle(OrderEvent event) {
		if (event == null) return false;
		LOGGER.info("Event to process with business logic: " + event.getPayload().getOrderID());
		// ADD Here business logic that could come time
		return true;
	}
}
