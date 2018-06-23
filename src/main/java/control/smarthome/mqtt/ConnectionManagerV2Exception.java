package control.smarthome.mqtt;

public class ConnectionManagerV2Exception extends Exception {

	private static final long serialVersionUID = 1L;

	public ConnectionManagerV2Exception(String _message) {
		super(_message);
	}

	public ConnectionManagerV2Exception(Throwable _cause) {
		super(_cause);
	}

	public ConnectionManagerV2Exception(String _message, Throwable _cause) {
		super(_message, _cause);
	}

}
