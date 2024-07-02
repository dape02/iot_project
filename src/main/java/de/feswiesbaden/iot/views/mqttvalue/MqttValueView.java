package de.feswiesbaden.iot.views.mqttvalue;

import java.util.logging.Logger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import de.feswiesbaden.iot.data.mqttclient.MqttValueService;
import de.feswiesbaden.iot.mqttconnector.MqttConnector;
import de.feswiesbaden.iot.mqttconnector.MyMqttCallback;
import de.feswiesbaden.iot.views.MainLayout;
import de.feswiesbaden.iot.views.MainViewController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

@PageTitle("Hello World")
@Route(value = "hello", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class MqttValueView extends VerticalLayout {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Grid<MqttValue> grid; // Grid for displaying data

    private MqttConnector mqttConnector;  // MQTT Publisher

    @Value("${mqtt.broker.address}")
    private String brokerAddress; // Address of the MQTT Broker

    private final Environment env; // Environment variables for the MQTT Broker configuration (see application.properties)

    MqttValueService mqttValueService; // Service for database operations

    /**
     * Called when the view is attached to the UI
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {

        // Singleton
        if (mqttConnector == null) {
            mqttConnector = new MqttConnector(brokerAddress, "Client-01",
                    new MyMqttCallback(mqttValueService, new MainViewController(attachEvent.getUI(), grid))
            );
            mqttConnector.start(
                    env.getProperty("mqtt.broker.username"),
                    env.getProperty("mqtt.broker.password")); // Establish connection to the broker
            mqttConnector.subscribe("#"); // Subscribe to all topics
        }

        logger.info("OnAttach!!");
    }

    /**
     * Called when the view is detached from the UI
     */
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // TODO: Handle detachment if necessary
    }

    public MqttValueView(Environment env, MqttValueService mqttValueService) {
        this.env = env;
        this.mqttValueService = mqttValueService;

        add(new Span("Mqtt Broker Address: " + brokerAddress));
        setSizeFull();
        genExamplePublish();
        genExampleSubscribe();
    }

    /**
     * Example for publishing data
     */
    public void genExamplePublish() {

        HorizontalLayout ePublish = new HorizontalLayout();

        Button btnPublish = new Button("Publish");
        TextField tfMessage = new TextField("Message");
        TextField tfTopic = new TextField("Topic");

        Button btnGridUpdater = new Button("Update Grid");
        btnGridUpdater.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGridUpdater.addClickListener(e -> {
            grid.setItems(mqttValueService.findAll());
        });

        btnPublish.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnPublish.addClickListener(e -> {
            MqttValue value = new MqttValue(tfMessage.getValue(), tfTopic.getValue());
            mqttConnector.publish(value);
        });

        ePublish.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnPublish);
        ePublish.add(tfTopic, tfMessage, btnPublish);

        add(ePublish, btnGridUpdater);
    }

    /**
     * Example for displaying data in a grid
     */
    public void genExampleSubscribe() {

        grid = new Grid<>(MqttValue.class, false);
        grid.setItems(mqttValueService.findAll());

        grid.addColumn(MqttValue::getId).setHeader("id").setSortable(true);
        grid.addColumn(new LocalDateTimeRenderer<>(MqttValue::getTimeStamp, "dd.MM.YYYY HH:mm:ss"))
                .setHeader("Zeitstempel").setSortable(true).setComparator(MqttValue::getTimeStamp);
        grid.addColumn(MqttValue::getTopic).setHeader("Topic").setSortable(false);
        grid.addColumn(MqttValue::getMessage).setHeader("Message").setSortable(false).setFlexGrow(1);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(event -> Notification.show(event.getItem().toString()));

        add(grid);
    }
}
