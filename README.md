# Control Smarthome Windows PC Mqtt Client

A java mqtt client for a windows pc with addons, runs as a jar written in java with maven.</br>
Upon connecting or disconnecting to the mqtt broker, the client publishes a message to the *topic* **smarthome/mqtt_client/status_update** with the *payload* **Online/Offline**.

## AskMyPc Controller

A smarthome control component that subscribes to the *topic* **smarthome/mqtt_client/askmypc_action**.</br>
When the mqtt client recieves a message with this topic, it will hand it over the *AskMyPc* component which will match an action to perform from a designated json file and perform the action on the computer.

Please note, the client actually subscribes to the *topic* **smarthome/mqtt_client/#** for future components support.

### Requirements

- [Java Development Kit (JDK) version 1.7 and above](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven Deployment](https://maven.apache.org/download.cgi)

### Deployment

Build the jar file with maven using the following command within the folder containing the *pom.xml* file:</br>
**mvn clean compile assembly:single**

This action will create a folder called *target* with the build project files, we only need three files, you can delete the rest and the downloaded repository after copying the three files to your selected path:
- *AskMyPc-jar-with-dependencies.jar* is the application code.
- *AskMyPc.bat* is the application activation script, edit the file in any text editor and update your broker's data inside the designated quotation marks.
- *action_map.json* is the mapping file meant to map and action to perform on your computer to a payload recived to the topic.

#### Usage

To start the application, just double click the *AskMyPc.bat*, assuming it's configured properly, it will start the mqtt client and a message will be published with the *payload* **Online** to the *topic* **smarthome/mqtt_client/status_update** notifying the broker with the client status.</br>
The client then will subscribe to the *topic* **smarthome/mqtt_client/#** and will transfer messages with the *topic* **smarthome/mqtt_client/askmypc_action** to the *AskMyPc Controller*.</br>

AskMyPc Controller will get the message and iterate over the *action_map.json* looking for an action to perform based on the recieved *payload* and will try to perform it as a Desktop Application and if it failes, the controller will try to perform the action as a Web Application.

##### Example of usage

