/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.comm.mqtt;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Tyrone Lopez
 */
public abstract class MqttPublishAbstract {

    static final Logger LOGGER = Logger.getLogger(MqttPublishAbstract.class);

    

    public void publish() throws Exception{
        //    LOGGER file name and pattern to log
        try {
            // Create an Mqtt client
            MqttClient mqttClient = new MqttClient("tcp://"+getBrokenUrl()+":1883", getClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            // connOpts.setUserName(username);
            //connOpts.setPassword(password.toCharArray());

            // Connect the client
            LOGGER.info("Connecting to Solace messaging at " + toString());
            mqttClient.connect(connOpts);
            LOGGER.info("Connected");

            // Create a Mqtt message
            String messageReplace = getMessage().replace("?", "");
            MqttMessage message = new MqttMessage(messageReplace.getBytes());
            // Set the QoS on the Messages - 
            // Here we are using QoS of 0 (equivalent to Direct Messaging in Solace)
            message.setQos(0);

            LOGGER.info("Publishing message: " +messageReplace);

            // Publish the message
            mqttClient.publish(getTopic(), message);

            // Disconnect the client
            mqttClient.disconnect();

            LOGGER.info("Message published. Exiting");

        } catch (MqttException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * The broker url.
     * @return 
     */
    public abstract String getBrokenUrl();
    
    public abstract String getTopic();

    public abstract String getClientId();

    public abstract String getMessage();

}
