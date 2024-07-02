package de.feswiesbaden.iot.views.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

public class LocalDateToLocalDateTimeConverter implements Converter<LocalDate, LocalDateTime> {

    private final ZoneId zoneId;

    public LocalDateToLocalDateTimeConverter(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public Result<LocalDateTime> convertToModel(LocalDate value, ValueContext context) {
        return value == null ? Result.ok(null) : Result.ok(value.atStartOfDay(zoneId).toLocalDateTime());
    }

    @Override
    public LocalDate convertToPresentation(LocalDateTime value, ValueContext context) {
        return value == null ? null : value.toLocalDate();
    }
}
