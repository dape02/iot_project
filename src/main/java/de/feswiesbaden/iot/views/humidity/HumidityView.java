package de.feswiesbaden.iot.views.humidity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.charts.model.Tooltip;
import com.vaadin.flow.component.charts.model.XAxis;
import com.vaadin.flow.component.charts.model.YAxis;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import de.feswiesbaden.iot.data.mqttclient.MqttValueService;
import de.feswiesbaden.iot.views.MainLayout;
import de.feswiesbaden.iot.views.converter.LocalDateToLocalDateTimeConverter;

@PageTitle("MQTT Humidity Values")
@Route(value = "mqtt-humidity-values/:mqttID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
public class HumidityView extends Div implements BeforeEnterObserver {

    private final String MQTT_ID = "mqttID";
    private final String MQTT_EDIT_ROUTE_TEMPLATE = "mqtt-humidity-values/%s/edit";

    private final Grid<MqttValue> grid = new Grid<>(MqttValue.class, false);

    private DatePicker timeStamp;

    private final BeanValidationBinder<MqttValue> binder;

    private MqttValue mqttValue;

    private final MqttValueService mqttValueService;

    private Chart chart;

    public HumidityView(MqttValueService mqttValueService) {
        this.mqttValueService = mqttValueService;
        addClassNames("mqtt-humidity-value-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createFilterEditorLayout(splitLayout);
        createChart(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("id").setAutoWidth(true);
        grid.addColumn("version").setAutoWidth(true);
        grid.addColumn("message").setAutoWidth(true);
        grid.addColumn(new LocalDateTimeRenderer<>(MqttValue::getTimeStamp, "dd.MM.yyyy")).setHeader("Date").setAutoWidth(true);
        grid.addColumn(new LocalDateTimeRenderer<>(MqttValue::getTimeStamp, "HH:mm:ss")).setHeader("Time").setAutoWidth(true);
        grid.addColumn("topic").setAutoWidth(true);

        // Set the last 100 items with the topic filter applied
        grid.setItems(mqttValueService.fetchMany(100).stream()
                .filter(mqttValue -> "sensor/humidity".equals(mqttValue.getTopic()))
                .collect(Collectors.toList()));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(MQTT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(HumidityView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(MqttValue.class);

        // Bind fields manually for timeStamp with converter
        binder.forField(timeStamp)
              .withConverter(new LocalDateToLocalDateTimeConverter(ZoneId.systemDefault()))
              .bind(MqttValue::getTimeStamp, MqttValue::setTimeStamp);

        // Bind other fields automatically
        binder.bindInstanceFields(this);

        // Initialize chart with current day's data
        updateChart(LocalDate.now());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> mqttId = event.getRouteParameters().get(MQTT_ID).map(Long::parseLong);
        if (mqttId.isPresent()) {
            Optional<MqttValue> mqttValueFromBackend = mqttValueService.get(mqttId.get());
            if (mqttValueFromBackend.isPresent()) {
                populateForm(mqttValueFromBackend.get());
            } else {
                Notification.show(String.format("The requested MQTT value was not found, ID = %s", mqttId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(HumidityView.class);
            }
        }
    }

    private void createFilterEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        timeStamp = new DatePicker("Date");

        Button filterButton = new Button("Filter");
        filterButton.addClickListener(e -> {
            // Apply filtering logic here
            LocalDate selectedDate = timeStamp.getValue();
            updateChart(selectedDate);
            grid.setItems(mqttValueService.fetchMany(100).stream()
                    .filter(mqttValue -> "sensor/humidity".equals(mqttValue.getTopic()))
                    .filter(mqttValue -> selectedDate == null || mqttValue.getTimeStamp().toLocalDate().equals(selectedDate))
                    .collect(Collectors.toList()));
        });

        formLayout.add(timeStamp, filterButton);

        editorDiv.add(formLayout);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void createChart(SplitLayout splitLayout) {
        chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Humidity Values");

        XAxis xAxis = new XAxis();
        xAxis.setTitle("Time");
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Humidity (%)");
        conf.addyAxis(yAxis);

        // Tooltip konfigurieren
        Tooltip tooltip = new Tooltip();
        tooltip.setEnabled(true);
        tooltip.setFormatter("function() { return 'Humidity: ' + this.y + '%'; }");
        conf.setTooltip(tooltip);

        Div chartWrapper = new Div();
        chartWrapper.setClassName("chart-wrapper");
        chartWrapper.add(chart);
        splitLayout.addToPrimary(chartWrapper);
    }

    private void updateChart(LocalDate date) {
        List<MqttValue> data = mqttValueService.fetchMany(100).stream()
                .filter(mqttValue -> "sensor/humidity".equals(mqttValue.getTopic()))
                .filter(mqttValue -> date == null || mqttValue.getTimeStamp().toLocalDate().equals(date))
                .filter(mqttValue -> {
                    try {
                        Double.parseDouble(mqttValue.getMessage());
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        // Group data by hour and calculate the average humidity for each hour
        Map<Integer, Double> hourlyAverages = data.stream()
                .collect(Collectors.groupingBy(
                        mqttValue -> mqttValue.getTimeStamp().getHour(),
                        Collectors.averagingDouble(mqttValue -> Double.parseDouble(mqttValue.getMessage()))
                ));

        // Sort hours and extract the average values
        List<Integer> sortedHours = new ArrayList<>(hourlyAverages.keySet());
        Collections.sort(sortedHours);

        List<String> availableHours = sortedHours.stream()
                .map(hour -> String.format("%02d:00", hour))
                .collect(Collectors.toList());

        List<Double> humidityValues = sortedHours.stream()
                .map(hourlyAverages::get)
                .collect(Collectors.toList());

        Configuration conf = chart.getConfiguration();
        conf.getxAxis().setCategories(availableHours.toArray(new String[0]));

        ListSeries series = new ListSeries();
        series.setName("Humidity");
        series.setData(humidityValues.toArray(new Double[0]));

        conf.setSeries(series);

        if (data.isEmpty()) {
            Notification.show("No data available for the selected date");
        }

        chart.drawChart();
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(MqttValue value) {
        this.mqttValue = value;
        binder.readBean(this.mqttValue);
        // Manually set the timeStamp field if value is not null
        if (value != null) {
            timeStamp.setValue(value.getTimeStamp().toLocalDate());
        } else {
            timeStamp.clear();
        }
    }
}
