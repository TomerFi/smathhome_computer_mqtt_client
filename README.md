# Control Smarthome Windows PC Mqtt Client

![Maintenance](https://img.shields.io/maintenance/no/2020)

A java mqtt client for a windows pc with addons, runs as a jar written in java with maven.</br>
Upon connecting or disconnecting to the mqtt broker, the client publishes a message to the *topic* **smarthome/mqtt_client/status_update** with the *payload* **Online/Offline**.

## AskMyPc Controller
A smarthome control component that subscribes to the *topic* **smarthome/mqtt_client/askmypc_action**.</br>
When the mqtt client recieves a message with this topic, it will hand it over the *AskMyPc* controller which will match an action to perform from a designated json file and perform the action on the computer.

### Requirements
- [Java Development Kit (JDK) version 1.7 and above](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven Deployment](https://maven.apache.org/download.cgi)

### Deployment
Build the jar file with maven using the following command within the folder containing the *pom.xml* file:</br>
**mvn clean package**

This action will create a folder called *target* with the build project files, we only need three files, you can delete the rest and the downloaded repository after copying the three files to your selected path:
- *SmarthomeMqttClient-jar-with-dependencies.jar* is the application code.
- *SmarthomeMqttClient.bat* is the application activation script, edit the file in any text editor and update your broker's data inside the designated quotation marks.
- *action_map.json* is the mapping file for mapping an action to perform on your computer to a payload recieved to the topic.

### Usage
To start the application, just double click the *AskMyPc.bat*, assuming it's configured properly, it will start the mqtt client and a message will be published with the *payload* **Online** to the *topic* **smarthome/mqtt_client/status_update** notifying the broker with the client status.</br>
The client then will subscribe to the *topic* **smarthome/askmypc/action** and transfer the incoming payloads to the *AskMyPc Handler*.</br>

Tge AskMyPc Handler will get the message and iterate over the *action_map.json* looking for an action to perform based on the recieved *payload* and will try to perform it as a Desktop Application and if it failes, the controller will try to perform the action as a Web Application.

### Example of usage
In the [*action_map.json file*](conf_files/action_map.json):
- The *facebook* payload is mapped to *https://www.facebook.com/*.
- The *excel* payload is mapped to *C:/Program Files/Microsoft Office/Root/Office16/EXCEL.EXE*
- The *word and google* is mapped to a json object containing to mappings:
  - *C:/Program Files/Microsoft Office/Root/Office16/WINWORD.EXE*
  - *https://www.google.com*

When the AskMyPc Controller recieves the payloads:
- *facebook* it will open the facebook site as a web application in your default web browser.
- *excel* it will open microsoft excel as a desktop application.
- *word and google* it will open the google website as a web application and microsoft word as a desktop application.

When editing the *action_map.json* file, please check the syntax with [JSONLint](https://jsonlint.com/), a broken syntax will cause the application to crash.

## Log Files

All the log files are daily rolled:
- *MqttConnectionManager.yyyy-MM-dd.log* is the log file for the Mqtt Client.
- *AskMyPc.yyyy-MM-dd.log* is the log file for AskMyPc Controller.

## Special Notes

- Please note, the client does not supports ssl connectivty to the broker, it is meant to run inside the local network.
- Please note, **Online** payloads will be published with the topic **smarthome/mqtt_client/status_update**, when the topic **smarthome/mqtt_client/request_status_update** is recieved, that way you can get the status of the client from other clients.
