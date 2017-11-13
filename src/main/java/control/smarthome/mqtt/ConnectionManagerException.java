package control.smarthome.mqtt;

public class ConnectionManagerException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConnectionManagerException(String _message) {
		super(_message);
	}

	public ConnectionManagerException(Throwable _cause) {
		super(_cause);
	}

	public ConnectionManagerException(String _message, Throwable _cause) {
		super(_message, _cause);
	}

}
