package project.unitracker.model.database.command;

import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.psql.PseudoSQLError;
import project.unitracker.model.database.DatabaseClass;
import project.unitracker.model.database.command.executor.CommandExecutor;
import project.unitracker.model.database.command.semantic.SemanticChecker;

import java.sql.Connection;
import java.sql.SQLException;

// Handler delle classi che si occupano di eseguire o verificare l'eseguibilità dei ParsedCommand
// Inoltra il risultato a DatabaseHandler che a sua volta inoltra a chi ha richiesto il risultato
public class CommandHandler extends DatabaseClass {
    private static CommandHandler instance;

    private SemanticChecker semanticChecker;
    private CommandExecutor commandExecutor;

    private CommandHandler() {}

    public static CommandHandler getInstance() {
        if (instance == null) instance = new CommandHandler();
        return instance;
    }

    public void setConnection(Connection connection) throws SQLException {
        super.setConnection(connection);
        initializeInstances();
    }

    @Override
    protected void initializeInstances() throws SQLException {
        semanticChecker = SemanticChecker.getInstance();
        semanticChecker.setConnection(connection);

        commandExecutor = CommandExecutor.getInstance();
        commandExecutor.setConnection(connection);
    }

    @Override
    protected void setStatements() {}

    @Override
    public void closeStatements() throws SQLException {
        semanticChecker.closeStatements();
        commandExecutor.closeStatements();
    }

    public void executeCommand(ParsedCommand cmd) throws SQLException {
        commandExecutor.executeCommand(cmd);
    }

    public PseudoSQLError checkSemanticError(ParsedCommand cmd) throws SQLException {
        return semanticChecker.checkSemanticError(cmd);
    }
}
