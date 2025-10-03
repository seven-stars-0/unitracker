package project.unitracker.model.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import project.unitracker.utility.constant.URL;
import project.unitracker.utility.uimodel.ViewTuple;
import project.unitracker.controller.Controller;
import project.unitracker.model.io.FileHandler;
import project.unitracker.model.eventbus.ThemeBus;

import java.io.*;
import java.util.List;

// La classe statica che crea scene, stage e si occupa di cambiare tema, segnalarne il cambiamento
// e applicare il cambiamento
public class StageHandler {

    // Variabile che conserva il tema attualmente in uso
    private static String currTheme;

    // Viene chiamato all'apertura del programma
    // Legge da file l'ultimo tema scelto dall'utente
    public static void loadCurrentTheme() {
        FileHandler.checkFileExistence(URL.LAST_THEME, List.of(URL.LIGHT_THEME));

        List<String> lines = FileHandler.readFile(URL.LAST_THEME);

        // Nel caso in cui il file sia stato modificato e presenta valori ingestibili, viene scelta di default la light-theme
        if ( lines.size() != 1 )
            currTheme = URL.LIGHT_THEME;
        else if ( ! (lines.getFirst().equals(URL.LIGHT_THEME) || lines.getFirst().equals(URL.DARK_THEME) ) )
            currTheme = URL.LIGHT_THEME;
        else
            currTheme = lines.getFirst();
    }

    // Cambia il tema da chiaro a scuro, o viceversa
    public static void changeTheme() {
        currTheme = (currTheme.equals(URL.LIGHT_THEME)) ? URL.DARK_THEME : URL.LIGHT_THEME;
        ThemeBus.getInstance().signalThemeChange();
    }

    // Chiamata dai Controller dopo il ThemeBus.signalThemeChange() nel metodo changeTheme()
    // Applica alla scena il CSS del tema scelto
    public static void applyTheme(Scene scene) {
        try {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(StageHandler.class.getResource(currTheme).toExternalForm());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // Viene chiamato alla chiusura del programma
    // Salva l'ultimo tema scelto dall'utente, in modo che alla riapertura con loadCurrentTheme la sua preferenza
    // rimanga
    public static void saveCurrentTheme() {
        FileHandler.writeFile(URL.LAST_THEME, List.of(currTheme));
    }

    // Crea il Parent root e il controller
    // Usa il record ViewTuple perché in alcuni casi (ad esempio nella creazione di Tab in TabPaneModel)
    // è necessario un riferimento al Controller per chiamare alcuni suoi metodi
    public static ViewTuple getRoot(String FXML, List<Object> parameters) {
        try {
            FXMLLoader loader = new FXMLLoader(StageHandler.class.getResource(FXML));
            Parent root = loader.load();

            Controller controller = loader.getController();
            controller.setParameters(parameters);

            return new ViewTuple(root, controller);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Crea uno stage basandosi sui parametri
    private static Stage createStage(String FXML, String title, Stage owner, List<Object> parameters) throws IOException {
        ViewTuple viewTuple = getRoot(FXML, parameters);

        Scene scene = new Scene(viewTuple.root());
        applyTheme(scene);

        Stage stage = new Stage();
        stage.setTitle(title);

        if (owner != null)
            stage.initOwner(owner);
        stage.setScene(scene);

        // Icona di UniTracker
        stage.getIcons().add(new Image(StageHandler.class.getResource(URL.APP_ICON).toExternalForm()));

        // Quando una finestra viene chiusa, il controller esegue il suo metodo di chiusura
        stage.setOnCloseRequest(windowEvent -> viewTuple.controller().onClosed());

        return stage;
    }

    // Apre una finestra non modale
    public static void openWindow(String FXML, String title, Stage owner, List<Object> parameters) {
        try {
            Stage stage = createStage(FXML, title, owner, parameters);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Apre una finestra modale
    public static void openModalWindow(String FXML, String title, Stage owner, List<Object> parameter) {
        try {
            Stage stage = createStage(FXML, title, owner, parameter);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Crea un FileChooser
    private static FileChooser createFileChooser(String title, List<FileChooser.ExtensionFilter> filters) {
        // Creiamo un FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        // Si aggiungono i filtri
        fileChooser.getExtensionFilters().addAll( filters );
        return fileChooser;
    }

    // Apre la finestra per salvare un file
    public static File openFileSaver(Stage stage, String title, List<FileChooser.ExtensionFilter> filters, String initialName) {
        FileChooser fileChooser = createFileChooser(title, filters);

        fileChooser.setInitialFileName(initialName);

        // Mostriamo il dialogo per salvare un file
        return fileChooser.showSaveDialog(stage);
    }

    // Apre la finestra per aprire un file
    public static File openFileChooser(Stage stage, String title, List<FileChooser.ExtensionFilter> filters) {
        FileChooser fileChooser = createFileChooser(title, filters);

        // Mostriamo il dialogo per aprire un file
        return fileChooser.showOpenDialog(stage);
    }

}
