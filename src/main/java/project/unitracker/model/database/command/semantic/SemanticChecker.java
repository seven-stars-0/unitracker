package project.unitracker.model.database.command.semantic;

import project.unitracker.utility.psql.PseudoSQLError;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.model.database.DatabaseClass;
import project.unitracker.model.database.retriever.backup.BackupQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

// La classe che si occupa di verificare l'eseguibilità di un ParsedCommand
// Viene utilizzata da PseudoSQLHandler, PeriodicTransactionHandler, e da vari Controller per garantire che
// il comando fornito sia eseguibile.
// Comunica con le altre classi tramite PseudoSQLError. Se restituisce null significa che il comando non causa problemi,
// altrimenti l'errore fornito ha tutte le informazioni necessarie riguardo la natura del problema
public class SemanticChecker extends DatabaseClass {
    private static SemanticChecker instance;

    private PreparedStatement CHART_EXISTENCE, GROUP_EXISTENCE, TRANSACTION_EXISTENCE,
            CHART_NAME_EXISTENCE, GROUP_NAME_EXISTANCE, CHART_NAME, GROUP_NAME, CHART_GROUP, GROUP_SUPERGROUP;

    private SemanticChecker() {}

    @Override
    protected void setStatements() throws SQLException {
        CHART_EXISTENCE = connection.prepareStatement(SemanticQuery.CHART_EXISTENCE);
        GROUP_EXISTENCE = connection.prepareStatement(SemanticQuery.GROUP_EXISTENCE);

        TRANSACTION_EXISTENCE = connection.prepareStatement(SemanticQuery.APPLIED_TRANSACTION_EXISTENCE);

        CHART_NAME_EXISTENCE = connection.prepareStatement(SemanticQuery.CHART_NAME_EXISTENCE);
        GROUP_NAME_EXISTANCE = connection.prepareStatement(SemanticQuery.GROUP_NAME_EXISTENCE);

        CHART_GROUP = connection.prepareStatement(SemanticQuery.CHART_GROUP);
        GROUP_SUPERGROUP = connection.prepareStatement(SemanticQuery.GROUP_SUPERGROUP);

        CHART_NAME = connection.prepareStatement(BackupQuery.CHART_INFO);
        GROUP_NAME = connection.prepareStatement(BackupQuery.GROUP_INFO);
    }

    @Override
    public void closeStatements() throws SQLException {
        CHART_EXISTENCE.close();
        GROUP_EXISTENCE.close();

        TRANSACTION_EXISTENCE.close();

        CHART_NAME_EXISTENCE.close();
        GROUP_NAME_EXISTANCE.close();

        CHART_GROUP.close();
        GROUP_SUPERGROUP.close();

        CHART_NAME.close();
        GROUP_NAME.close();
    }

    public static SemanticChecker getInstance() {
        if ( instance == null ) instance = new SemanticChecker();
        return instance;
    }

    public void setConnection(Connection connection) throws SQLException {
        super.setConnection(connection);
    }

    @Override
    protected void initializeInstances() {}

    // Crea un messaggio di errore riguardante l'esistenza (o meno) di un grafico o gruppo
    private String buildExistenceErrorMessage(String id, boolean chart, boolean exists) {
        StringBuilder errorMessage = new StringBuilder();

        if (chart) errorMessage.append("Grafico ");
        else errorMessage.append("Gruppo ");

        errorMessage.append("\"").append(id).append("\"");

        if (exists) errorMessage.append(" già esistente");
        else errorMessage.append(" non trovato");

        return errorMessage.toString();
    }

    // Crea un messaggio di errore sulla ridondanza del nome di un grafico o gruppo all'interno di 'codice_gruppo'
    private String buildNameRedundancyError(String codice_gruppo, String nome, boolean chart) {
        StringBuilder errorMessage = new StringBuilder("Nome ");

        if (chart) errorMessage.append("grafico");
        else errorMessage.append("gruppo");

        errorMessage.append(" ").append("\"").append(nome).append("\"").append(" ridondante in ").append("\"").append(codice_gruppo).append("\"");
        return errorMessage.toString();
    }

    // Verifica l'esistenza di un grafico o gruppo
    // Lancia o meno un errore in base a 'shouldExist'
    private PseudoSQLError checkExistence(String id, boolean chart, boolean shouldExist) throws SQLException {
        PseudoSQLError error = null;
        ResultSet result;

        PreparedStatement queryToRun = (chart) ? CHART_EXISTENCE : GROUP_EXISTENCE;

        queryToRun.setString(1, id);
        result = queryToRun.executeQuery();

        if ( result.next() ) {
            int count = result.getInt("count");

            // Se non esiste quando dovrebbe esistere, o se esiste quando non dovrebbe, viene lanciato un errore
            if ((shouldExist && count == 0) || (!shouldExist && count > 0))
                error = new PseudoSQLError("SemanticError", buildExistenceErrorMessage(id, chart, count > 0), "");
        }

        result.close();
        return error;
    }

    // Verifica l'esistenza della transazione identificata con quell'id
    private PseudoSQLError checkTransactionID(Integer id, String queryToRun) throws SQLException {
        TRANSACTION_EXISTENCE = connection.prepareStatement(queryToRun);
        TRANSACTION_EXISTENCE.setInt(1, id);

        ResultSet result = TRANSACTION_EXISTENCE.executeQuery();
        PseudoSQLError error = null;

        if ( result.next() ) {
            int count = result.getInt("count");

            // Se la transazione non esiste, lancia un PseudoSQLError
            if ( count == 0 ) {
                String errorMessage = "Transazione " + id + " non trovata";
                if (queryToRun.equals(SemanticQuery.APTE_NOT_CANCEL))
                    errorMessage += " o già cancellata provvisoriamente";
                error = new PseudoSQLError("SemanticError", errorMessage, "");
            }
        }

        result.close();
        return error;
    }

    // L'ordine dei parametri 'id' atteso è il seguente:
    // codice_gruppo, nome
    // Verifica l'esistenza del gruppo di destinazione e la presenza di un grafico o gruppo con il nome scelto
    // Se rename = true, l'ordine atteso è
    // codice, nome
    // Dove codice può essere codice_gruppo o codice_grafico in base al booleano chart
    private PseudoSQLError checkNameRedundancy(List<String> id, boolean chart, boolean rename) throws SQLException {
        PseudoSQLError error1, error2 = null;
        ResultSet result;

        String codice = id.getFirst();
        String nome = id.getLast();

        // Verifica l'esistenza di 'codice'
        // Se rename == true codice rappresenta il codice del gruppo di partenza
        if (rename)
            error1 = checkExistence(codice, chart, true);
        // Questo è il caso MOVE, dove verifichiamo l'esistenza del gruppo di destinazione
        else
            error1 = checkExistence(codice, false, true);

        if (error1 != null) return error1;

        String codice_gruppo = "";

        if (rename) {
            // Troviamo il gruppo di appartenenza del grafico o del gruppo
            PreparedStatement queryToRun = (chart) ? CHART_GROUP : GROUP_SUPERGROUP;
            queryToRun.setString(1, codice);

            result = queryToRun.executeQuery();
            if (result.next())
                codice_gruppo = result.getString("gruppo");
        }
        else
            codice_gruppo = codice;

        // Questa è la parte dove si verifica la ridondanza del nome
        // Verifichiamo che all'interno del gruppo di appartenenza non ci sia un grafico o gruppo con lo stesso nome
        PreparedStatement queryToRun = (chart) ? CHART_NAME_EXISTENCE : GROUP_NAME_EXISTANCE;

        queryToRun.setString(1, codice_gruppo);
        queryToRun.setString(2, nome);

        result = queryToRun.executeQuery();

        if ( result.next() ) {
            int count = result.getInt("count");

            // Significa che c'è ridondanza
            if ( count > 0 )
                error2 = new PseudoSQLError("SemanticError", buildNameRedundancyError(codice_gruppo, nome, chart), "");
        }

        result.close();
        return error2;
    }

    // Chiamato dalle operazioni MOVE, si aspetta parametri nel seguente ordine
    // codice, codice_gruppo
    private PseudoSQLError checkMove(List<String> id, boolean chart) throws SQLException {
        PseudoSQLError error1, error2 = null;

        String codice = id.getFirst(), codice_gruppo = id.getLast();

        // Verifica l'esistenza di 'codice', ossia il gruppo o grafico che vogliamo spostare in un altro gruppo
        error1 = checkExistence(codice, chart, true);
        if (error1 != null) return error1;

        PreparedStatement queryToExecute = (chart) ? CHART_NAME : GROUP_NAME;
        queryToExecute.setString(1, codice);

        // Estrae il nome del grafico o del gruppo
        ResultSet result = queryToExecute.executeQuery();
        if ( result.next() ) {
            String nome = result.getString("nome");
            // Verifica l'esistenza del gruppo di destinazione, e la ridondanza del nome all'interno del gruppo di destinazione
            error2 = checkNameRedundancy(List.of(codice_gruppo, nome), chart, false);
        }
        result.close();
        return error2;
    }

    // L'ordine dei parametri 'id' atteso è il seguente:
    // codice, codice_gruppo, nome
    private PseudoSQLError checkCreate(List<String> id, boolean chart) throws SQLException {
        PseudoSQLError error1;
        // Non deve esistere nessun grafico o gruppo con lo stesso codice
        error1 = checkExistence(id.getFirst(), chart, false);
        if (error1 != null) return error1;

        // In quel gruppo non deve esistere un grafico o gruppo che ha già quel nome
        return checkNameRedundancy(id.subList(1,3), chart, false);
    }

    // L'unico metodo esposto
    // In base al tipo di comando chiama metodi diversi che verificano la correttezza semantica del comando
    public PseudoSQLError checkSemanticError(ParsedCommand cmd) throws SQLException {
        return switch (cmd.type) {
            // Non hanno parametri, quindi non causano nessun errore
            case "APPLY_ALL":
            case "CANCEL_ALL":
            case "COMPLETE_DROP":
                yield null;

            // Questi richiedono che l'id fornito esista
            case "A_APPLY":
            case "A_CANCEL":
                yield checkTransactionID(Integer.parseInt(cmd.parameters.getFirst()), SemanticQuery.ADD_PROV_TRANS_EXISTENCE);
            case "D_APPLY":
            case "D_CANCEL":
                yield checkTransactionID(Integer.parseInt(cmd.parameters.getFirst()), SemanticQuery.DEL_PROV_TRANS_EXISTENCE);
            case "DELETE":
                yield (cmd.apply) ?
                        checkTransactionID(Integer.parseInt(cmd.parameters.getFirst()), SemanticQuery.APPLIED_TRANSACTION_EXISTENCE) :
                        checkTransactionID(Integer.parseInt(cmd.parameters.getFirst()), SemanticQuery.APTE_NOT_CANCEL);

            // --- Questi richiedono che il grafico o il gruppo esistano ---
            case "ADD":
            case "APPLY":
            case "CANCEL":
            case "C_DROP":
                yield checkExistence(cmd.parameters.getFirst(), true, true);
            case "G_DROP":
                // --- Impedisce che il gruppo ROOT venga eliminato ---
                if ( cmd.parameters.getFirst().equals("ROOT") )
                    yield new PseudoSQLError("SemanticError", "Impossibile eliminare il gruppo ROOT", "");
                yield checkExistence(cmd.parameters.getFirst(), false, true);


            // --- Questi richiedono rispettivamente che il gruppo non esista ed esista
            case "G_CREATE":
                yield checkCreate(cmd.parameters, false);
            case "C_CREATE":
                yield checkCreate(cmd.parameters, true);

            // --- Questi fanno la stessa cosa, ma il loro comportamento cambia in base al fatto che sia un grafico o un gruppo ---
            case "G_MOVE":
                yield checkMove(cmd.parameters, false);
            case "C_MOVE":
                yield checkMove(cmd.parameters, true);

            // --- Questi fanno la stessa cosa, ma il loro comportamento cambia in base al fatto che sia un grafico o un gruppo ---
            case "G_RENAME":
                yield checkNameRedundancy(cmd.parameters, false, true);
            case "C_RENAME":
                yield checkNameRedundancy(cmd.parameters, true, true);

            default:
                yield new PseudoSQLError("InvalidCommand", "Tipo di comando non valido", cmd.type);
        };
    }
}
