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
import project.unitracker.utility.validator.Filter;
import project.unitracker.controller.Controller;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.psql.PseudoSQLError;
import project.unitracker.utility.uimodel.TreeEntry;

import java.util.Arrays;
import java.util.List;

// Il controller della finestra modale rename-move.fxml
// Agisce da (C|G)_(RENAME|MOVE) in base ai parametri ricevuti
public class RenameMoveController implements Controller {

    @FXML
    private Label errorMessage;

    @FXML
    private Label oldLabel;

    @FXML
    private Label newLabel;

    @FXML
    // Questo viene disabilitato non appena popolato dai parametri ricevuti
    // Il suo scopo è solo quello di mostrare il valore precedente
    private TextField oldField;

    @FXML
    private TextField newField;

    @FXML
    private Button executeButton;

    // Contengono le informazioni sul nodo selezionato e sul genitore del nodo selezionato
    // se superNodeInfo non viene fornito come parametro, il controller agisce come RENAME, altrimenti come MOVE
    private TreeEntry nodeInfo, superNodeInfo;

    // Genera il comando
    private ParsedCommand createCommand() {
        String type = ( (nodeInfo.isChart()) ? "C" : "G" ) + ( (superNodeInfo == null) ? "_RENAME" : "_MOVE" );

        return new ParsedCommand(
                type,
                Arrays.asList(nodeInfo.getCode(), newField.getText()),
                false
        );
    }

    @FXML
    // Chiamato a ogni input da tastiera su newField
    void checkSemanticError(KeyEvent event) {
        // Non posso fare .clear() altrimenti viene cancellata anche la classe di base
        newField.getStyleClass().removeAll("error-field", "success-field");

        // Se vuoto, segnala di dover riempire il campo
        if ( newField.getText().isEmpty() ) {
            newField.getStyleClass().add("error-field");
            LabelButtonHandler.updateLabelButton(errorMessage, executeButton, false);
            return;
        }

        // Verifica eventuali errori
        PseudoSQLError error = DatabaseHandler.getInstance().checkSemanticError( createCommand() );

        // Se non vi sono errori, rende il bordo di newField verde, elimina eventuali errorMessage e abilita l'esecuzione
        if ( error == null ) {
            newField.getStyleClass().add("success-field");
            LabelButtonHandler.updateLabelButton(errorMessage, executeButton, true);
        }
        // Se ci sono errori, vengono mostrati a schermo, il bordo di newField diventa rosso e viene impedita l'esecuzione
        else {
            newField.getStyleClass().add("error-field");
            LabelButtonHandler.updateLabelButton(errorMessage, executeButton, error.toString(), false);
        }
    }

    @FXML
    // Esegue il comando
    void execute(ActionEvent event) {
        DatabaseHandler.getInstance().executeCommand( createCommand() );

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
        // Impedisce di scrivere la virgola
        newField.setTextFormatter(new TextFormatter<>(Filter.noCommaFilter));
    }

    @Override
    // In base a quanti parametri vengono passati, si comporta da RENAME o da MOVE
    // Con un solo parametro RENAME, con due MOVE
    public void setParameters(List<Object> parameters) {
        String oldLabelText = "Vecchio ", newLabelText = "Nuovo ";
        nodeInfo = (TreeEntry) parameters.getFirst();

        // Caso RENAME
        if ( parameters.size() == 1 ) {
            superNodeInfo = null;

            oldLabelText += "nome";
            newLabelText += "nome";

            oldField.setText(nodeInfo.getName());
        }
        // Case MOVE
        else {
            superNodeInfo = (TreeEntry) parameters.getLast();

            oldLabelText += "supergruppo";
            newLabelText += "supergruppo";

            oldField.setText(superNodeInfo.getCode());
        }

        oldLabel.setText(oldLabelText);
        newLabel.setText(newLabelText);

        // Questo è solo per mostrare il valore precedente, quindi viene impedito modificarlo
        oldField.setDisable(true);
    }

    @Override
    public void reload() {}

    @Override
    public void onClosed() {}

}
