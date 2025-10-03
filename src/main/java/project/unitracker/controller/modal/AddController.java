package project.unitracker.controller.modal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import project.unitracker.model.view.componentmodel.LabelButtonHandler;
import project.unitracker.utility.uimodel.LabelUtility;
import project.unitracker.utility.validator.Filter;
import project.unitracker.controller.Controller;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.psql.ParsedCommand;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Controller della finestra modale add-transaction.fxml
public class AddController implements Controller {

    @FXML
    // Serve a scegliere se applicare immediatamente la transazione o meno
    private CheckBox checkApply;

    @FXML
    private TextArea description;

    @FXML
    private Label errorLabel;

    @FXML
    private Button executeButton;

    @FXML
    private TextField quantity;

    @FXML
    private TextField chartCode;

    @FXML
    private DatePicker datePicker;

    @FXML
    // Una volta inseriti tutti i parametri correttamente, genera un ParsedCommand che viene eseguito
    void executeCommand(ActionEvent event) {
        ArrayList<String> parameters = new ArrayList<>();

        // Tutti i valori dei Field vengono resi parametri
        parameters.add( chartCode.getText() );
        parameters.add( quantity.getText() );
        parameters.add( datePicker.getValue().toString() );
        parameters.add( (description.getText().isEmpty()) ? null : description.getText() );

        // Trasforma i parametri direttamente in un ParsedCommand ben formato che viene eseguito subito
        ParsedCommand cmd = new ParsedCommand(
                "ADD",
                parameters,
                checkApply.isSelected()
        );

        // Il comando viene eseguito
        DatabaseHandler.getInstance().executeCommand(cmd);

        goBack(null);
    }

    @FXML
    // Chiude la finestra modale
    void goBack(ActionEvent event) {
        Stage stage = (Stage) executeButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        // Impedisce di eseguire se tutti i campi obbligatori non vengono
        // chartCode viene fornito sempre come parametro e viene impedito modificarlo, quindi non c'è bisogno
        // del binding chartCode.textProperty().isEmpty() poiché non sarà mai vuoto né invalido
        quantity.textProperty().addListener((obs, oldVal, newVal) -> {
            LabelButtonHandler.updateLabelButton(errorLabel, executeButton, !newVal.isEmpty());
        });


        // Di default viene messa la data odierna
        datePicker.setValue(LocalDate.now());
        // Impedisce che l'utente scriva all'interno
        datePicker.getEditor().setDisable(true);
        datePicker.getEditor().setOpacity(1);

        // Filtri che impediscono all'utente di inserire valori in formato invalido
        quantity.setTextFormatter( new TextFormatter<>(Filter.quantityFilter) );
        description.setTextFormatter( new TextFormatter<>(Filter.noCommaFilter) );
    }

    // Se viene fornito come parametro la chartCode, viene impedito di modificarlo
    // L'unico parametro che si aspetta è chartCode
    @Override
    public void setParameters(List<Object> parameters) {
        this.chartCode.setText((String)parameters.getFirst());
        this.chartCode.setDisable(true);
    }

    @Override
    public void reload() {}

    @Override
    public void onClosed() {}
}
