package control.smarthome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import control.smarthome.mqtt.ConnectionManagerV2;
import control.smarthome.mqtt.ConnectionManagerV2Exception;
import control.smarthome.mqtt.handlers.*;

public class StartSmarthome {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StartSmarthome.class);
	
	public static void main(String[] args) {
		
		String address = "";
		int port = 0;
		String clientId = "";
		String user = "";
		String password = "";
		
		LOGGER.debug("gathering arguments");
		try {
			/* *
			 * After the build the, you will find a file called SmarthomeMqttClient.bat for calling this application.
			 * It will do so like this, please update your details in it:
			 * 	start java -jar SmarthomeMqttClient-jar-with-dependencies.jar "broker address" "broker port" "client id" "broker user" "broker password"			 * 
			 * */
			address = args[0];
			port = Integer.parseInt(args[1]);
			clientId = args[2];
			user = args[3];
			password = args[4];
		} catch (Exception ex) {
			LOGGER.error("error in arguments, caught exception: " + ex.getMessage());
			System.exit(0);
		}
		
		new StartSmarthome(address, port, clientId, user, password);

	}
	
	public StartSmarthome(String _address, int _port, String _clientId, String _user, String _password) {
		
		LOGGER.info("smarthome mqtt client starting");
		try {
			final ConnectionManagerV2 connection = new ConnectionManagerV2(_address, _port, _clientId, _user, _password); 
			
			Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
				LOGGER.info("smarthome mqtt client shutting down");
				connection.shutdown();
			    LOGGER.info("goodbye");
			}});
			
			/* *
			 * Here you can register an handler to a topic.
			 * Handlers implements the control.smarthome.mqtt.MessageHandlerV2 interface.
			 * There are 4 different handlers registered here, only the first one is actually doing something.
			 * The other 3 just print the messages and use them as an example.
			 * Don't forget configuring the loggers accordingly in src/main/resources/log4j.properties.
			 * */
			connection.setTopicHandler("smarthome/askmypc/action", new AskMyPc());
			connection.setTopicHandler("omg/#", new RFGateway());
			connection.setTopicHandler("multisensor/#", new MultiSensors());
			connection.setTopicHandler("owntracks/#", new Owntracks());
			
		} catch (ConnectionManagerV2Exception ex) {
			LOGGER.error("failed to start, caught exception: " + ex.getMessage());
			ex.printStackTrace();
			System.exit(0);
		}
		LOGGER.info("smarthome mqtt client started successfully");
	}
}
