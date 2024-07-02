package de.feswiesbaden.iot.data.mqttclient;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqttValueService {

    private final MqttValueRepository repository;

    public MqttValueService(@Autowired MqttValueRepository repository) {
        this.repository = repository;
    }

    public Optional<MqttValue> get(Long id) {
        return repository.findById(id);
    }

    public MqttValue update(MqttValue entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<MqttValue> list() {
        return repository.findAll();
    }

    public List<MqttValue> fetchMany(int n) {
        return repository.findLastN(n);
    }

    public int count() {
        return (int) repository.count();
    }
    
    // Add the findAll method
    public List<MqttValue> findAll() {
        return repository.findAll();
    }
}
