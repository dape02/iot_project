package de.feswiesbaden.iot.mqttconnector;

import java.util.logging.Logger;

import de.feswiesbaden.iot.data.mqttclient.MqttValueService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import de.feswiesbaden.iot.views.MainViewController;

public class MyMqttCallback implements MqttCallback  {

    private final MqttValueService mqttValueService;
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final MainViewController mainViewController;

    public MyMqttCallback(MqttValueService mqttValueService, MainViewController mainViewController){

        this.mqttValueService=mqttValueService;
        this.mainViewController=mainViewController;

        logger.info("Callback started!!");
     
    }
    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub
        logger.info("Connection lost");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
        logger.info("Delivery complete");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // TODO Auto-generated method stub

        MqttValue value =new MqttValue(message.toString(), topic);

        logger.info("Message arrived:");
        logger.info(value.toString());

        mqttValueService.update(value);

        mainViewController.updateGrid();

    }
    
}

