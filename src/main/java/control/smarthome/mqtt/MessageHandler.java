package control.smarthome.mqtt;

public interface MessageHandler {
	
	public void onMessageRecieved (String _topic, String _payload, int _qos);
	
	public void onConnectionLost (Throwable _cause);

}
