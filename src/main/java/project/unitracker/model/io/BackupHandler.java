package project.unitracker.model.io;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import project.unitracker.utility.constant.URL;
import project.unitracker.model.view.StageHandler;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.psql.ParsedCommand;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Classe che si occupa di gestire l'import e l'export di file .psql o .txt relativi a comandi
// PseudoSQL o relativi alle transazioni periodiche
public class BackupHandler {

    // Filtri usati per gestire la scelta o il salvataggio di file
    private final static ArrayList<FileChooser.ExtensionFilter> filters = new ArrayList<>(List.of(
            new FileChooser.ExtensionFilter("PSQL", "*.psql"),
            new FileChooser.ExtensionFilter("File di testo", "*.txt")));

    // Converte i ParsedCommand in comandi PSQL testuali
    private static ArrayList<String> generatePSQLCommands(String codice, boolean isChart) {
        ArrayList<ParsedCommand> commands = DatabaseHandler.getInstance().getBackupCommands(codice, isChart);
        ArrayList<String> cmdToString = new ArrayList<>();

        for (ParsedCommand cmd : commands)
            cmdToString.add( cmd.toString() );

        return cmdToString;
    }

    // Chiamato da Backup > Esporta tutto, oppure da DropController quando si preme Backup ed Elimina,
    // oppure semplicemente con click destro su un gruppo o un grafico > Esporta
    public static boolean createBackupFile(Stage stage, String codice, boolean isChart) {
        // Il nome di default del file è CHART o GROUP seguito dal codice
        String initialName = ((isChart) ? "CHART" : "GROUP") + "-" + codice;

        File file = StageHandler.openFileSaver(stage, "Salva", filters, initialName);

        // Se l'utente sceglie di non fare nulla, restituisce false
        if ( file == null )
            return false;

        ArrayList<String> pSQLCommands = generatePSQLCommands(codice, isChart);

        // Prova a scrivere sul file
        return FileHandler.writeFile(file, pSQLCommands);
    }

    // Legge un file .psql o .txt, il cui contenuto verrà passato tramite setParameters() a PSQLController
    public static List<String> readBackupFile(Stage stage) {
        File file =  StageHandler.openFileChooser(stage,"Apri un file di backup", filters);

        // Se l'utente non sceglie nessun file non fa nulla
        if ( file == null )
            return null;

        return FileHandler.readFile(file);
    }

    // Crea un backup del file data/periodic.txt
    public static boolean createPeriodicBackup(Stage stage) {
        // Viene usato solo il filtro .txt
        File file = StageHandler.openFileSaver(stage, "Salva", filters.subList(1,2), "periodic");

        if ( file == null ) return false;

        List<String> periodicContent = FileHandler.readFile(URL.PERIODIC);

        return FileHandler.writeFile(file, periodicContent);
    }

    // Legge un file, facendo APPEND del contenuto su data/periodic.txt
    public static boolean readPeriodicBackup(Stage stage) {
        // Come filtro usiamo solo .txt
        File file =  StageHandler.openFileChooser(stage,"Apri un file di backup", filters.subList(1,2));

        if ( file == null ) return false;

        List<String> periodicContent = FileHandler.readFile(file);
        return FileHandler.appendFile(URL.PERIODIC, periodicContent);
    }
}
