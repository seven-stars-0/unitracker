package project.unitracker.model.psql;


import project.unitracker.utility.psql.CommandValidationResult;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.model.psql.syntax.SyntaxChecker;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.psql.PseudoSQLError;

// La classe statica che viene chiamata per interpretare ed eseguire comandi PSQL
// Si occupa di convertire i comandi da formato testuale a ParsedCommand, verificarne l'eseguibilità
// ed eventualmente eseguirli
public class PseudoSQLHandler {

    // Interpreta e verifica la correttezza sintattica e semantica del comando,
    // convertendo la stringa in un ParsedCommand pronto all'esecuzione
    public static CommandValidationResult validateCommand(String command) {
        ParsedCommand cmd = SyntaxChecker.parseCommand(command);

        // Se null, c'è un errore di sintassi
        if ( cmd == null )
            return new CommandValidationResult(
                    null,
                    new PseudoSQLError("SyntaxError", "Sintassi errata", command)
            );

        PseudoSQLError error = DatabaseHandler.getInstance().checkSemanticError(cmd);

        // Se error == null, il comando sarà valido
        return new CommandValidationResult(cmd, error);
    }

    // Verifica la presenza di errori di sintassi e semantica, e se non sono presenti
    // passa i comandi al DatabaseHandler per eseguirli
    public static PseudoSQLError interpretAndExecute(String command) {
        CommandValidationResult result = validateCommand(command);

        // Se c'è un errore, lo segnala
        if (result.error() != null)
            return result.error();

        // Esegue il comando
        DatabaseHandler.getInstance().executeCommand(result.command());
        return null;
    }
}
