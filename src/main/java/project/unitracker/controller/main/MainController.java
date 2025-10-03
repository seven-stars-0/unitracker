package project.unitracker.controller.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import project.unitracker.controller.Controller;
import project.unitracker.model.io.BackupHandler;
import project.unitracker.model.view.componentmodel.TabPaneModel;
import project.unitracker.utility.constant.URL;
import project.unitracker.model.eventbus.ThemeBus;
import project.unitracker.model.view.StageHandler;

import java.util.ArrayList;
import java.util.List;

// Controller di main.fxml
public class MainController implements Controller {

    @FXML
    private SplitPane centerSplitPane;

    @FXML
    private StackPane stackPane;

    @FXML
    private TabPane tabPane;

    // Il root del file group-tree.fxml
    private BorderPane treeViewRoot;

    // Usata per ricordare l'ultima posizione del divider di centerSplitPane
    // in modo che a ogni toggle venga ripristinata. Di default è 0.2
    private Double lastDividerPosition = 0.2;


    @FXML
    // L'unico punto della applicazione dove è possibile cambiare tema, passando da Light a Dark, e vice versa
    // Tramite il ThemeBus tutte le finestre aperte verranno notificate del cambiamento
    void changeTheme(ActionEvent event) {
        StageHandler.changeTheme();
    }

    @FXML
    // Apre la finestra dei crediti
    void generalDescription(ActionEvent event) {
        StageHandler.openWindow(URL.CREDITS_FXML, "Crediti", null, null);
    }

    @FXML
    // Apre o seleziona la tab per eseguire comandi PseudoSQL
    void pseudoSQLToggle(ActionEvent event) {
        TabPaneModel.getInstance().openGenericTab(URL.PSQL_FXML ,"PseudoSQL", null);
    }

    @FXML
    // Apre la finestra del tutorial di PseudoSQL
    void pseudoSQLTutorial(ActionEvent event) {
        StageHandler.openWindow(URL.TUTORIAL_FXML, "PSQL Tutorial", null, null);
    }

    @FXML
    // Chiamata da Backup > Importa PSQL
    // Selezionato un file, viene aperta o selezionata la tab di PSQL e vengono inserite
    // le righe del file selezionato nella TextArea, pronte all'esecuzione
    void importFile(ActionEvent event) {
        Stage owner = (Stage) stackPane.getScene().getWindow();
        List<String> fileContent = BackupHandler.readBackupFile(owner);

        if (fileContent == null)
            return;

        // Qui convertiamo List<String> in List<Object>
        TabPaneModel.getInstance().openGenericTab(URL.PSQL_FXML, "PseudoSQL", new ArrayList<>(fileContent));
    }

    @FXML
    // Chiamata da Backup > Esporta tutto
    // Crea un backup di tutto il database in formato PSQL
    void exportAll(ActionEvent event) {
        BackupHandler.createBackupFile((Stage) stackPane.getScene().getWindow(), "ROOT", false);
    }

    @FXML
    // Chiamata da Backup > Importa periodico
    // Permette di selezionare un file, il cui contenuto viene aggiunto al file data/periodic.txt
    // Eventuali errori di sintassi o semantica vengono ignorati
    void importPeriodic(ActionEvent event) {
        BackupHandler.readPeriodicBackup((Stage) stackPane.getScene().getWindow());
    }

    @FXML
    // Chiamata da Backup > Esporta periodico
    // Crea una copia del file data/periodic.txt
    void exportPeriodic(ActionEvent event) {
        BackupHandler.createPeriodicBackup((Stage) stackPane.getScene().getWindow());
    }

    @FXML
    // Apre o seleziona la tab che mostra le transazioni periodiche
    void showPeriodicTransactions(ActionEvent event) {
        TabPaneModel.getInstance().openGenericTab(URL.PERIODIC_FXML ,"Transazioni periodiche", null);
    }

    @FXML
    // Aggiunge o toglie il menù laterale che mostra la gerarchia di gruppi e grafici
    // Se tolto, l'ultima posizione del divider viene salvata in modo da essere ripristinata alla successiva aggiunta
    void toggleTreeView(ActionEvent event) {
        if (centerSplitPane.getItems().contains(treeViewRoot)) {
            // Ricorda l'ultima posizione del divider
            lastDividerPosition = centerSplitPane.getDividerPositions()[0];
            centerSplitPane.getItems().remove(treeViewRoot);
        }
        else {
            centerSplitPane.getItems().addFirst(treeViewRoot);
            // Ripristina l'ultima posizione del divider
            centerSplitPane.setDividerPositions(lastDividerPosition);
        }
    }

    @FXML
    void initialize() {
        // Iscrive se stesso al ThemeBus, per essere notificato dei cambi di tema
        ThemeBus.getInstance().register(this);

        // Inizializza il TabPaneModel, che gestisce le Tab del TabPane e gli eventi di CoherenceBus
        TabPaneModel.getInstance().setTabPane(tabPane);

        // Carica group-tree.fxml, rendendola immediatamente disponibile all'evenienza
        treeViewRoot = (BorderPane) StageHandler.getRoot(URL.GROUP_VIEW_FXML, null).root();
    }

    @Override
    // Quando la finestra viene chiusa si cancella dal ThemeBus
    public void onClosed() {
        ThemeBus.getInstance().unregister(this);
    }

    @Override
    // Non servono parametri per questo controller
    public void setParameters(List<Object> parameters) {}

    @Override
    // Viene chiamato dal ThemeBus per cambiare il CSS associato alla scena
    public void reload() {
        StageHandler.applyTheme(stackPane.getScene());
    }
}
