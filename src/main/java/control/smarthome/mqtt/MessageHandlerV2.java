package control.smarthome.mqtt;

public interface MessageHandlerV2 {
	public void onMessageRecieved (String _topic, String _payload, int _qos, boolean _retained);
}
