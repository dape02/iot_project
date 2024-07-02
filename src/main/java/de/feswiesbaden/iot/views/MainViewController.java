package de.feswiesbaden.iot.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import de.feswiesbaden.iot.data.mqttclient.MqttValue;

public class MainViewController {
    private UI ui;
    private Grid<MqttValue> grid;

    public MainViewController(UI ui, Grid<MqttValue> grid){

        this.ui=ui;
        this.grid=grid;
    }

    public void updateGrid() {

        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                // Sicherstellen, dass das Grid noch angehängt ist
                if (grid.isAttached()) {
                    // Datenprovider aktualisieren
                    grid.getDataProvider().refreshAll();
                }
                // UI pushen, um Änderungen sofort sichtbar zu machen
                ui.push();
            });
        }
    }
}
