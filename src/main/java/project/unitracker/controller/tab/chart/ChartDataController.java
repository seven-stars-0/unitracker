package project.unitracker.controller.tab.chart;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import project.unitracker.controller.Controller;
import project.unitracker.model.view.componentmodel.ChartTabModel;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.uimodel.TableViewRow;

import java.util.List;

// Il controller della componente della Tab chart.fxml
// Mostra una tabella contenente tutte le transazione avvenute sul grafico, permettendo di applicarle o cancellarle
public class ChartDataController implements Controller {

    @FXML
    private Button applyButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TableView<TableViewRow> tableView;

    @FXML
    private TableColumn<TableViewRow, Integer> idColumn;

    @FXML
    private TableColumn<TableViewRow, String> typeColumn;

    @FXML
    private TableColumn<TableViewRow, Double> quantityColumn;

    @FXML
    private TableColumn<TableViewRow, String> dateColumn;

    @FXML
    private TableColumn<TableViewRow, String> descriptionColumn;

    @FXML
    private HBox hbox;

    private ChartTabModel tabModel;

    // Viene aggiunta dinamicamente quando l'utente seleziona una riga APPLIED
    // nel caso in cui voglia eliminare quella transazione, può scegliere se farlo temporaneamente o definitivamente
    // In pratica è APPLY della DELETE
    private CheckBox applyCheckBox;

    @FXML
    // Viene chiamata sempre da transazioni provvisorie, che siano eliminazioni provvisorie o aggiunte provvisorie
    // Questo crea un ParsedCommand che applica la transazione, per poi eseguirlo
    void applySelectedTransaction(ActionEvent event) {
        TableViewRow selectedRow = tableView.getSelectionModel().getSelectedItem();

        String type = ( selectedRow.getType().equals("PROV") ) ? "A_APPLY" : "D_APPLY";

        ParsedCommand cmd = new ParsedCommand(
                type,
                List.of(selectedRow.getId().toString()),
                false
        );


        DatabaseHandler.getInstance().executeCommand(cmd);
    }

    @FXML
    // Può essere chiamata selezionando qualsiasi transazione
    // Se viene chiamata su una transazione APPLIED la cancella provvisoriamente o meno in base ad applyCheckBox
    // Se viene chiamata su PROV o DELETE, semplicemente le elimina. In questi casi apply è sempre falsa
    void deleteSelectedTransaction(ActionEvent event) {
        TableViewRow selectedRow = tableView.getSelectionModel().getSelectedItem();

        String type = "";
        boolean apply = false;

        switch (selectedRow.getType()) {
            case "APPLIED" -> {
                type = "DELETE";
                apply = applyCheckBox.isSelected();
            }
            case "PROV" -> type = "A_CANCEL";
            case "DELETE" -> type = "D_CANCEL";
        }

        ParsedCommand cmd = new ParsedCommand(
            type,
            List.of(selectedRow.getId().toString()),
            apply
        );

        // Il comando viene eseguito
        DatabaseHandler.getInstance().executeCommand(cmd);
    }


    @FXML
    void initialize() {
        // Viene inizializzata la CheckBox che viene aggiunta solo quando si seleziona
        // una transazione applicata
        applyCheckBox = new CheckBox("Elimina definitivamente");

        // Per gestire la selezione di una riga
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            // Se non si seleziona nulla, tutti i bottoni vengono disabilitati
            if (newSel == null) {
                deleteButton.setDisable(true);
                applyButton.setDisable(true);

                hbox.getChildren().remove(applyCheckBox);
                return;
            }

            // Qualsiasi transazione si può cancellare, a prescindere dal tipo
            deleteButton.setDisable(false);
            // Non si può applicare una transazione già applicata
            applyButton.setDisable(newSel.getType().equals("APPLIED"));

            // Se la transazione selezionata è APPLIED, allora possiamo cancellarla definitivamente
            // quindi aggiungiamo applyCheckBox
            if ( newSel.getType().equals("APPLIED") ) {
                applyCheckBox.setDisable(false);
                applyCheckBox.setSelected(false); // Di default non lo seleziona per evitare danni

                // Se non già presente, la CheckBox viene aggiunta alla HBox
                if ( ! hbox.getChildren().contains(applyCheckBox))
                    hbox.getChildren().addFirst(applyCheckBox);
            }
            // Se non è una transazione APPLIED applyCheckBox viene rimosso
            else {
                applyCheckBox.setDisable(true);
                hbox.getChildren().remove(applyCheckBox);
            }
        });

        // --- Tutte le colonne vengono inizializzate ---
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    @Override
    // Come parametro riceve il ChartTabModel da ChartController
    public void setParameters(List<Object> parameters) {
        tabModel = (ChartTabModel) parameters.getFirst();

        // Si registra per venire segnalato di cambiamenti nel database
        tabModel.register(this);

        reload();
    }

    @Override
    // Popola la tabella con dati
    // Viene chiamato dal suo ChartTabModel per mantenere i dati mostrati coerenti con il database
    public void reload() {
        tableView.setItems( tabModel.getRows() );
    }

    @Override
    public void onClosed() {}
}