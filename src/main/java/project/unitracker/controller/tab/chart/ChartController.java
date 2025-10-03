package project.unitracker.controller.tab.chart;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import project.unitracker.controller.Controller;
import project.unitracker.model.view.componentmodel.ChartTabModel;
import project.unitracker.model.view.StageHandler;
import project.unitracker.utility.constant.URL;

import java.io.IOException;
import java.util.List;

// Il controller della Tab chart.fxml
public class ChartController implements Controller {
    @FXML
    private SplitPane splitPane;
    // Conserva l'ultima posizione del divider di splitPane
    // Di default è 0.5
    private Double lastDividerPosition = 0.5;

    // Questa contiene la TableView
    private BorderPane borderPane;

    @FXML
    // Per selezionare i grafici con dati temporanei
    private CheckBox checkBoxUnapplied;

    @FXML
    private ToggleGroup radioGroup;

     @FXML
     private LineChart<String, Number> lineChart;

     // Il model che contiene i dati dei grafici, e si occupa di mantenere la coerenza con il database
     private ChartTabModel tabModel;

    @FXML
    // Apre la finestra modale add-transaction.fxml
    void addTransaction(ActionEvent event) throws IOException  {
        Stage owner = (Stage) lineChart.getScene().getWindow();
        StageHandler.openModalWindow(URL.ADD_TRANSACTION_FXML, "Aggiungi transazione", owner, List.of(tabModel.getCode()));
    }

    @FXML
    // Apre o chiude la TableView contenuta in borderPane
    // Salva l'ultima posizione del divider alla chiusura, e la ripristina alla riapertura
    void toggleTableView(ActionEvent event) {
        if (splitPane.getItems().contains(borderPane)) {
            lastDividerPosition = splitPane.getDividerPositions()[0];
            splitPane.getItems().remove(borderPane);
        }
        else {
            splitPane.getItems().addLast(borderPane);
            splitPane.setDividerPositions(lastDividerPosition);
        }
    }

    @FXML
    // Viene chiamata quando si interagisce con checkBoxUnapplied
    // Chiama reload() per mostrare il grafico corretto
    void checkBox(ActionEvent event) {
        reload();
    }

    // Prende il valore (D,W,M,Y) del RadioButton attualmente selezionato
    private String currPeriod() {
        return ((RadioButton) radioGroup.getSelectedToggle()).getText();
    }

    @Override
    // Prende da ChartTabModel i dati corretti basandosi sul valore del RadioButton e sul fatto che l'utente voglia vedere
    // o meno i dati provvisori (checkBoxUnapplied)
    public void reload() {
        lineChart.getData().clear();
        lineChart.getData().add( tabModel.getGraph(currPeriod(), !checkBoxUnapplied.isSelected()) );
    }

    @Override
    // Riceve il proprio model come parametro
    public void setParameters(List<Object> parameters) {
        tabModel = (ChartTabModel) parameters.getFirst();

        // Registra se stesso al ChartTabModel in modo che il model possa chiamare reload() ogni volta che c'è un'incoerenza
        // con il database, per mostrare sempre dati aggiornati
        tabModel.register(this);

        // Crea la TableView che mostra tutte le transazioni relative al grafico aperto
        borderPane = (BorderPane) StageHandler.getRoot(URL.CHART_DATA_FXML, parameters).root();
        lineChart.getYAxis().setLabel(tabModel.getUnit());

        reload();
    }

    @FXML
    public void initialize() {
        // Ogni volta che viene selezionato un RadioButton, il grafico viene ricaricato dal model
        radioGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;
            reload();
        });

        lineChart.setAnimated(false);
    }

    @Override
    public void onClosed() {}

}
