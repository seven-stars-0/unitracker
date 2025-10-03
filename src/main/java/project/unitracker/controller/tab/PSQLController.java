package project.unitracker.controller.tab;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import project.unitracker.controller.Controller;
import project.unitracker.model.psql.PseudoSQLHandler;
import project.unitracker.utility.uimodel.ListViewItem;
import project.unitracker.utility.psql.PseudoSQLError;

import java.util.List;

public class PSQLController implements Controller {

    @FXML
    private SplitPane splitPane;

    @FXML
    private Button executeButton;

    @FXML
    private TextArea pSQLCommands;

    @FXML
    private Label pseudoSQLError;

    private ListView<ListViewItem> listView;
    private Double lastDividerPosition = 0.8;

    @FXML
    // Eseguita premendo il bottone "Cancella tutto"
    void deleteTextArea(ActionEvent event) {
        pSQLCommands.setText("");
    }

    @FXML
    // Eseguita premendo il bottone "Esegui"
    void executePseudoSQLCommands(ActionEvent event) {
        // Le righe vengono trasformate in un array di stringhe
        String[] listOfCommands = pSQLCommands.getText().split("\\r?\\n+");

        for ( String command : listOfCommands ) {

            // Evita di eseguire righe vuote
            command = command.trim();
            if ( command.isEmpty() ) continue;

            // Ogni riga viene eseguita
            PseudoSQLError error = PseudoSQLHandler.interpretAndExecute(command);

            pseudoSQLError.getStyleClass().removeAll("success-label", "error-label");

            // Alla ListView viene aggiunta una ListViewItem, con l'icona di successo o errore in base
            // a error == null
            listView.getItems().add( new ListViewItem(command, error == null) );

            // Se si è verificato un errore, viene mostrato a schermo e l'esecuzione interrotta
            if (error != null) {
                pseudoSQLError.getStyleClass().add("error-label");
                pseudoSQLError.setText(error.toString());
                return;
            }
            // Se tutto è andato a buon fine viene mostrata una scritta verde che lo testimonia
            pseudoSQLError.getStyleClass().add("success-label");
            pseudoSQLError.setText("Successo!");
        }
    }

    @FXML
    // Per mostrare o nascondere la ListView
    // La posizione del divider viene ricordata e ripristinata all'evenienza
    void toggleListView() {
        if ( splitPane.getItems().contains(listView) ) {
            // L'ultima posizione viene salvata
            lastDividerPosition = splitPane.getDividerPositions()[0];
            splitPane.getItems().remove(listView);
        }
        else {
            splitPane.getItems().addLast(listView);
            // L'ultima posizione viene ripristinata
            splitPane.setDividerPositions(lastDividerPosition);
        }
    }

    @FXML
    void initialize() {
        // Viene impedito di eseguire se la TextArea è vuota
        executeButton.disableProperty().bind(
            pSQLCommands.textProperty().isEmpty()
        );

        // Creazione della ListView con le regole per mostrare ListViewItem
        listView = new ListView<>();
        listView.setCellFactory(lv -> new ListCell<ListViewItem>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(ListViewItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                imageView.setImage(item.getIcon());
                imageView.setFitWidth(16);
                imageView.setFitHeight(16);

                setText(item.getText());
                setGraphic(imageView);
            }
        });
    }

    @Override
    // I parametri vengono forniti nel caso di Backup > Importa PSQL
    // I vari comandi contenuti nel file selezionato vengono aggiunti a quelli eventualmente già presenti
    // separandoli da due spazi vuoti
    public void setParameters(List<Object> parameters) {
        // Se non viene passato nessun parametro non c'è niente da fare
        if (parameters == null) return;

        // Per distanziare quello che era già presente da quello che aggiungiamo
        if (! pSQLCommands.getText().isEmpty())
            pSQLCommands.appendText("\n\n");

        for (Object parameter : parameters)
            pSQLCommands.appendText(parameter + "\n");
    }

    @Override
    public void reload() {}

    @Override
    public void onClosed() {}
}
