package de.feswiesbaden.iot.data.mqttclient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MqttValueRepository extends JpaRepository<MqttValue, Long> {

    @Query(value = "SELECT * FROM mqtt_value ORDER BY id DESC LIMIT 100", nativeQuery = true)
    List<MqttValue> findLast100();

    @Query(value = "SELECT * FROM mqtt_value ORDER BY id DESC LIMIT ?1", nativeQuery = true)
    List<MqttValue> findLastN(int n);
}
