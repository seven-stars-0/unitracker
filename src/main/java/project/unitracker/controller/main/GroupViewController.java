package project.unitracker.controller.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import project.unitracker.controller.Controller;
import project.unitracker.model.view.componentmodel.GroupTreeModel;
import project.unitracker.model.view.StageHandler;
import project.unitracker.utility.uimodel.TreeEntry;
import project.unitracker.utility.constant.URL;

import java.util.List;

// Controller di group-tree.fxml
// Si occupa di creare grafici e gruppi. Il resto del lavoro, che richiede logica applicativa, è affidato a
// GroupTreeModel, che aggiunge TreeItem alla TreeView e tiene traccia del nodo attualmente selezionato dall'utente
public class GroupViewController implements Controller {

    @FXML
    private TreeView<TreeEntry> groupView;


    @FXML
    // Apre la finestra modale che permette di creare un nuovo grafico
    // Come supergruppo di default viene selezionato nearestGroup() dal GroupTreeModel
    void createChart(ActionEvent event) {
        Stage owner = (Stage) groupView.getScene().getWindow();
        StageHandler.openModalWindow(URL.C_CREATE_FXML, "Aggiungi grafico", owner, List.of(GroupTreeModel.getInstance().nearestGroup()));
    }

    @FXML
    // Apre la finestra modale che permette di creare un nuovo gruppo
    // Come supergruppo di default viene selezionato nearestGroup() dal GroupTreeModel
    void createGroup(ActionEvent event) {
        Stage owner = (Stage) groupView.getScene().getWindow();
        StageHandler.openModalWindow(URL.G_CREATE_FXML, "Aggiungi gruppo", owner, List.of(GroupTreeModel.getInstance().nearestGroup()));
    }

    @FXML
    void initialize() {
        // Inizializza il GroupTreeModel, che popola groupView con la gerarchia di gruppi e grafici
        GroupTreeModel.getInstance().initialize(groupView);
    }

    @Override
    public void onClosed() {}

    @Override
    public void reload() {}

    @Override
    public void setParameters(List<Object> parameters) {}
}
