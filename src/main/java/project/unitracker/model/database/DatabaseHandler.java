package project.unitracker.model.database;


import project.unitracker.model.io.FileHandler;
import project.unitracker.model.database.command.CommandHandler;
import project.unitracker.model.database.retriever.RetrieverHandler;
import project.unitracker.utility.constant.URL;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.psql.PseudoSQLError;
import project.unitracker.utility.uimodel.DataPoint;
import project.unitracker.utility.uimodel.HierarchyNode;
import project.unitracker.utility.uimodel.TableViewRow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// La classe che funge da interfaccia esterna del database al resto delle classi del programma
// I suoi metodi chiamano a catena quelli che implementano effettivamente l'utilità richiesta,
// ma qui avviene l'effettiva connessione con il database e la gestione delle varie SQLException
// che nelle altre classi vengono semplicemente inoltrate
public class DatabaseHandler extends DatabaseClass {
    private static DatabaseHandler instance;

    private static CommandHandler commandHandler;
    private static RetrieverHandler retrieverHandler;

    private DatabaseHandler() {
        // Verifica l'esistenza del database, e in caso negativo lo copia dal file backup
        // che contiene un database con schema già definito ma istanze vuote (a parte per il gruppo ROOT)
        FileHandler.checkFileExistenceCopy(URL.DATABASE, URL.DB_BACKUP);

        setConnection(null);
        initializeInstances();
    }

    @Override
    // Questa rispetto alle altre classi che estendono DatabaseClass è quella che crea la connessione
    // al posto di riceverla
    public void setConnection(Connection connection) {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + URL.DATABASE);
            // Abilita i vincoli di integrità referenziale
            this.connection.createStatement().execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    // Inizializza gli altri handler
    protected void initializeInstances() {
        try {
            commandHandler = CommandHandler.getInstance();
            commandHandler.setConnection(connection);

            retrieverHandler = RetrieverHandler.getInstance();
            retrieverHandler.setConnection(connection);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setStatements() {}

    @Override
    // Questa inoltra la chiusura degli statement a tutte le classi, e poi chiude la connessione
    // Viene chiamata alla chiusura di tutte le finestre del programma
    public void closeStatements() {
        try {
            commandHandler.closeStatements();
            retrieverHandler.closeStatements();

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseHandler getInstance() {
        if ( instance == null ) instance = new DatabaseHandler();
        return instance;
    }

    // Chiamata da ChartXYAdapter per ottenere i dati del grafico 'code' relativi a 'period'
    public List<DataPoint> getLineChartData(String code, String period, boolean appliedOnly) {
        try {
            return retrieverHandler.getLineChartData(code, period, appliedOnly);
        } catch (SQLException e) {
            return null;
        }
    }

    // Chiamata da TableRowsAdapter per ottenere i dati relativi al grafico 'code' per mostrarli nella TableView
    public List<TableViewRow> getLineChartData(String code) {
        try {
            return retrieverHandler.getChartData(code);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Esegue un ParsedCommand
    public void executeCommand(ParsedCommand cmd) {
        try {
            commandHandler.executeCommand(cmd);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Verifica la correttezza semantica di un ParsedCommand
    public PseudoSQLError checkSemanticError(ParsedCommand cmd) {
        try {
            return commandHandler.checkSemanticError(cmd);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce la gerarchia a partire dal gruppo 'code'
    public HierarchyNode getHierarchy(String name, String code) {
        try {
            return retrieverHandler.getHierarchy(name, code);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce la gerarchia a partire da 'ROOT', quindi la gerarchia totale
    public HierarchyNode getHierarchy() {
        try {
            return retrieverHandler.getHierarchy();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce i comandi necessari per creare il gruppo o il grafico 'code'
    public ArrayList<ParsedCommand> getBackupCommands(String code, boolean isChart) {
        try {
            return retrieverHandler.getBackupCommands(code, isChart);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
