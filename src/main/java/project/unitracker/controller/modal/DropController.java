package project.unitracker.controller.modal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import project.unitracker.controller.Controller;
import project.unitracker.model.io.BackupHandler;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.uimodel.TreeEntry;

import java.util.ArrayList;
import java.util.List;

// Il controller della finestra modale drop.fxml
// Funziona sia come C_DROP che come G_DROP
public class DropController implements Controller {

    // Preso da GroupTreeModel (dal quale viene invocata la finestra) rappresenta le informazioni
    // del grafico o del gruppo che vogliamo eliminare
    private TreeEntry nodeInfo;

    @FXML
    private Label messageLabel;

    @FXML
    // Esegue il backup del grafico/gruppo prima di eliminarlo
    // Se l'utente decide di annullarlo o qualcosa va storto, delete() non viene chiamata
    void backupAndDelete(ActionEvent event) {
        Stage stage = (Stage) messageLabel.getScene().getWindow();

        if ( BackupHandler.createBackupFile(stage, nodeInfo.getCode(), nodeInfo.isChart()) )
            delete(null);
    }

    @FXML
    // Elimina il grafico/gruppo
    // Crea un ParsedCommand basandosi sulle informazioni in nodeInfo, per poi eseguirlo
    void delete(ActionEvent event) {
        String type = (nodeInfo.isChart()) ? "C_DROP" : "G_DROP";

        ArrayList<String> parameters = new ArrayList<>();
        parameters.add(nodeInfo.getCode());

        ParsedCommand cmd = new ParsedCommand(type, parameters, false);
        DatabaseHandler.getInstance().executeCommand(cmd);

        goBack(null);
    }

    @FXML
    // Chiude la finestra modale
    void goBack(ActionEvent event) {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.close();
    }

    @Override
    // Viene fornito come parametro il nodo da eliminare
    // Il messaggio mostrato viene adattato in base alla natura del nodo (grafico o gruppo)
    public void setParameters(List<Object> parameters) {
        nodeInfo = (TreeEntry) parameters.getFirst();

        StringBuilder message = new StringBuilder("Vuoi davvero eliminare ");
        if (nodeInfo.isChart()) message.append("il grafico ");
        else message.append("il gruppo ");

        message.append(nodeInfo.getName()).append("-").append(nodeInfo.getCode()).append("?");

        messageLabel.setText(message.toString());
    }

    @Override
    public void reload() {}

    @Override
    public void onClosed() {}
}
