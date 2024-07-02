package de.feswiesbaden.iot.data.sensordata;

import java.time.LocalDateTime;

import de.feswiesbaden.iot.data.AbstractEntity;
import jakarta.persistence.Entity;

@Entity
public class SensorData extends AbstractEntity {
    private Long id;
    private LocalDateTime timeStamp;
    private float temperature;
    private int humidity;

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }
}