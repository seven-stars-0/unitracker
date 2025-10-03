package project.unitracker.utility.psql;

// La classe che usa PseudoSQLHandler per comunicare la correttezza di un comando PSQL
public record CommandValidationResult(ParsedCommand command, PseudoSQLError error) {
}
