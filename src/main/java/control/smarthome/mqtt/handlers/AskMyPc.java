package control.smarthome.mqtt.handlers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import control.smarthome.mqtt.MessageHandlerV2;

public class AskMyPc implements MessageHandlerV2{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AskMyPc.class);
	
	public void onMessageRecieved(String _topic, String _payload, int _qos, boolean _retain) {
		getAction(_payload);		
	}
	
    private void getAction (String _actionName){
    	try {
    		LOGGER.info("looking for corresponding action for " + _actionName);
    		File directory = (new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath())).getParentFile();
     		File jsonFile = new File(directory,"action_map.json");
    		
    		String jsonText = IOUtils.toString(new FileInputStream(jsonFile));
    		JSONObject jsonObj = new JSONObject(jsonText);
    		
    		try {
    			Object obj = jsonObj.get(_actionName);
    			
    			if (obj instanceof JSONArray){
    				JSONArray jarray = (JSONArray)obj;
    				for (int i = 0; i < jarray.length(); i++) {
    					startAction(_actionName, (String)jarray.get(i));
    				}
    			
    			} else if (obj instanceof JSONObject) {
    				JSONObject jobject = (JSONObject)obj;
    				for (Iterator<String> jiterator = jobject.keySet().iterator(); jiterator.hasNext();) {
    					startAction(_actionName, (String)jobject.get((String) jiterator.next()));
    				}
    			
    			} else {
    				startAction(_actionName, (String) obj);
    			}
    		} catch (JSONException ex) {
    			LOGGER.warn("action for "+_actionName+" not found");
    		}
    	
    	} catch (Exception ex) {
    		LOGGER.error("failed to launch corresponding action, exception caught: " + ex.getMessage());
    	}
    }
    
    private void startAction (String _actionName, String _actionValue) throws URISyntaxException, IOException{
    	
		if (_actionValue != null && !_actionValue.isEmpty()) {
	    	try {
				Desktop.getDesktop().open(new File(_actionValue));
				LOGGER.info("started " + _actionValue + " as desktop application");    
			} catch (IllegalArgumentException ex) {
				Desktop.getDesktop().browse(new URI(_actionValue));
				LOGGER.info("started " + _actionValue + " with default web browser");    
			}   

		} else {
			LOGGER.warn("action for "+_actionName+" not found");
		}
    }
}
