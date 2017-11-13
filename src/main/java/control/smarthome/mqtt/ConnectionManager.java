package control.smarthome.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager implements MqttCallback{
	
	private static MqttClient pub_client;
	private static MqttClient sub_client;
	private static MqttConnectOptions conOpt;
	private static MessageHandler msgHandler;
	private static String brokerUrl;
	private static String topic_prefix = "smarthome/mqtt_client/";
	private static String pub_topic = topic_prefix + "status_update";
	private static int pub_qos = 0;
	private static String pub_clientId = "smarthome_client";
	private static String request_update_payload = "request_status_update";
	private static int sub_qos = 0;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManager.class);
	
	public ConnectionManager (String _broker, int _port, String _clientId, String _user, String _password, MessageHandler _msgHandler) throws ConnectionManagerException {

		msgHandler = _msgHandler;
		brokerUrl = "tcp://"+_broker+":"+_port;
		
		try {
			
			
			conOpt = new MqttConnectOptions();
			conOpt.setCleanSession(true);
			conOpt.setPassword(_password.toCharArray());
			conOpt.setUserName(_user);
			conOpt.setKeepAliveInterval(30);
			conOpt.setConnectionTimeout(30);

			MemoryPersistence persistence = new MemoryPersistence();
			
			pub_client = new MqttClient(brokerUrl, pub_clientId);
			sub_client = new MqttClient(brokerUrl, _clientId, persistence);
			sub_client.setCallback(this);
			
			LOGGER.info("client configuration completed");
		
		} catch(MqttException ex) {
			LOGGER.error("mqtt client configuration failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerException("mqtt client configuration failed", ex);
		} catch (Exception ex) {
			LOGGER.error("mqtt client configuration failed, caught exception: " + ex.getMessage());
			throw new ConnectionManagerException("mqtt client configuration failed", ex);
		}
	}
	
	public void subscribe() throws ConnectionManagerException {
		try {
			sub_client.subscribe(topic_prefix + "#", sub_qos);
			LOGGER.info("subscribed to topic " + topic_prefix + "#");
		} catch (MqttException ex) {
			LOGGER.error("client subscribe to " + topic_prefix + "# failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerException("client subscribe to " + topic_prefix + "# failed", ex);
		}
		
	}
	
	private void publish(String _payload) throws ConnectionManagerException{
		
		try {
			pub_client.connect(conOpt);
			MqttMessage message = new MqttMessage(_payload.getBytes());
			message.setQos(pub_qos);
			message.setRetained(true);
			pub_client.publish(pub_topic, message);
			pub_client.disconnect();
			LOGGER.info("published " + _payload + " message to topic " + pub_topic);
		} catch (MqttPersistenceException ex) {
			LOGGER.error("client publish of payload " + _payload + " to topic " + pub_topic + " failed, caught persistence exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerException("client publish of payload " + _payload + " to topic " + pub_topic + " failed", ex);
		} catch (MqttException ex) {
			LOGGER.error("client publish of payload " + _payload + " to topic " + pub_topic  + " failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerException("client failed to publish, exception caught", ex);
		}
	}
	
	public void connect() throws ConnectionManagerException{
		try {
			sub_client.connect(conOpt);
			LOGGER.info("client connected");
			publish("Online");
		} catch (MqttSecurityException ex) {
			LOGGER.error("client connect failed, caught security exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerException("client connection failed", ex);
		} catch (MqttException ex) {
			LOGGER.error("client connect failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerException("client connection failed", ex);
		}
		
	}
	
	public void disconnect() throws ConnectionManagerException {
		
		try {
			publish("Offline");
			sub_client.disconnect();
			sub_client.close();
			pub_client.close();
			LOGGER.info("client disconnected");
		} catch (MqttException ex) {
			LOGGER.error("client disconnect failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerException("client disconnect failed", ex);
		}
	}

	public void connectionLost(Throwable _cause){
		try {
			LOGGER.error("connection lost, exception message: " + _cause.getMessage());
			publish("Offline");
		} catch (ConnectionManagerException ex) {
		}
		LOGGER.info("handing control over to handler");
		msgHandler.onConnectionLost(_cause);
		
	}

	public void messageArrived(String _topic, MqttMessage _msg) throws ConnectionManagerException {
		try {
			String payload = new String(_msg.getPayload());
			if (request_update_payload.equals(payload) ) {
				LOGGER.info("recieved status update request");
				publish("Online");
			} else if (!pub_topic.equals(_topic)) {
				LOGGER.info("recieved message handing control over to handler");
				msgHandler.onMessageRecieved(_topic, payload, _msg.getQos());
			}	
		} catch (Exception ex) {
			LOGGER.error("payload handling on incoming message failed, caught exception: " + ex.getMessage());
			throw new ConnectionManagerException("payload handling on incoming message failed", ex);
		}
				
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		/*String[] deliverdTopics = token.getTopics();
		for (int i = 0; i < deliverdTopics.length; i++ ){
			if (token.isComplete()) {
				LOGGER.info("message delivery succeeded for topic "+ deliverdTopics[i]);
			} else {
				LOGGER.info("message delivery failed for topic "+ deliverdTopics[i]);
			}			
		}*/
	}
}
