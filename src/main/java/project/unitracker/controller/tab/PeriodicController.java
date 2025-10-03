package project.unitracker.controller.tab;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import project.unitracker.controller.Controller;
import project.unitracker.model.periodic.PeriodicTransactionHandler;
import project.unitracker.model.view.StageHandler;
import project.unitracker.utility.uimodel.TableViewRow;
import project.unitracker.utility.constant.URL;

import java.util.List;

// Il controller della Tab periodic.fxml
public class PeriodicController implements Controller {

    @FXML
    private TableColumn<TableViewRow, String> date;

    @FXML
    private Button deleteButton;

    @FXML
    private TableColumn<TableViewRow, String> description;

    @FXML
    private TableColumn<TableViewRow, Double> quantity;

    @FXML
    private TableColumn<TableViewRow, String> chart;

    @FXML
    private TableView<TableViewRow> tableView;

    @FXML
    private TableColumn<TableViewRow, String> type;

    @FXML
    // Apre la finestra modale per aggiungere una transazione periodica
    void addPeriodicTransaction(ActionEvent event) {
        Stage owner = (Stage) tableView.getScene().getWindow();
        StageHandler.openModalWindow(URL.ADD_PERIODIC_FXML, "Aggiungi transazione", owner, null);

        reload();
    }

    @FXML
    // Cancella dal file data/periodic.txt la riga selezionata
    void deleteSelected(ActionEvent event) {
        PeriodicTransactionHandler.delete( tableView.getSelectionModel().getSelectedItem() );

        reload();
    }

    @FXML
    void initialize() {
        // Ogni volta che viene selezionata una riga, abilitiamo il bottone per eliminarla
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel == null) {
                deleteButton.setDisable(true);
                return;
            }

            deleteButton.setDisable(false);
        });

        // --- Le colonne della TableView vengono inizializzate ---
        chart.setCellValueFactory(new PropertyValueFactory<>("chartCode"));
        type.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        date.setCellValueFactory(new PropertyValueFactory<>("date"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));

        reload();
    }

    @Override
    public void setParameters(List<Object> parameters) {}

    @Override
    // Recupera dal file data/periodic.txt le righe della TableView
    public void reload() {
        tableView.setItems( PeriodicTransactionHandler.getObservableRows() );
    }

    @Override
    public void onClosed() {}
}


