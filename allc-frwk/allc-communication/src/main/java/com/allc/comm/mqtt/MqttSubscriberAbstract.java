/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.allc.comm.mqtt;

import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Tyrone Lopez
 */
public abstract class MqttSubscriberAbstract extends Thread implements MqttCallback {

    static final Logger LOGGER = Logger.getLogger(MqttSubscriberAbstract.class);

    private MqttClient mqttClient;

    public void subscribe() {
        //    logger file name and pattern to log
        try {
            mqttClient = new MqttClient("tcp://" + getBrokenUrl() + ":1883", getClientId());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            // connOpts.setUserName(username);
            // connOpts.setPassword(password.toCharArray());

            // Connect the client
            LOGGER.info("Connecting to Solace messaging at " + getBrokenUrl());
            mqttClient.connect(connOpts);
            LOGGER.info("Connected");

            // Latch used for synchronizing b/w threads
            final CountDownLatch latch = new CountDownLatch(1);

            // Topic filter the client will subscribe to
            mqttClient.setCallback(this);
            LOGGER.info("Subscribing client to topic: " + getTopic());
            mqttClient.subscribe(getTopic(), 0);
            LOGGER.info("Subscribed");

            // Wait for the message to be received
            try {
                latch.await(); // block here until message received, and latch will flip
            } catch (InterruptedException e) {
                LOGGER.error("I was awoken while waiting");
            }

            // Disconnect the client
            mqttClient.disconnect();
            LOGGER.info("Subscribed" + getTopic());
            LOGGER.info("Listening");
        } catch (MqttException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void run() {
        LOGGER.info("Ingresa al run");
        this.subscribe();
    }

    public void connectionLost(Throwable thrwbl) {
        LOGGER.error(thrwbl.getMessage());
    }

    public void messageArrived(String string, MqttMessage message) throws Exception {
        LOGGER.info("LLEGA respuesta -- " + message.toString());
        if (processesResult(message.toString())) {
            mqttClient.unsubscribe(string);
            LOGGER.info("Unsusbribe topic " + getTopic());
        }
    }

    public void deliveryComplete(IMqttDeliveryToken imdt) {

    }

    public abstract String getTopic();

    public abstract String getClientId();

    public abstract String getBrokenUrl();

    public abstract boolean processesResult(String result);

}
