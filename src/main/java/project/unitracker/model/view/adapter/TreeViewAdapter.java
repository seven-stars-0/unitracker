package project.unitracker.model.view.adapter;

import javafx.scene.control.TreeItem;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.uimodel.HierarchyNode;
import project.unitracker.utility.uimodel.TreeEntry;

// La classe che si occupa di costruire la gerarchia grafica di gruppi e grafici
// Viene usata da GroupTreeModel
public class TreeViewAdapter {

    // Converte in modo ricorsivo la gerarchia di HierarchyNode in TreeItem<TreeEntry>
    private static TreeItem<TreeEntry> buildTree(HierarchyNode node) {
        // Viene chiamato il metodo di HierarchyNode che converte se stesso in TreeEntry
        TreeItem<TreeEntry> treeItem = new TreeItem<>( node.toTreeEntry() );

        // Se non è un grafico, è probabile che abbia dei figli, che siano grafici o gruppi
        // Questa parte li aggiunge
        if (! node.isChart() )
            for (HierarchyNode child : node.getSubNodes())
                treeItem.getChildren().add(buildTree(child));

        return treeItem;
    }

    // Restituisce il TreeItem<TreeEntry> del gruppo ROOT
    public static TreeItem<TreeEntry> getRoot() {
        HierarchyNode hierarchyRoot = DatabaseHandler.getInstance().getHierarchy();

        return buildTree(hierarchyRoot);
    }
}
