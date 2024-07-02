package de.feswiesbaden.iot.data.mqttclient;

import java.time.LocalDateTime;
import java.util.Objects;

import de.feswiesbaden.iot.data.AbstractEntity;
import jakarta.persistence.Entity;

@Entity
public class MqttValue extends AbstractEntity {
    private String message;
    private String topic;
    private LocalDateTime timeStamp;

    public MqttValue() {
    }

    public MqttValue(String message, String topic) {
        this.message = message;
        this.topic = topic;
        this.timeStamp = LocalDateTime.now();
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * @return the timeStamp
     */
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "MqttValue [message=" + message + ", timeStamp=" + timeStamp + ", topic=" + topic + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, timeStamp, topic);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MqttValue other = (MqttValue) obj;
        return Objects.equals(message, other.message) && Objects.equals(timeStamp, other.timeStamp)
                && Objects.equals(topic, other.topic);
    }
}