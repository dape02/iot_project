package de.feswiesbaden.iot.data.sensordata;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SensorDataService {
       private final SensorDataRepository repository;

    public SensorDataService(SensorDataRepository repository) {
        this.repository = repository;
    }

    public Optional<SensorData> get(Long id) {
        return repository.findById(id);
    }

    public SensorData update(SensorData entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<SensorData> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<SensorData> list(Pageable pageable, Specification<SensorData> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }
}