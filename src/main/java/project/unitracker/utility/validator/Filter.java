package project.unitracker.utility.validator;

import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

// Contiene tutti i filtri usati dai TextField dei controller per prevenire input invalidi
public class Filter {
    public static UnaryOperator<TextFormatter.Change> quantityFilter = change -> {
        String newText = change.getControlNewText();

        if ( newText.matches("\\d*(\\.\\d{0,2})?")) return change;
        return null;
    };

    public static UnaryOperator<TextFormatter.Change> integerFilter = change -> {
        String newText = change.getControlNewText();

        if ( newText.matches("\\d*")) return change;
        return null;
    };

    public static UnaryOperator<TextFormatter.Change> noCommaFilter = change -> {
        String newText = change.getControlNewText();

        if ( newText.matches("[^,]*")) return change;
        return null;
    };
}
