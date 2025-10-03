package project.unitracker.model.view.componentmodel;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import project.unitracker.model.io.BackupHandler;
import project.unitracker.model.eventbus.HierarchyBus;
import project.unitracker.model.view.StageHandler;
import project.unitracker.model.view.adapter.TreeViewAdapter;
import project.unitracker.utility.uimodel.TreeEntry;
import project.unitracker.utility.constant.URL;

import java.util.List;

// Il singleton che gestisce gli eventi della TreeView di main.fxml
public class GroupTreeModel {
    private static GroupTreeModel instance;

    private TreeView<TreeEntry> groupView;

    // --- Salva le informazioni del nodo selezionato e del genitore ---
    private TreeEntry currNodeParent;
    private TreeEntry currNode;

    private GroupTreeModel() {
        HierarchyBus.getInstance().register(this);
    }

    public static GroupTreeModel getInstance() {
        if (instance == null) instance = new GroupTreeModel();
        return instance;
    }

    // Apre la finestra modale rename-move.fxml come RENAME
    private void renameSelected() {
        StageHandler.openModalWindow(URL.RENAME_MOVE_FXML, "Rinomina", (Stage) groupView.getScene().getWindow(), List.of(currNode));
    }

    // Apre la finestra modale rename-move.fxml come MOVE
    private void moveSelected() {
        StageHandler.openModalWindow(URL.RENAME_MOVE_FXML, "Sposta", (Stage) groupView.getScene().getWindow(), List.of(currNode, currNodeParent));
    }

    // Crea un backup del gruppo o del grafico selezionato in formato PSQL
    private void exportSelected() {
        BackupHandler.createBackupFile((Stage) groupView.getScene().getWindow(), currNode.getCode(), currNode.isChart());
    }

    // Apre la finestra modale drop.fxml
    private void deleteSelected() {
        StageHandler.openModalWindow(URL.DROP_FXML, "Elimina", (Stage) groupView.getScene().getWindow(), List.of(currNode));
    }

    // Restituisce il gruppo più vicino al nodo selezionato
    // Se è stato selezionato un gruppo, restituisce il gruppo stesso
    // Se è stato selezionato un grafico, restituisce il gruppo di appartenenza
    public String nearestGroup() {
        return (! currNode.isChart()) ? currNode.getCode() : currNodeParent.getCode();
    }

    // Crea un ContextMenu con tutte le opzioni disponibili
    private ContextMenu createContextMenu() {
        MenuItem renameItem = new MenuItem("Rinomina");
        renameItem.setOnAction(e -> renameSelected() );

        MenuItem moveItem = new MenuItem("Sposta");
        moveItem.setOnAction(e-> moveSelected() );

        MenuItem exportItem = new MenuItem("Esporta");
        exportItem.setOnAction(e -> exportSelected() );

        MenuItem deleteItem = new MenuItem("Elimina");
        deleteItem.setOnAction(e -> deleteSelected() );

        return new ContextMenu(renameItem, moveItem, exportItem, deleteItem);
    }

    // Riceve la TreeView da GroupViewController
    public void initialize(TreeView<TreeEntry> treeView) {
        groupView = treeView;
        groupView.setShowRoot(false);

        groupView.setCellFactory(tv -> new TreeCell<>() {

            @Override
            protected void updateItem(TreeEntry entry, boolean empty) {
                super.updateItem(entry, empty);

                if (empty || entry == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    setTooltip(null);
                    return;
                }

                setText(entry.toString());

                String ImageURL = (entry.isChart()) ? URL.CHART_ICON : URL.GROUP_ICON;
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(ImageURL)));
                icon.setFitHeight(16);
                icon.setFitWidth(16);
                setGraphic(icon);

                // --- Context menu ---
                setContextMenu( createContextMenu() );

                // --- Tooltip ---
                setTooltip( new Tooltip("Codice: " + entry.getCode()) );
            }
        });

        // Serve a deselezionare se l'utente non clicca su nessun TreeItem
        groupView.setOnMousePressed(e -> {
            Node n = e.getPickResult().getIntersectedNode();
            // risali finché non trovi una TreeCell (o finisci)
            while ( n != null && ! (n instanceof TreeCell<?>) )
                n = n.getParent();

            // Se il nodo selezionato è nullo o vuoto, viene selezionato ROOT e deselezionato graficamente
            if ( n == null || ((TreeCell<?>) n).isEmpty() ) {
                groupView.getSelectionModel().clearSelection();
                currNode = currNodeParent = groupView.getRoot().getValue();
                e.consume(); // Facciamo in modo che l'evento non venga elaborato ulteriormente
            }
        });

        // Se si fa doppio click su un nodo che rappresenta un grafico, si apre una nuova Tab oppure
        // se già aperta si seleziona
        // Altrimenti semplicemente si seleziona, utile per le operazioni di DELETE, MOVE e RENAME
        groupView.setOnMouseClicked(event -> {
            TreeItem<TreeEntry> selectedItem = groupView.getSelectionModel().getSelectedItem();
            if (selectedItem == null)
                return;

            currNodeParent = selectedItem.getParent().getValue();
            currNode = selectedItem.getValue();

            // Se si fa doppio click su un grafico, questo viene aperto (o selezionato se già aperto)
            if (event.getClickCount() >= 2 && currNode.isChart() )
                TabPaneModel.getInstance().openGraphicTab(currNode);
        });

        reload();
    }

    // Viene chiamato da HierarchyBus per segnalare cambi nella gerarchia
    // Ricarica dal database la gerarchia, mostrandola
    public void reload() {
        groupView.setRoot(TreeViewAdapter.getRoot());
        currNode = currNodeParent = groupView.getRoot().getValue();
    }
}
