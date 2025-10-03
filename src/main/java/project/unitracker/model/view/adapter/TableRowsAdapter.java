package project.unitracker.model.view.adapter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import project.unitracker.utility.psql.CommandValidationResult;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.model.psql.PseudoSQLHandler;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.uimodel.TableViewRow;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// La classe che si occupa di convertire i dati del database in TableViewRow, ossia i tipi
// di dati che usano le TableView in ChartDataController e in PeriodicController
public class TableRowsAdapter {

    // Il pattern che le righe di data/periodic.txt devono rispettare per venire eseguite
    private static final Pattern PERIODIC = Pattern.compile("^\\s*(\\d+[YMWD])\\s*:\\s*(ADD.+)\\s*$");

    // Questo viene chiamato da ChartTabModel per ChartDataController
    public static ObservableList<TableViewRow> getChartRows(String code) {
        List<TableViewRow> tableData = DatabaseHandler.getInstance().getLineChartData(code);

        return FXCollections.observableList(tableData);
    }

    // Questo viene chiamato da PeriodicTransactionHandler per PeriodicController
    public static List<TableViewRow> getPeriodicRows(List<String> allLines) {
        List<TableViewRow> periodicRows = new ArrayList<>();

        for (String line : allLines) {
            Matcher matcher = PERIODIC.matcher(line);

            // Eventuali righe che non seguono il Pattern vengono ignorate
            if (!matcher.find())
                continue;

            String frequenza = matcher.group(1);
            String pSQLCommand = matcher.group(2);

            // Il comando viene interpretato
            CommandValidationResult result = PseudoSQLHandler.validateCommand( pSQLCommand );

            // Eventuali errori di sintassi e semantica (ad esempio grafici inesistenti) vengono ignorati
            if (result.error() != null)
                continue;

            ParsedCommand cmd = result.command();

            // --- Dati per creare la TableViewRow ---
            String code = cmd.parameters.getFirst();
            Double quantita = Double.parseDouble(cmd.parameters.get(1));
            String data = cmd.parameters.get(2);
            String descrizione = cmd.parameters.getLast();

            periodicRows.add(new TableViewRow(code, frequenza, quantita, data, descrizione));
        }
        return periodicRows;
    }
}
