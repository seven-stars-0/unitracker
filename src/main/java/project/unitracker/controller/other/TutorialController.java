package project.unitracker.controller.other;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import project.unitracker.controller.Controller;
import project.unitracker.model.eventbus.ThemeBus;
import project.unitracker.model.view.StageHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Il controller della finestra psql-tutorial.fxml
// Descrive il funzionamento di ogni comando PSQL, selezionabile da una TreeView che viene popolata qui
// Le descrizioni effettive dei comandi sono contenute nelle VBox, definite a livello di FXML
public class TutorialController implements Controller {

    @FXML private VBox ADD;
    @FXML private VBox APPLY;
    @FXML private VBox APPLY_ALL;
    @FXML private VBox A_APPLY;
    @FXML private VBox A_CANCEL;
    @FXML private VBox COMPLETE_DROP;
    @FXML private VBox C_CREATE;
    @FXML private VBox C_DROP;
    @FXML private VBox C_MOVE;
    @FXML private VBox C_RENAME;
    @FXML private VBox DELETE;
    @FXML private VBox D_APPLY;
    @FXML private VBox D_CANCEL;
    @FXML private VBox G_CREATE;
    @FXML private VBox G_DROP;
    @FXML private VBox G_MOVE;
    @FXML private VBox G_RENAME;
    @FXML private VBox INTRO;
    @FXML private VBox SIGNATURE;
    @FXML private VBox CANCEL;
    @FXML private VBox CANCEL_ALL;

    @FXML private TreeView<String> treeView;

    private VBox currentTutorial;

    // Uso la LinkedHashMap per preservare l'ordine di inserimento, poiché per popolare la TreeView
    // volevo mantenere lo stesso ordine della signature descritta in SIGNATURE
    private final Map<String, VBox> tutorialMap = new LinkedHashMap<>();

    @FXML
    void initialize() {
        // Si iscrive a ThemeBus per eventuali cambi di tema
        ThemeBus.getInstance().register(this);

        // Popola la mappa
        tutorialMap.put("INTRO", INTRO);
        tutorialMap.put("SIGNATURE", SIGNATURE);
        tutorialMap.put("G_CREATE", G_CREATE);
        tutorialMap.put("G_DROP", G_DROP);
        tutorialMap.put("G_MOVE", G_MOVE);
        tutorialMap.put("G_RENAME", G_RENAME);
        tutorialMap.put("C_CREATE", C_CREATE);
        tutorialMap.put("C_DROP", C_DROP);
        tutorialMap.put("C_MOVE", C_MOVE);
        tutorialMap.put("C_RENAME", C_RENAME);
        tutorialMap.put("ADD", ADD);
        tutorialMap.put("DELETE", DELETE);
        tutorialMap.put("COMPLETE_DROP", COMPLETE_DROP);
        tutorialMap.put("APPLY_ALL", APPLY_ALL);
        tutorialMap.put("CANCEL_ALL", CANCEL_ALL);
        tutorialMap.put("APPLY", APPLY);
        tutorialMap.put("CANCEL", CANCEL);
        tutorialMap.put("A_APPLY", A_APPLY);
        tutorialMap.put("A_CANCEL", A_CANCEL);
        tutorialMap.put("D_APPLY", D_APPLY);
        tutorialMap.put("D_CANCEL", D_CANCEL);

        // Settiamo quello iniziale
        currentTutorial = INTRO;

        // Popoliamo la treeView
        TreeItem<String> rootItem = new TreeItem<>("Tutorial");
        for (String key : tutorialMap.keySet())
            rootItem.getChildren().add(new TreeItem<>(key));

        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        // Quando viene premuto un TreeItem, la schermata mostra il tutorial relativo al valore dell'item
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                switchTo(newSel.getValue());
            }
        });
    }

    // Il tutorial selezionato viene messo in evidenza nello StackPane che contiene tutte le VBox,
    // portando indietro currentTutorial
    private void switchTo(String name) {
        VBox next = tutorialMap.get(name);

        if (next == null || next == currentTutorial)
            return;

        if (currentTutorial != null)
            currentTutorial.setVisible(false);

        next.setVisible(true);
        next.toFront();
        currentTutorial = next;
    }

    @Override
    // Cancella se stesso da ThemeBus alla chiusura della finestra
    public void onClosed() {
        ThemeBus.getInstance().unregister(this);
    }

    @Override
    public void setParameters(List<Object> parameters) {

    }

    @Override
    // Chiamato da ThemeBus per segnalare un cambiamento di tema
    public void reload() {
        StageHandler.applyTheme(treeView.getScene());
    }
}
