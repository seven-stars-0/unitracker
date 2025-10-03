package project.unitracker.model.view.componentmodel;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import project.unitracker.controller.Controller;
import project.unitracker.model.view.adapter.ChartXYAdapter;
import project.unitracker.model.view.adapter.TableRowsAdapter;
import project.unitracker.utility.uimodel.TableViewRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Il model relativo alle Tab dei grafici
//
// Mantiene i dati dei grafici relativi a vari periodi in memoria, finché la Tab non viene chiusa o la loro coerenza
// con il database viene persa
// Nel secondo caso i dati salvati vengono cancellati e ricaricati all'evenienza, lazily
public class ChartTabModel {
    private final String code;
    private final String unit;

    private Map<String, XYChart.Series<String, Number>> appliedCharts;
    private Map<String, XYChart.Series<String, Number>> allDataCharts;
    private ObservableList<TableViewRow> rows;

    // Serve a chiamare reload() di tutti i controller a esso associati quando avviene
    // l'invalidazione della coerenza
    private final ArrayList<Controller> linkedControllers;

    // Richiede i grafici solo se non sono già presenti
    // La coerenza con il database è gestita nel metodo getGraph
    private void putGraph(String period, boolean appliedOnly) {
        Map<String, XYChart.Series<String, Number>> targetMap = (appliedOnly) ? appliedCharts : allDataCharts;

        if ( ! targetMap.containsKey(period) ) {
            targetMap.put(period, ChartXYAdapter.getXYSeries(this.code, period, appliedOnly));
            targetMap.get(period).setName(unit);
        }
    }

    public ChartTabModel(String code, String unit) {
        this.code = code;
        this.unit = unit;

        appliedCharts = new HashMap<>();
        allDataCharts = new HashMap<>();
        rows = TableRowsAdapter.getChartRows(code);

        linkedControllers = new ArrayList<>();
    }

    // Prende il grafico del periodo selezionato
    // Se non presente in memoria, oppure non coerente con i dati nel database, viene chiesto di crearlo da zero
    // Segue un approccio lazy, ricalcolando solo i grafici di cui ha bisogno, solo quando ne ha bisogno
    public XYChart.Series<String, Number> getGraph(String period, boolean appliedOnly) {
        putGraph(period, appliedOnly);

        return (appliedOnly) ? appliedCharts.get(period) : allDataCharts.get(period);
    }

    // Chiamata da ChartDataController, serve per
    public ObservableList<TableViewRow> getRows() {
        rows = TableRowsAdapter.getChartRows(code);

        return rows;
    }

    //
    // I dati appliedOnly cambiano solo se la modifica apportata è permanente, ma allData cambia a prescindere
    public void invalidateCoherence(boolean appliedToo) {
        allDataCharts = new HashMap<>();
        if (appliedToo) appliedCharts = new HashMap<>();

        signalReload();
    }

    // Segnala ai propri controller di dover aggiornare la loro vista per mantenere la coerenza con il database
    private void signalReload() {
        for (Controller controller : linkedControllers)
            controller.reload();
    }

    // I controller ChartController e ChartDataController si registrano per venire segnalati in caso di modifiche
    // di dati
    public void register(Controller controller) { linkedControllers.add(controller); }

    public String getCode() { return code; }

    public String getUnit() { return unit; }
}
