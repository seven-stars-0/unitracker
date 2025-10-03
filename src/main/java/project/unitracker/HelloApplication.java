package project.unitracker;

import javafx.application.Application;
import javafx.stage.Stage;
import project.unitracker.model.periodic.PeriodicTransactionHandler;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.model.view.StageHandler;
import project.unitracker.utility.constant.URL;

import java.io.IOException;

// La classe che gestisce l'apertura e la chiusura del programma
// Qui vengono aperti e chiusi i file necessari al funzionamento del programma e la connessione al database
public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        // Avvia la connessione al database
        DatabaseHandler.getInstance();

        // Esegue le transazioni periodiche che vanno eseguite
        PeriodicTransactionHandler.tryExecuteAll();

        // Carica l'ultimo tema usato
        StageHandler.loadCurrentTheme();

        // Viene aperta la schermata principale
        StageHandler.openWindow(URL.MAIN_FXML, "UniTracker", null, null);
    }

    @Override
    public void stop() {
        // Chiude gli statement e la connection con il database
        DatabaseHandler.getInstance().closeStatements();

        // Salva l'ultimo tema usato
        StageHandler.saveCurrentTheme();
    }
}
