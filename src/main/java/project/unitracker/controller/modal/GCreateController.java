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

// Il controller della finestra modale g-create.fxml
public class GCreateController implements Controller {

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

    // Genera un ParsedCommand con l'input fornito
    private ParsedCommand createCommand() {
        String code = codeField.getText();
        String supergroup = supergroupField.getText();
        String name = nameField.getText();

        return new ParsedCommand(
                "G_CREATE",
                List.of(code, supergroup, name),
                false
        );
    }

    @FXML
    // Viene chiamato a ogni input da tastiera su qualsiasi TextField
    void checkSemanticError(KeyEvent event) {
        errorMessage.getStyleClass().clear();

        // Impedisce l'esecuzione se tutti i campi non sono riempiti
        if (codeField.getText().isEmpty() || supergroupField.getText().isEmpty() || nameField.getText().isEmpty()) {
            LabelButtonHandler.updateLabelButton(errorMessage, createButton, false);
            return;
        }

        // Verifica eventuali errori semantici
        PseudoSQLError error = DatabaseHandler.getInstance().checkSemanticError( createCommand() );

        // Se ci sono errori, vengono mostrati a schermo e viene impedita l'esecuzione
        if (error != null)
            LabelButtonHandler.updateLabelButton(errorMessage, createButton, error.toString(), false);
        // altrimenti viene permessa
        else
            LabelButtonHandler.updateLabelButton(errorMessage, createButton, true);
    }


    @FXML
    // Crea effettivamente il nuovo gruppo
    void create(ActionEvent event) {
        DatabaseHandler.getInstance().executeCommand( createCommand() );
        goBack(null);
    }

    @FXML
    // Chiude la finestra
    void goBack(ActionEvent event) {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    void initialize() {
        // Impone il vincolo di non inserire la virgola nei campi
        codeField.setTextFormatter(new TextFormatter<>(Filter.noCommaFilter));
        nameField.setTextFormatter(new TextFormatter<>(Filter.noCommaFilter));
    }

    @Override
    // Questo parametro sarà il supergruppo di default dato da GroupTreeModel.nearestGroup()
    public void setParameters(List<Object> parameters) {
        supergroupField.setText( (String) parameters.getFirst() );
    }

    @Override
    public void reload() {}

    @Override
    public void onClosed() {}
}
