package project.unitracker.model.eventbus;

import project.unitracker.model.view.componentmodel.TabPaneModel;

import java.util.ArrayList;
import java.util.List;

// Il singleton che fa da tramite tra CommandExecutor e TabPaneModel
// Serve a garantire la coerenza tra i dati del database e i dati mostrati dai grafici
// Aiuta in questo scopo chiamando i metodi del TabPaneModel che si occupano di inoltrare l'invalidazione
// ai ChartTabModel
public class CoherenceBus {
    private static CoherenceBus instance;
    private static TabPaneModel model;

    private CoherenceBus() {}

    public static CoherenceBus getInstance() {
        if (instance == null) instance = new CoherenceBus();
        return instance;
    }

    // Segnala l'eliminazione di un grafico, eventualmente per chiudere la Tab a esso associata
    public void signalDelete(List<String> chartCodes) {
        if (model == null) return;

        model.closeDeletedCharts(chartCodes);
    }

    // Segnala l'incoerenza con i dati del database
    // In caso appliedToo = false, vengono invalidati solo i dati allData, mentre quelli applied rimangono invariati
    public void signalIncoherence(ArrayList<String> chartCodes, boolean appliedToo) {
        if (model == null) return;

        model.invalidateCoherence(chartCodes, appliedToo);
    }

    public void register(TabPaneModel model) { this.model = model; }
}
