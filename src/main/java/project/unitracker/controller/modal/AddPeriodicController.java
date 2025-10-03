package project.unitracker.controller.modal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import project.unitracker.model.view.componentmodel.LabelButtonHandler;
import project.unitracker.utility.uimodel.LabelUtility;
import project.unitracker.utility.validator.Filter;
import project.unitracker.controller.Controller;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.model.periodic.PeriodicTransactionHandler;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.psql.PseudoSQLError;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// Controller della finestra modale add-periodic.fxml
public class AddPeriodicController implements Controller {

    @FXML
    private Button addButton;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea description;

    @FXML
    private Label errorMessage;

    @FXML
    private TextField number;

    @FXML
    private ChoiceBox<String> periodChoiceBox;

    @FXML
    private TextField quantity;

    @FXML
    private TextField chartCode;

    @FXML
    // Una volta inseriti tutti i dati, viene chiamato PeriodicTransactionHandler che aggiunge al file data/periodic.txt
    // una nuova transazione periodica con i valori forniti in input
    void addPeriodic(ActionEvent event) {
        String code = chartCode.getText();
        String periodicity = number.getText() + periodChoiceBox.getSelectionModel().getSelectedItem();
        String qty = quantity.getText();
        String date = (datePicker.getValue() != null) ? datePicker.getValue().toString() : LocalDate.now().toString();
        String desc = (description.getText().isEmpty()) ? null : description.getText();

        PeriodicTransactionHandler.add(code, periodicity, qty, date, desc);
        goBack(null);
    }

    @FXML
    void goBack(ActionEvent event) {
        ( (Stage) chartCode.getScene().getWindow() ).close();
    }

    // Converte i valori in un ParsedCommand, per permettere l'analisi semantica dell'input
    private ParsedCommand createCommand() {
        String code = chartCode.getText();
        String qty = quantity.getText();
        String date = (datePicker.getValue() != null) ? datePicker.getValue().toString() : LocalDate.now().toString();
        String desc = (description.getText().isEmpty()) ? null : description.getText();

        return new ParsedCommand("ADD", Arrays.asList(code, qty, date, desc), true);
    }

    @FXML
    // Viene chiamato a ogni input da tastiera su qualsiasi TextField
    void checkSemanticError(KeyEvent event) {
        // Impedisce di eseguire se tutti i campi non sono stati inseriti
        if (chartCode.getText().isEmpty() || quantity.getText().isEmpty() || number.getText().isEmpty() ) {
            LabelButtonHandler.updateLabelButton(errorMessage, addButton, false);
            return;
        }

        PseudoSQLError error = DatabaseHandler.getInstance().checkSemanticError( createCommand() );

        // Se il comando genera un errore semantico, lo segnala
        if (error != null)
            LabelButtonHandler.updateLabelButton(errorMessage, addButton, error.toString(), false);
        // altrimenti viene abilitata la possibilità di eseguire il comando
        else
            LabelButtonHandler.updateLabelButton(errorMessage, addButton, true);
    }

    @FXML
    void initialize() {
        // Aggiunge i quattro periodi possibili alla ChoiceBox
        periodChoiceBox.getItems().addAll("D", "W", "M", "Y");
        periodChoiceBox.setValue("D");

        datePicker.setValue(LocalDate.now());
        // Impedisce che l'utente scriva all'interno
        datePicker.getEditor().setDisable(true);
        datePicker.getEditor().setOpacity(1);

        chartCode.setTextFormatter(new TextFormatter<>(Filter.noCommaFilter));
        quantity.setTextFormatter(new TextFormatter<>(Filter.quantityFilter));
        number.setTextFormatter(new TextFormatter<>(Filter.integerFilter));
    }

    @Override
    public void setParameters(List<Object> parameters) {}

    @Override
    public void reload() {}

    @Override
    public void onClosed() {}
}

