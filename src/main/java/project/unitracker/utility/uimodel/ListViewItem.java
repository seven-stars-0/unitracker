package project.unitracker.utility.uimodel;

import javafx.scene.image.Image;
import project.unitracker.utility.constant.URL;

// Classe usata per mostrare quali righe di PSQL sono state eseguite correttamente e quali no
public class ListViewItem {
    Image icon;
    String text;

    public ListViewItem(String text, boolean success) {
        icon = new Image( getClass().getResource((success) ? URL.SUCCESS_ICON : URL.ERROR_ICON).toExternalForm() );
        this.text = text;
    }

    public Image getIcon() { return icon; }
    public String getText() { return text; }
}
