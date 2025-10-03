package project.unitracker.model.view.componentmodel;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import project.unitracker.utility.constant.URL;
import project.unitracker.utility.uimodel.ViewTuple;
import project.unitracker.controller.Controller;
import project.unitracker.model.eventbus.CoherenceBus;
import project.unitracker.model.view.StageHandler;
import project.unitracker.utility.uimodel.TreeEntry;

import java.util.HashMap;
import java.util.List;

public class TabPaneModel {
    private TabPane tabPane;
    private static TabPaneModel instance;

    private final HashMap<String, ChartTabModel> tabModels;
    private final HashMap<String, Tab> chartTabs;
    private final HashMap<String, Tab> genericTabs;

    private TabPaneModel() {
        tabModels = new HashMap<>();
        chartTabs = new HashMap<>();
        genericTabs = new HashMap<>();

        CoherenceBus.getInstance().register(this);
    }

    public static TabPaneModel getInstance() {
        if ( instance == null ) instance = new TabPaneModel();
        return instance;
    }

    // Viene chiamato da MainController, che gli passa il TabPane sul quale verranno aperte e chiuse le Tab
    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;

        // Ogni volta che una Tab viene selezionata, ricarica il grafico (se invalido)
        // Questo serve a garantire la coerenza con il database nel caso venga aggiornata da comandi pSQL mentre è aperta
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, tab) -> {
            if (tab == null) return;

            Controller controller = (Controller) tab.getUserData();
            if (controller != null)
                controller.reload();
        });
    }

    // Se la tab è già aperta, la seleziona
    // Questo nei casi in cui si cerca di aprire lo stesso grafico (o tab generica) due volte
    // Al posto di aprire due tab separate semplicemente mette in evidenza quella che si cerca di aprire
    private boolean checkAlreadyOpened(String id, boolean chart) {
        HashMap<String, Tab> setToCheck = (chart) ? chartTabs : genericTabs;

        if ( setToCheck.containsKey(id) ) {
            tabPane.getSelectionModel().select( setToCheck.get(id) );
            return true;
        }
        return false;
    }

    // Crea una Tab e assegna come user data il proprio controller
    // Questo serve per passare parametri più volte nel suo ciclo di vita, e non solo all'inizio
    private Tab tabMaker(String FXML, String title, List<Object> parameters) {
        ViewTuple tuple = StageHandler.getRoot(FXML, parameters);

        Tab newTab = new Tab(title, tuple.root());
        newTab.setUserData(tuple.controller());

        return newTab;
    }

    // Si occupa di aprire le Tab associate a dei grafici, e il rispettivo ChartTabModel
    public void openGraphicTab(TreeEntry node) {
        // Se si cerca di aprire una Tab di un grafico già aperto
        if ( checkAlreadyOpened(node.getCode(), true ) ) return;

        String code = node.getCode();
        String unit = node.getUnit();

        ChartTabModel model = new ChartTabModel(code, unit);

        Tab newTab = tabMaker(URL.CHART_FXML, code, List.of(model));
        // Quando viene cancellata viene eliminato anche il ChartTabModel corrispondente
        newTab.setOnClosed(event -> deleteTab(code, true) );

        tabModels.put(code, model);
        chartTabs.put(code, newTab);

        addTab(newTab);
    }

    // Apre delle Tab generiche senza model associato
    public void openGenericTab(String FXML, String title, List<Object> parameters) {
        Tab newTab = (checkAlreadyOpened(title, false)) ?
                genericTabs.get(title) :
                tabMaker(FXML, title, null);

        ((Controller) newTab.getUserData()).setParameters(parameters);

        // Se è già aperta non c'è bisogno di proseguire
        if (genericTabs.containsKey(title) )
            return;

        newTab.setOnClosed(event -> deleteTab(title, false) );
        genericTabs.put(title, newTab);
        addTab(newTab);
    }

    // Aggiunge la Tab al TabPane, rendendola visibile
    private void addTab(Tab tab) {
        tabPane.toFront();
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    // Chiude la Tab identificata da code
    // Se la Tab è relativa a un grafico, allora elimina anche il ChartTabModel associato
    public void deleteTab(String code, boolean chart) {
        HashMap<String, Tab> setToCheck = (chart) ? chartTabs : genericTabs;

        setToCheck.remove(code);
        if (chart) tabModels.remove(code);

        // Riporta indietro il TabPane per mostrare l'AnchorPane
        if (tabModels.isEmpty() && genericTabs.isEmpty())
            tabPane.toBack();
    }

    // Usato per mantenere la coerenza con il database
    // Quando un gruppo viene eliminato, tutte i grafici al suo interno vengono eliminati, quindi bisogna chiuderli
    // ed eliminare il loro model (se presente)
    public void closeDeletedCharts(List<String> chartCodes) {
        for (String code : chartCodes) {
            if ( ! chartTabs.containsKey(code))
                continue;

            tabPane.getTabs().remove(chartTabs.get(code));
            chartTabs.remove(code);
            deleteTab(code, true);
        }
    }

    // Chiamato tramite CoherenceBus da DatabaseHandler.CommandExecutor
    // per garantire la coerenza dei grafici con i dati del database
    public void invalidateCoherence(List<String> chartCodes, boolean appliedToo) {
        for (String code : chartCodes)
            if (tabModels.containsKey(code))
                tabModels.get(code).invalidateCoherence(appliedToo);
    }

}
