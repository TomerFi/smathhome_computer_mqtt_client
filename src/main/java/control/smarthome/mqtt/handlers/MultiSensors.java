package control.smarthome.mqtt.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import control.smarthome.mqtt.MessageHandlerV2;

public class MultiSensors implements MessageHandlerV2{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MultiSensors.class);

	public void onMessageRecieved(String _topic, String _payload, int _qos, boolean _retained) {
		LOGGER.debug("topic: " + _topic);
		LOGGER.debug("payload: " + _payload);
		LOGGER.debug("qos: " + String.valueOf(_qos));
		LOGGER.debug("retain: " + String.valueOf(_retained));		
	}

}
