package ibm.gse.eda.app.infrastructure.kafka;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;

/**
 * Configuration leverage properties file and environment variables
 * 
 * @author jeromeboyer
 *
 */
@Singleton
public class KafkaConfiguration {
	private static final Logger LOGGER = Logger.getLogger(KafkaConfiguration.class.getName()); 
		
	public static final long PRODUCER_TIMEOUT_SECS = 10;
	public static final long PRODUCER_CLOSE_TIMEOUT_SEC = 10;
	public static final Duration CONSUMER_POLL_TIMEOUT = Duration.ofSeconds(10);
	public static final Duration CONSUMER_CLOSE_TIMEOUT = Duration.ofSeconds(10);
    public static final long TERMINATION_TIMEOUT_SEC = 10;

  
    @ConfigProperty(name = "main.topic.name", defaultValue="orders")
    protected String mainTopicName;
 
    @ConfigProperty(name = "kafka.consumer.groupid", defaultValue="order.consumer.id")
    protected String groupid;
    
    protected Map<String, String> env = System.getenv();
    
    protected String clientId;
    
    public KafkaConfiguration() {}
    
    // To create this bean at startup by listening to startup event
    /*
    void startup(@Observes StartupEvent event) { 
    	getMainTopicName();
    	getConsumerGroupID();
    }
    */
    
    public Properties getConsumerProperties(
    		String clientid, 
    		boolean commit,
    		String offset) {
        Properties properties = buildCommonProperties();
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.toString(commit));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,  this.getConsumerGroupID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, getClientId());
        properties.forEach((k,v)  -> LOGGER.info(k + " : " + v)); 
        return properties;
    }
    
    /**
     * Take into account the environment variables if set
     *
     * @return common kafka properties
     */
    private  Properties buildCommonProperties() {
        Properties properties = new Properties();
      

        if (env.get("KAFKA_BROKERS") == null) {
            throw new IllegalStateException("Missing environment variable KAFKA_BROKERS");
        }
        properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.get("KAFKA_BROKERS"));

    	if (env.get("KAFKA_APIKEY") != null) {
          properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
          properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
          properties.put(SaslConfigs.SASL_JAAS_CONFIG,
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\""
                            + env.get("KAFKA_APIKEY") + "\";");
          properties.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
          properties.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2");
          properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");

          if ("true".equals(env.get("TRUSTSTORE_ENABLED"))){
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, env.get("TRUSTSTORE_PATH"));
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, env.get("TRUSTSTORE_PWD"));
          }
        }
        return properties;
    }

	public String getMainTopicName() {
		LOGGER.info(mainTopicName);
		return mainTopicName;
	}
	

	
	public String getConsumerGroupID() {
		LOGGER.info(groupid);
		return groupid;
	}
	


	public String getClientId() {
		if (clientId == null) {
			clientId="OrderEventsAgent";
		}
		
		return clientId;
	}


}
