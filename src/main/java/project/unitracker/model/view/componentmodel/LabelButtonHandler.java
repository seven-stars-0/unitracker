package project.unitracker.model.view.componentmodel;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import project.unitracker.utility.uimodel.LabelUtility;

// Questa classe si occupa di gestire le Label e Button per l'interazione con i form
// I bottoni vengono disabilitati o meno e il testo della label cambia in base ai valori dei campi inseriti dall'utente
public class LabelButtonHandler {

    // Questa imposta un testo fornito come argomento
    public static void updateLabelButton(Label label, Button button, String text, boolean success) {
        label.getStyleClass().clear();
        label.getStyleClass().add( (success) ? LabelUtility.SUCCESS_CLASS : LabelUtility.ERROR_CLASS );

        label.setText(text);
        button.setDisable(!success);
    }

    // Questa imposta un testo di default
    public static void updateLabelButton(Label label, Button button, boolean success) {
        String text = (success) ? LabelUtility.NO_ERROR : LabelUtility.MISSING_ARG;
        updateLabelButton(label, button, text, success);
    }
}
