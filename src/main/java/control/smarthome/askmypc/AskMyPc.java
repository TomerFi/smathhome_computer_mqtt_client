package control.smarthome.askmypc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import control.smarthome.mqtt.ConnectionManager;
import control.smarthome.mqtt.ConnectionManagerException;

public class AskMyPc {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AskMyPc.class);

	public static void main(String[] args) {
		
		
		String clientId = "askmypc_client" ;
		String brokerAddress = null;
		int brokerPort = 0;
		String brokerUser = null;
		String brokerPassword = null;
	
		try {
			brokerAddress = args[0];
			brokerPort = Integer.parseInt(args[1]);
			brokerUser = args[2];
			brokerPassword = args[3];
			
		} catch(Exception ex) {
			LOGGER.error("arguments failure, please follow pass the following."+ System.getProperty("line.seperator") +
					"mandatory arguments are: broker_address, broker_port, broker_user, broker_password");
			System.exit(0);
		}
		
		try {
			final ConnectionManager connection = new ConnectionManager(brokerAddress, brokerPort, clientId, brokerUser, brokerPassword, new HandleActions());
			
			Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){

				try {
					connection.disconnect();
			    	LOGGER.info("disconnected from broker");
			    } catch (ConnectionManagerException ex) {
					LOGGER.error("failed to disconnect from broker, caught exception: " + ex.getMessage());
				}
			    LOGGER.info("goodbye");
			}});
			
			connection.connect();
			LOGGER.info("connected to broker " + brokerAddress);
			LOGGER.info("subscribing for topic");
			connection.subscribe();
			LOGGER.info("waiting for messages");
			
		} catch (ConnectionManagerException ex) {
			LOGGER.error(ex.getMessage(),ex);
			System.exit(0);
		}
	}

}
