package control.smarthome.mqtt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManagerV2{
	
	private static MqttAsyncClient client;
	private static MqttConnectOptions connOptions;
	private static HashMap<ArrayList<String>, MessageHandlerV2> topicsToHandlers = new HashMap<ArrayList<String>, MessageHandlerV2>();
	
	private static final String STATUS_UPDATE_TOPIC = "smarthome/mqtt_client/status_update" ;
	private static final String REQUEST_STATUS_UPDATE_TOPIC = "smarthome/mqtt_client/request_status_update";
	private static final String CLIENT_ONLINE_PAYLOAD = "Online";
	private static final String CLIENT_OFFLINE_PAYLOAD = "Offline";
	
	private static final int DEFAULT_KEEP_ALIVE = 60;
	private static final int DEFAULT_TIMEOUT = 30;
	private static final int DEFAULT_QOS = 0;
	private static final boolean DEFAULT_RETAINED = false;
	private static final int DEFAULT_RECONNECT_RETRIES = 10;
	private static final int DEFAULT_RECONNECT_DELAY = 5;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionManagerV2.class);
	
	public ConnectionManagerV2 (String _broker, int _port, String _clientId, String _user, String _password) throws ConnectionManagerV2Exception{
		this(_broker, _port, _clientId, _user, _password, DEFAULT_KEEP_ALIVE, DEFAULT_TIMEOUT);
	}
	
	public ConnectionManagerV2 (String _broker, int _port, String _clientId, String _user, String _password, int _keepAlive, int _timeout) throws ConnectionManagerV2Exception{
		String brokerUrl = "tcp://"+_broker+":"+_port;
		
		connOptions = new MqttConnectOptions();
		
		connOptions.setCleanSession(true);
		connOptions.setPassword(_password.toCharArray());
		connOptions.setUserName(_user);
		connOptions.setKeepAliveInterval(_keepAlive);
		connOptions.setConnectionTimeout(_timeout);
		
		try {
			LOGGER.debug("creating mqtt client");
			client = new MqttAsyncClient(brokerUrl,_clientId, new MemoryPersistence());
			client.setCallback(new ConnectionManagerV2Callback());

			try {
				connect();
			} catch (Exception ex) {
				reconnect(DEFAULT_RECONNECT_DELAY, DEFAULT_RECONNECT_RETRIES);
			}
		
		} catch (MqttSecurityException ex) {
			LOGGER.error("subscription failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerV2Exception("subscription failed with mqtt security exception", ex);
		} catch (MqttException ex) {
			LOGGER.error("subscription failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerV2Exception("subscription failed with mqtt exception", ex);
		}
	}
	
	public void setTopicHandler(String _topic, MessageHandlerV2 _callback) throws ConnectionManagerV2Exception {
		setTopicHandler(new ArrayList<String>(Arrays.asList(_topic)), _callback);
	}
	
	public void setTopicHandler(ArrayList<String> _topics, MessageHandlerV2 _callback) throws ConnectionManagerV2Exception {
		topicsToHandlers.put(_topics, _callback);
		for (String topic : _topics) {
			subscribeToTopic(topic);
		}
	}
	
	public void subscribeToTopic(String _topic) throws ConnectionManagerV2Exception {
		subscribeToTopic(_topic, DEFAULT_QOS);
	}
	
	public void subscribeToTopic(String _topic, int _qos) throws ConnectionManagerV2Exception {
		
		try {
			IMqttToken sToken = client.subscribe(_topic, _qos);
			LOGGER.info("subscribed to " + _topic);
			LOGGER.debug("MSG-" + sToken.getMessageId() + ": subscribed to " + _topic + " with qos " + _qos);
		} catch(MqttException ex) {
			LOGGER.error("subscription failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerV2Exception("subscription failed with mqtt exception", ex);
		} catch (Exception ex) {
			LOGGER.error("subscription failed, caught exception: " + ex.getMessage());
			throw new ConnectionManagerV2Exception("subscription failed", ex);
		}
	}
	
	public void publishPayload(String _topic, String _payload) throws ConnectionManagerV2Exception {
		publishPayload(_topic, _payload, DEFAULT_QOS, DEFAULT_RETAINED);
	}

	public void publishPayload(String _topic, String _payload, int _qos) throws ConnectionManagerV2Exception {
		publishPayload(_topic, _payload, _qos, DEFAULT_RETAINED);
	}
	
	public void publishPayload(String _topic, String _payload, int _qos, boolean _retained) throws ConnectionManagerV2Exception{
		
		try {
			MqttMessage message = new MqttMessage(_payload.getBytes());
			message.setQos(_qos);
			message.setRetained(_retained);
			
			IMqttDeliveryToken dToken = client.publish(_topic, message);
			
			LOGGER.info("published " + _payload + " to " + _topic);
			LOGGER.debug("MSG-" + dToken.getMessageId() + ": published " + _payload + " to " + _topic + " with qos " + _qos + " and retain " + _retained);
		
		} catch (MqttPersistenceException ex) {
			LOGGER.error("client publish of payload " + _payload + " to topic " + _topic + " failed, caught persistence exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerV2Exception("client publish of payload " + _payload + " to topic " + _topic + " failed", ex);
		} catch (MqttException ex) {
			LOGGER.error("client publish of payload " + _payload + " to topic " + _topic  + " failed, caught mqtt exception, reason code is: " + ex.getReasonCode());
			throw new ConnectionManagerV2Exception("client failed to publish, exception caught", ex);
		}
	}
	
	private void connect() throws MqttSecurityException, MqttException, ConnectionManagerV2Exception {
		IMqttToken cToken = client.connect(connOptions);
		cToken.waitForCompletion();
		LOGGER.info("client connected");
		LOGGER.debug("MSG-" + cToken.getMessageId() + ": mqtt client connected");
		
		publishPayload(STATUS_UPDATE_TOPIC, CLIENT_ONLINE_PAYLOAD);
		//publishPayload("multisensor/livingroomnode", "", 0, true);
		subscribeToTopic(REQUEST_STATUS_UPDATE_TOPIC);
	}
	
	public void shutdown() {
		try {
			LOGGER.debug("shutting down");
			publishPayload(STATUS_UPDATE_TOPIC, CLIENT_OFFLINE_PAYLOAD);
	    } catch (ConnectionManagerV2Exception ex) {
		} finally{
			try {
				if (client.isConnected()) {
					client.disconnect();
				}
			} catch (MqttException ex) {
				LOGGER.error("failed to disconnect from broker, caught exception: " + ex.getMessage());
			}
			LOGGER.debug("client diconnected");
		}				
	}
	
	private void reconnect(int _delay, int _retries){
		if (_retries > 0) {
			LOGGER.warn(_retries + " reconnect attempts left");
			try {
				connect();
			} catch (Exception ex) {
				LOGGER.warn("reconnect failed, caught exception: " + ex.getMessage());
				LOGGER.warn("sleeping for " + _delay + " seconds");
				try {
					Thread.sleep(_delay * 1000);
				} catch (InterruptedException e) {
				}
				reconnect(_delay * 2, _retries -1);
			}
			Iterator<ArrayList<String>> it = topicsToHandlers.keySet().iterator();
			while (it.hasNext()) {
				for (String topic : it.next()) {
					try {
						subscribeToTopic(topic);
					} catch (ConnectionManagerV2Exception e) {
						LOGGER.warn("failed to subscribe to " + topic);
					}
				}
			}
			LOGGER.info("reconnected succesfully");
		}
	}
	
	private class ConnectionManagerV2Callback implements MqttCallback {

		public void connectionLost(Throwable _cause) {
			LOGGER.warn("ConnectionManagerV2Callback recieved connection lost cause: " + _cause.getMessage());
			try {
				publishPayload(STATUS_UPDATE_TOPIC, CLIENT_OFFLINE_PAYLOAD);
			} catch (ConnectionManagerV2Exception ex) {
			}
			reconnect(DEFAULT_RECONNECT_DELAY, DEFAULT_RECONNECT_RETRIES);
		}

		public void messageArrived(String _topic, MqttMessage _message) throws Exception {
			LOGGER.info("recieved message for " + _topic);
			LOGGER.debug("recieved message " + new String(_message.getPayload()) + " to " + _topic + " with qos " + _message.getQos() + " and retain " + String.valueOf(_message.isRetained()));
			if (_topic.equals(REQUEST_STATUS_UPDATE_TOPIC)) {
				publishPayload(STATUS_UPDATE_TOPIC, CLIENT_ONLINE_PAYLOAD);
				return;
			}
			Iterator<?> it = topicsToHandlers.entrySet().iterator();
			while (it.hasNext()) {
				@SuppressWarnings("unchecked")
				Map.Entry<ArrayList<String>, MessageHandlerV2> entry = (Map.Entry<ArrayList<String>, MessageHandlerV2>)it.next();
				for (String topic : entry.getKey()) {
					if (_topic.equals(topic)) {
						LOGGER.info("handing over message to " + entry.getValue().getClass().getName());
						entry.getValue().onMessageRecieved(topic, new String(_message.getPayload()), _message.getQos(), _message.isRetained());
					}
					else if (topic.contains("#")) {
						String newTopic = topic.substring(0, topic.indexOf("#"));
						if (_topic.substring(0, newTopic.length()).equals(newTopic)) {
							LOGGER.info("handing over message to " + entry.getValue().getClass().getName());
							entry.getValue().onMessageRecieved(_topic, new String(_message.getPayload()), _message.getQos(), _message.isRetained());
						}
					}
				}
			}
		}

		public void deliveryComplete(IMqttDeliveryToken _token) {
			LOGGER.debug("MSG-" + _token.getMessageId() + " delivery completed");
		}

	}
}

