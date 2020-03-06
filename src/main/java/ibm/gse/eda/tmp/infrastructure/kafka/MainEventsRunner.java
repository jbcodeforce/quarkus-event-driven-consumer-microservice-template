package ibm.gse.eda.tmp.infrastructure.kafka;

import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.Header;

import com.google.gson.Gson;

import ibm.gse.eda.app.infrastructure.events.OrderEvent;

/**
 * Base runnable agent to continuously listen to events on the main topic.
 * It uses the Kafka consumer APIs. 
 *
 */
@ApplicationScoped
public class MainEventsRunner implements Runnable {
	private static final Logger logger = Logger.getLogger(MainEventsRunner.class.getName());

	private KafkaConsumer<String, String> kafkaConsumer = null;
	private Gson jsonParser = new Gson();
	private boolean running = true;
	
	@Inject
	private KafkaConfiguration kafkaConfiguration;
	
	public MainEventsRunner() {
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
    	Properties properties = kafkaConfiguration.getConsumerProperties(
          		"OrderEventsAgent",	
          		false,  
          		"earliest" );
    	// ADD any properties on top of the default ones here
    	properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        this.kafkaConsumer = new KafkaConsumer<String, String>(properties);
    	this.kafkaConsumer.subscribe(Collections.singletonList(kafkaConfiguration.getMainTopicName()));
    }
    
	
	@Override
	public void run() {
		logger.info("Order event consumer loop thread started");
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
            logger.warning("Failed closing Consumer " +  e.getMessage());
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
        		logger.info(header.key() + " " + header.value());
        	}
        			
        	
        	logger.info("Consumer Record - key:" + record.key() + " value:" + record.value() + " partition:" +
                    record.partition() + " offset:" + record.offset() +"\n");
        	OrderEvent event = deserialize(record.value());
            if (handle(event)) {
            	kafkaConsumer.commitAsync();
            	// Or use kafkaConsumer.commitSync();
            };
        }
	}
	
	private OrderEvent deserialize(String eventAsString) {
		return jsonParser.fromJson(eventAsString, OrderEvent.class);
	}
	
	private boolean handle(OrderEvent event) {
		logger.info("Event to process with business logic: " + event.getPayload().getOrderID());
		// ADD Here business logic that could come time
		return true;
	}
}
