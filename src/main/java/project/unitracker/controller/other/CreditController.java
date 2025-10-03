package project.unitracker.controller.other;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import project.unitracker.utility.constant.URL;
import project.unitracker.controller.Controller;
import project.unitracker.model.io.FileHandler;
import project.unitracker.model.eventbus.ThemeBus;
import project.unitracker.model.view.StageHandler;

import java.util.List;
import java.util.regex.Pattern;

// Il controller della finestra credits.fxml
// Mostra i crediti (ci sono solo io) e permette di provare o rimuovere compassione premendo dei bottoni
//
// NOTA: Questa parte del progetto è intesa come scherzo, non aggiunge nessuna funzionalità seria al progetto
// e va interpretata nell'ottica di "aggiunta simpatica all'ultimo minuto" e di "Easter Egg"
public class CreditController implements Controller {

    @FXML
    private Label compassionCounter;

    @FXML
    private Button deleteButton;

    // Mostra a schermo la quantità di compassione avuta durante le varie esecuzioni del programma
    private SimpleIntegerProperty count;
    // Tiene conto di quante volte è stata rimossa compassione
    private int disableCount = 0;


    @FXML
    // Aggiunge compassione
    // Se si raggiungono le tappe 100, 1.000 e 10.000 viene aperta la finestra modale compassion.fxml con specifici
    // messaggi che segnalano la milestone
    void addCompassion(ActionEvent event) {
        count.set( count.get() + 1 );

        if ( count.get() == 100 )
            StageHandler.openModalWindow(URL.MODAL_COMPASSION, "Grazieeeee", (Stage) deleteButton.getScene().getWindow(), List.of(0));
        else if ( count.get() == 1000 )
            StageHandler.openModalWindow(URL.MODAL_COMPASSION, "Ehm...", (Stage) deleteButton.getScene().getWindow(), List.of(4));
        else if ( count.get() == 10000 )
            StageHandler.openModalWindow(URL.MODAL_COMPASSION, "Sono un po' imbarazzato", (Stage) deleteButton.getScene().getWindow(), List.of(5));
    }

    @FXML
    // Rimuove compassione
    // In una singola sessione si può rimuovere compassione per un massimo di tre volte, raggiunte le quali il bottone
    // per rimuoverla verrà disabilitato, impedendo di offendere il programmatore ulteriormente
    void deleteCompassion(ActionEvent event) {
        count.set( count.get() - 1 );

        // Raggiunta una compassione superiore a 1.000, non viene imposto un vincolo su quante volte si possa rimuovere compassione
        // Questo perché qualsiasi valore superiore a 1.000 è considerato "troppo" e quindi abbassarlo non causa offesa
        if ( count.get() > 1000 )
            return;

        disableCount++;
        StageHandler.openModalWindow(URL.MODAL_COMPASSION, "Non dovevi farlo!", (Stage) deleteButton.getScene().getWindow(), List.of(disableCount) );

        // Il bottone viene disabilitato
        if (disableCount == 3)
            deleteButton.setDisable(true);
    }

    @FXML
    void initialize() {
        // Si registra al ThemeBus per venire notificato di eventuali cambi di tema
        ThemeBus.getInstance().register(this);

        // Verifica l'esistenza del file data/compassion-counter.txt
        // In eventuale assenza lo crea con valore di default 0
        FileHandler.checkFileExistence(URL.COMPASSION, List.of("0"));

        String lastNum = FileHandler.readFile(URL.COMPASSION).getFirst();

        // Nel caso in cui il file sia stato modificato e presenta valori ingestibili
        // la compassione viene azzerata
        if ( ! Pattern.matches("^-?\\d+$", lastNum) )
            lastNum = "0";

        // Il valore contenuto nel file viene trattato come intero
        count = new SimpleIntegerProperty( Integer.parseInt(lastNum) );

        // La label che mostra la compassione viene legata a count
        compassionCounter.textProperty().bind( count.asString() );
    }

    @Override
    // Alla chiusura vengono salvati i progressi e si cancella da ThemeBus
    public void onClosed() {
        FileHandler.writeFile(URL.COMPASSION, List.of( count.asString().get() ));
        ThemeBus.getInstance().unregister(this);
    }

    @Override
    public void setParameters(List<Object> parameters) {}

    @Override
    // Chiamato da ThemeBus per applicare il cambiamento di tema
    public void reload() {
        StageHandler.applyTheme(deleteButton.getScene());
    }
}
