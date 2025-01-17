package de.feswiesbaden.iot.views.sensordata;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;

import de.feswiesbaden.iot.data.sensordata.SensorData;
import de.feswiesbaden.iot.data.sensordata.SensorDataService;
import de.feswiesbaden.iot.views.MainLayout;



@PageTitle("SensorData-Detail")
@Route(value = "sensordata-detail/:SensorDataID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
public class SensorDataView extends Div implements BeforeEnterObserver {

        private final String SENSORDATA_ID = "sensordataID";
        private final String SENSORDATA_EDIT_ROUTE_TEMPLATE = "sensordata-detail/%s/edit";

        private final Grid<SensorData> grid = new Grid<>(SensorData.class, false);

    private TextField id;
    private DateTimePicker timeStamp;
    private TextField temperature;
    private TextField humidity;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SensorData> binder;

    private SensorData sensorData;

    private final SensorDataService sensorDataService;

    public SensorDataView(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
        addClassNames("sensordata-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("id").setAutoWidth(true);
        grid.addColumn("timeStamp").setAutoWidth(true);
        grid.addColumn("temperature").setAutoWidth(true);
        grid.addColumn("humidity").setAutoWidth(true);


        grid.setItems(query -> sensorDataService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SENSORDATA_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(SensorDataView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(SensorData.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.sensorData == null) {
                    this.sensorData = new SensorData();
                }
                binder.writeBean(this.sensorData);
                sensorDataService.update(this.sensorData);
                clearForm();
                refreshGrid();
                Notification.show("Data updated");
                UI.getCurrent().navigate(SensorDataView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error updating the data. Somebody else has updated the record while you were making changes.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Failed to update the data. Check again that all values are valid");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> sensorDataId = event.getRouteParameters().get(SENSORDATA_ID).map(Long::parseLong);
        if (sensorDataId.isPresent()) {
            Optional<SensorData> sensorDataFromBackend = sensorDataService.get(sensorDataId.get());
            if (sensorDataFromBackend.isPresent()) {
                populateForm(sensorDataFromBackend.get());
            } else {
                Notification.show(String.format("The requested SensorData was not found, ID = %s", sensorDataId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(SensorDataView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        id = new TextField("ID");
        temperature = new TextField("Temperature");
        humidity = new TextField("Humidity");
        formLayout.add(id, temperature, humidity);
        timeStamp = new DateTimePicker();
        formLayout.addFormItem(timeStamp, "TimeStamp");

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SensorData value) {
        this.sensorData = value;
        binder.readBean(this.sensorData);

    }

}