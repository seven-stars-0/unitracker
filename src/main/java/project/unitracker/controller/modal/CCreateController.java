package project.unitracker.controller.modal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import project.unitracker.model.view.componentmodel.LabelButtonHandler;
import project.unitracker.utility.uimodel.LabelUtility;
import project.unitracker.utility.validator.Filter;
import project.unitracker.controller.Controller;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.psql.PseudoSQLError;

import java.util.List;

// Controller della finestra modale c-create.fxml
public class CCreateController implements Controller {

    @FXML
    private TextField codeField;

    @FXML
    private Button createButton;

    @FXML
    private Label errorMessage;

    @FXML
    private TextField nameField;

    @FXML
    private TextField supergroupField;

    @FXML
    private TextField unitField;

    private ParsedCommand createCommand() {
        String code = codeField.getText();
        String supergroup = supergroupField.getText();
        String name = nameField.getText();
        String unit = unitField.getText();

        return new ParsedCommand(
                "C_CREATE",
                List.of(code, supergroup, name, unit),
                false
        );
    }

    @FXML
    // Viene chiamato a ogni input da tastiera su qualsiasi TextField
    void checkSemanticError(KeyEvent event) {
        // Se tutti i parametri non sono inseriti, impedisce di eseguire
        if (codeField.getText().isEmpty() || supergroupField.getText().isEmpty() || nameField.getText().isEmpty() || unitField.getText().isEmpty() ) {
            LabelButtonHandler.updateLabelButton(errorMessage, createButton, false);
            return;
        }

        // Verifica la presenza di errori semantici
        PseudoSQLError error = DatabaseHandler.getInstance().checkSemanticError( createCommand() );

        // Se ci sono errori, li segnala a schermo
        if (error != null)
            LabelButtonHandler.updateLabelButton(errorMessage, createButton, error.toString(), false);
        // altrimenti abilita l'esecuzione del comando
        else
            LabelButtonHandler.updateLabelButton(errorMessage, createButton, true);
    }

    @FXML
    // Esegue il comando
    void create(ActionEvent event) {
        DatabaseHandler.getInstance().executeCommand( createCommand() );
        goBack(null);
    }

    @FXML
    // Chiude la finestra modale
    void goBack(ActionEvent event) {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        // Applica filtri per impedire formato invalido
        // Nessuno dei campi indicati permette di scrivere la virgola
        codeField.setTextFormatter(new TextFormatter<>(Filter.noCommaFilter));
        nameField.setTextFormatter(new TextFormatter<>(Filter.noCommaFilter));
        unitField.setTextFormatter(new TextFormatter<>(Filter.noCommaFilter));
    }

    @Override
    public void setParameters(List<Object> parameters) {
        // Il parametro sarà il gruppo di default, preso da GroupTreeModel.nearestGroup()
        supergroupField.setText( (String) parameters.getFirst() );
    }

    @Override
    public void reload() {}

    @Override
    public void onClosed() {}
}
