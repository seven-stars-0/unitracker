package project.unitracker.model.periodic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import project.unitracker.model.io.FileHandler;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.model.view.adapter.TableRowsAdapter;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.uimodel.TableViewRow;
import project.unitracker.utility.constant.URL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// La classe che gestisce il file data/periodic.txt e l'esecuzione delle transazioni periodiche
// Il metodo tryExecuteAll() viene sempre eseguito all'inizio del programma per aggiungere le transazioni eventualmente
// rimaste arretrate
public class PeriodicTransactionHandler {

    // Usato da PeriodicController per mostrare le righe della TableView e svolgere operazioni su di loro
    public static ObservableList<TableViewRow> getObservableRows() {
        return FXCollections.observableList( TableRowsAdapter.getPeriodicRows( FileHandler.readFile(URL.PERIODIC) ) );
    }

    // Converte le TableViewRow in un formato testuale, e sovrascrive il file
    private static void updateFile(List<TableViewRow> tableViewRows) {
        List<String> text = new ArrayList<>();

        for ( TableViewRow row : tableViewRows )
            text.add(row.getType() + " : " + toCommand(row));

        FileHandler.writeFile(URL.PERIODIC, text);
    }

    // Converte la TableViewRow in un ParsedCommand per l'operazione ADD
    private static ParsedCommand toCommand(TableViewRow row) {
        String code = row.getChartCode();
        String quantity = row.getQuantity().toString();
        String date = row.getDate();
        String description = row.getDescription();

        return new ParsedCommand("ADD", Arrays.asList(code, quantity, date, description), true);
    }

    // Prova a eseguire una transazione periodica finché la data della prossima esecuzione non è nel futuro
    private static void tryExecute(TableViewRow row) {
        Pattern pattern = Pattern.compile("^(\\d+)([YMWD])\\s*");
        LocalDate execDate = LocalDate.parse(row.getDate());

        // Se execDate <= oggi, viene eseguita l'operazione
        while (execDate.isBefore(LocalDate.now()) || execDate.isEqual(LocalDate.now())) {
            // La transazione viene eseguita
            DatabaseHandler.getInstance().executeCommand( toCommand(row) );

            Matcher matcher = pattern.matcher(row.getType());

            // Non dovrebbe mai essere eseguita, ma è qui per completezza
            if (!matcher.matches())
                return;

            int number = Integer.parseInt(matcher.group(1));
            String period = matcher.group(2);

            execDate = switch (period) {
                case "D" -> execDate.plusDays(number);
                case "W" -> execDate.plusWeeks(number);
                case "M" -> execDate.plusMonths(number);
                case "Y" -> execDate.plusYears(number);

                // Non viene mai eseguita grazie al matcher, ma in caso la imposta a domani
                default -> LocalDate.now().plusDays(1);
            };
            row.setDate( execDate.toString() );
        }
    }

    // Viene chiamato all'inizio del programma per provare a eseguire tutte le transazioni periodiche
    // presenti nel file data/periodic.txt
    // Eventuali errori sintattici o semantici vengono ignorati
    public static void tryExecuteAll() {
        FileHandler.checkFileExistence(URL.PERIODIC, null);

        // Qui le righe vengono filtrate in base alla loro correttezza sintattica e semantica
        List<TableViewRow> tableViewRows = TableRowsAdapter.getPeriodicRows( FileHandler.readFile(URL.PERIODIC) );

        for ( TableViewRow row : tableViewRows )
            tryExecute(row);

        // Una volta finito il file viene riscritto con i valori aggiornati
        updateFile(tableViewRows);
    }

    // Aggiunge una singola transazione periodica al file data/periodic.txt
    public static void add(String code, String periodicity, String quantity, String date, String description) {
        TableViewRow row = new TableViewRow(code, periodicity, Double.parseDouble(quantity), date, description);

        // La esegue immediatamente
        // Questo nel caso in cui l'utente decida di mettere la data di partenza nel passato, in modo da inserire
        // subito tutte le transazioni distribuite secondo la periodicità scelta
        tryExecute(row);

        String line = row.getType() + " : " + toCommand(row);
        FileHandler.appendFile(URL.PERIODIC, List.of(line));
    }

    // Cancella la TableViewRow usata
    // Viene usato il metodo TableViewRow.equals() per capire quale riga del file va cancellata
    public static void delete(TableViewRow row) {
        List<TableViewRow> tableViewRows = TableRowsAdapter.getPeriodicRows( FileHandler.readFile(URL.PERIODIC) );

        // Il metodo equals è stato ridefinito
        tableViewRows.remove(row);

        updateFile(tableViewRows);
    }

}
