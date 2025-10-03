package project.unitracker.model.eventbus;

import project.unitracker.model.view.componentmodel.GroupTreeModel;

// Il singleton che fa da tramite tra CommandExecutor e GroupTreeModel
// Segnala cambiamenti di qualsiasi tipo nella gerarchia di gruppi e grafici
public class HierarchyBus {
    private static HierarchyBus instance;
    private GroupTreeModel model;

    private HierarchyBus() {}

    public static HierarchyBus getInstance() {
        if ( instance == null ) instance = new HierarchyBus();
        return instance;
    }

    // Ricarica il model in modo da fargli prendere dati coerenti dal database
    public void signalIncoherence() { model.reload(); }

    public void register(GroupTreeModel model) { this.model = model; }
}
