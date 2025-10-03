package project.unitracker.utility.psql;

// Classe che comunica eventuali errori sintattici o semantici nell'esecuzione di un comando PSQL o di un ParsedCommand
public class PseudoSQLError {
    private String type, message, input;

    public PseudoSQLError(String type, String message, String input) {
        this.type = type;
        this.message = message;
        this.input = input;
    }

    @Override
    public String toString() {
        String errorMessage = message;
        if ( ! input.isBlank() || ! input.isEmpty() ) errorMessage += " (input: " + input + ")";
        return errorMessage;
    }
}
