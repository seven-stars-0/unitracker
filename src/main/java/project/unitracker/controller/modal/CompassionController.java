package project.unitracker.controller.modal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import project.unitracker.controller.Controller;
import project.unitracker.model.eventbus.ThemeBus;
import project.unitracker.model.view.StageHandler;

import java.util.List;

// Il controller della finestra modale compassion.fxml
// Modifica il testo della Label e del Button in base ai parametri con cui viene invocato
//
// NOTA: Questa parte del progetto è intesa come scherzo, non aggiunge nessuna funzionalità seria al progetto
// e va interpretata nell'ottica di "aggiunta simpatica all'ultimo minuto" e di "Easter Egg"
public class CompassionController implements Controller {

    @FXML
    private Button button;

    @FXML
    private Label text;

    @FXML
    void goBack(ActionEvent event) {
        ((Stage) text.getScene().getWindow()).close();
    }

    @FXML
    // Registra se stesso al ThemeBus per mantenere la coerenza con eventuali cambi di tema
    void initialize() {
        ThemeBus.getInstance().register(this);
    }

    @Override
    // In base al numero ricevuto come parametro mostra un testo diverso per Label e Button
    // A prescindere dal contenuto, premere Button chiama sempre goBack
    public void setParameters(List<Object> parameters) {
        int type = (int) parameters.getFirst();

        switch (type) {
            case 0 -> {
                text.setText("Complimenti!!!!\nHai vinto il mio affetto e la mia gratitudine <3 <3");
                button.setText("<3 <3 <3 <3");
            }
            case 1 -> {
                text.setText("Se fai così però ci rimango male...");
                button.setText("Giuro che non lo faccio più");
            }
            case 2 -> {
                text.setText("Avevi promesso di non farlo più.\nPerché mi hai mentito?");
                button.setText("Ho cliccato per sbaglio");
            }
            case 3 -> {
                text.setText("Hai oltrepassato il limite.\nNon ti permetto di insultarmi oltre!!!");
                button.setText("Sono stupido");
            }
            case 4 -> {
                text.setText("Ehm... troppa compassione.\nSicuro di non voler usare l'applicazione per quello a cui serve?");
                button.setText("Faccio quello che voglio");
            }
            case 5 -> {
                text.setText("Ma quanto tempo hai perso a compatirmi??\nGuarda che questa voleva essere una cosa per ridere...");
                button.setText("Che ridere");
            }
            default -> {
                text.setText("Non dovresti poter vedermi");
                button.setText("Ho imbrogliato");
            }
        }
    }

    @Override
    // Chiamato da ThemeBus per cambiare CSS alla scena
    public void reload() {
        StageHandler.applyTheme(text.getScene());
    }

    @Override
    // Quando la finestra viene chiusa si cancella da ThemeBus
    public void onClosed() {
        ThemeBus.getInstance().unregister(this);
    }
}
