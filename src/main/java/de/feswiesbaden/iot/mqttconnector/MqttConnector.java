package de.feswiesbaden.iot.mqttconnector;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.logging.Logger;

/**
 * Verwaltet MQTT-Verbindungen und behandelt das Versenden von Nachrichten mit dem Eclipse Paho Client.
 * Diese Klasse bietet eine Abstraktion für das Verbinden mit einem MQTT-Broker, das Abonnieren von Themen
 * und das Veröffentlichen von Nachrichten.
 */
public class MqttConnector {

    private MqttClient client;
    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Erstellt eine Instanz von MqttConnector.
     *
     * @param mqttBroker Die URL des MQTT-Brokers.
     * @param clientName Die Kennung für diesen Client.
     * @param myCallback Ein Callback, um Nachrichten und Verbindungsvorfälle zu behandeln.
     */
    public MqttConnector(String mqttBroker, String clientName, MyMqttCallback myCallback) {
        try {
            client = new MqttClient(mqttBroker, clientName);
            client.setCallback(myCallback);
        } catch (MqttException e) {
            logger.severe("Fehler beim Initialisieren des MqttClients: " + e.getMessage());
        }
    }

    /**
     * Startet die Verbindung zum MQTT-Broker mit den angegebenen Benutzerdaten.
     *
     * @param user Der Benutzername für die Authentifizierung am Broker.
     * @param pwd Das Passwort für die Authentifizierung am Broker.
     */
    public void start(String user, String pwd) {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(user);
        connOpts.setPassword(pwd.toCharArray());
        try {
            client.connect(connOpts);
            logger.info("Verbindung zum MQTT-Broker erfolgreich hergestellt.");
        } catch (MqttException e) {
            logger.severe("Verbindung zum MQTT-Broker konnte nicht hergestellt werden: " + e.getMessage());
        }
    }

    /**
     * Abonniert ein spezifisches MQTT-Topic.
     *
     * @param topicFilter Das Topic-Filter, das abonniert werden soll.
     */
    public void subscribe(String topicFilter) {
        try {
            client.subscribe(topicFilter, 0);
            logger.info("Erfolgreich abonniert auf Topic: " + topicFilter);
        } catch (MqttException e) {
            logger.severe("Fehler beim Abonnieren des Topics: " + e.getMessage());
        }
    }

    /**
     * Veröffentlicht eine Nachricht auf einem spezifischen Topic.
     *
     * @param topic Das Topic, auf dem die Nachricht veröffentlicht wird.
     * @param strMessage Die zu veröffentlichende Nachricht.
     */
    public void publish(String topic, String strMessage) {
        MqttMessage message = new MqttMessage(strMessage.getBytes());
        try {
            client.publish(topic, message);
            logger.info("Nachricht veröffentlicht: Topic = " + topic + ", Nachricht = " + strMessage);
        } catch (MqttException e) {
            logger.severe("Fehler beim Veröffentlichen der Nachricht: " + e.getMessage());
        }
    }

    /**
     * Veröffentlicht eine Nachricht auf einem spezifischen Topic.
     *
     * @param mqttValue Das MqttValue-Objekt, das die Nachricht und das Topic enthält.
     */
    public void publish(MqttValue mqttValue) {
        publish(mqttValue.getTopic(), mqttValue.getMessage());
    }
}
