package project.unitracker.model.database.command.executor;

import project.unitracker.model.database.DatabaseClass;
import project.unitracker.model.database.retriever.hierarchy.HierarchyQuery;
import project.unitracker.model.eventbus.CoherenceBus;
import project.unitracker.model.eventbus.HierarchyBus;
import project.unitracker.utility.psql.ParsedCommand;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// La classe che si occupa di eseguire i ParsedCommand
public class CommandExecutor extends DatabaseClass {
    private static CommandExecutor instance;

    private CommandExecutor() {}

    public static CommandExecutor getInstance() {
        if ( instance == null ) instance = new CommandExecutor();
        return instance;
    }

    public void setConnection(Connection connection) throws SQLException {
        super.setConnection(connection);
    }

    @Override
    protected void setStatements() {}

    @Override
    public void closeStatements() throws SQLException {}

    @Override
    protected void initializeInstances() {}

    // Serve a capire quali grafici vengono modificati dal comando che viene eseguito
    // Utilissimo per segnalare l'incoerenza con il database
    private ArrayList<String> findChartsAffected(String DMLQuery, List<String> parameters) throws SQLException {
        ArrayList<String> chartsAffected = new ArrayList<>();

        // Qui viene messa la query da eseguire per trovare i grafici coinvolti
        PreparedStatement statement;

        // Alcune operazioni hanno delle query specifiche per trovare il grafico coinvolto
        // mentre altre la contengono direttamente nei parametri
        switch (DMLQuery) {
            case ExecQuery.G_DROP -> {
                statement = connection.prepareStatement(HierarchyQuery.CHARTS_OF_GROUP);
                statement.setString(1, parameters.getFirst());
            }

            // Questi comandi non vengono eseguiti da una sola query in ExecQuery, quindi hanno bisogno di codici specifici
            case "ALL" -> statement = connection.prepareStatement(ExecQuery.ALL_CHARTS_AFFECTED_CODE);
            case "COMPLETE_DROP" -> statement = connection.prepareStatement(HierarchyQuery.ALL_CHARTS);

            // Partendo dall'id della transazione risaliamo al codice del grafico a cui appartiene
            case ExecQuery.APPLIED_DELETE, ExecQuery.UNAPPLIED_DELETE, ExecQuery.D_APPLY, ExecQuery.D_CANCEL -> {
                statement = connection.prepareStatement(ExecQuery.TRANSACTION_CHART_CODE);
                statement.setInt(1, Integer.parseInt(parameters.getFirst()));
            }


            // Partendo dall'id della transazione risaliamo al codice del grafico a cui appartiene
            case ExecQuery.A_APPLY, ExecQuery.A_CANCEL -> {
                statement = connection.prepareStatement(ExecQuery.A_APPLY_CHART_CODE);
                statement.setInt(1, Integer.parseInt(parameters.getFirst()));
            }

            // Questi invertono la propria lista per eseguire i comandi, quindi il grafico o gruppo
            // affetto è l'ultimo parametro
            case ExecQuery.C_RENAME, ExecQuery.C_MOVE, ExecQuery.G_RENAME, ExecQuery.G_MOVE -> {
                return new ArrayList<>(List.of(parameters.getLast()));
            }

            // In tutti gli altri casi il grafico è semplicemente il primo argomento
            // Nelle operazioni che coinvolgono solo gruppi questo parametro è un codice_gruppo,
            // ma nella gestione dell'incoerenza queste operazioni non segnalano incoerenza per le Tab, quindi non causa problemi
            default -> {
                return new ArrayList<>( List.of(parameters.getFirst()) );
            }
        }

        ResultSet resultSet = statement.executeQuery();

        // Aggiungiamo tutti i grafici che verranno modificati
        while ( resultSet.next() )
            chartsAffected.add(resultSet.getString("codice"));

        resultSet.close();
        statement.close();

        return chartsAffected;
    }

    // Specifica per le operazioni ADD, che richiedono conversione di valori e gestioni dei casi NULL
    private ArrayList<String> executeAdd(String DMLQuery, List<String> parameters) throws SQLException {
        ArrayList<String> chartsAffected = findChartsAffected(DMLQuery, parameters);

        PreparedStatement statement = connection.prepareStatement(DMLQuery);

        // codice_grafico
        statement.setString(1, parameters.getFirst());

        // Questa è la quantità
        statement.setDouble(2, Double.parseDouble(parameters.get(1)));

        // Il parametro 'data' è opzionale, se omesso viene impostata la data corrente
        String dateStr = parameters.get(2);
        LocalDate date = (dateStr == null || dateStr.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(dateStr);
        statement.setString(3, date.toString());

        // Anche la descrizione è opzionale, ma se lasciata vuota viene impostata a NULL
        String description = parameters.get(3);
        if (description == null || description.isBlank())
            statement.setNull(4, Types.VARCHAR);
        else
            statement.setString(4, description);

        // Infine viene eseguita
        statement.executeUpdate();
        statement.close();

        return chartsAffected;
    }

    // Specifica per le operazioni DELETE e CANCEL, che richiedono la conversione a intero
    private ArrayList<String> executeDelete(String DMLQuery, List<String> parameters) throws SQLException {
        ArrayList<String> chartsAffected = findChartsAffected(DMLQuery, parameters);
        PreparedStatement statement = connection.prepareStatement(DMLQuery);

        // I parametri delle DELETE e CANCEL sono solo un intero, che qui viene convertito
        int id_transaction = Integer.parseInt(parameters.getFirst());
        statement.setInt(1,id_transaction);

        statement.executeUpdate();
        statement.close();

        return chartsAffected;
    }

    // Assegna parametri String allo statement fornito in input
    private void setParameters(PreparedStatement statement, List<String> parameters) throws SQLException {
        for ( int i = 0; i < parameters.size(); i++ )
            statement.setString(i + 1, parameters.get(i));
    }

    // Questa esegue query che si basano solo su parametri String
    private ArrayList<String> executeGeneral(String DMLQuery, List<String> parameters) throws SQLException {
        ArrayList<String> chartsAffected = findChartsAffected(DMLQuery, parameters);

        PreparedStatement statement = connection.prepareStatement(DMLQuery);

        setParameters(statement, parameters);

        statement.executeUpdate();
        statement.close();

        return chartsAffected;
    }

    // Chiamata da APPLY e CANCEL (_ALL), applica tutte le transazioni provvisorie (se apply = true)
    // e cancella i dati provvisori. Se parameters.size() != 0, allora si applica solo a un grafico
    // il cui codice è parameters.getFirst()
    // L'auto commit viene disabilitato per evitare problemi di incoerenza, perché queste operazioni devono essere svolte
    // in maniera atomica
    private ArrayList<String> executeMultiple(List<String> parameters, boolean apply) throws SQLException {
        try {
            connection.setAutoCommit(false);

            boolean all = parameters.isEmpty();

            // Prende tutti i grafici che verranno modificati
            // Il caso "SOME" chiama il caso default nello switch in findChartsAffected
            ArrayList<String> chartsAffected = findChartsAffected((all) ? "ALL" : "SOME", parameters);

            String queryToRun;
            // La parte di APPLY_ALL, che applica tutte le transazioni
            if ( apply ) {
                queryToRun = (all) ? ExecQuery.A_APPLY_ALL : ExecQuery.A_APPLY_CHART;

                // Aggiunge tutte le transazioni provvisorie
                try (PreparedStatement statement = connection.prepareStatement(queryToRun)) {
                    setParameters(statement, parameters);
                    statement.executeUpdate();
                }

                queryToRun = (all) ? ExecQuery.D_APPLY_ALL : ExecQuery.D_APPLY_CHART;
                // Cancella tutte le transazioni messe in TransazioneCancellataProvvisoria
                try (PreparedStatement statement = connection.prepareStatement(queryToRun)) {
                    setParameters(statement, parameters);
                    statement.executeUpdate();
                }
            }

            // --- La parte comune a entrambe, dove vengono cancellate le transazioni provvisorie ---

            queryToRun = (all) ? ExecQuery.A_CANCEL_ALL : ExecQuery.A_CANCEL_CHART;

            // Cancella tutte le transazioni provvisorie
            try (PreparedStatement statement = connection.prepareStatement(queryToRun)) {
                setParameters(statement, parameters);
                statement.executeUpdate();
            }

            queryToRun = (all) ? ExecQuery.D_CANCEL_ALL : ExecQuery.D_CANCEL_CHART;


            // Cancella tutte le transazioni da TransazioneCancellataProvvisoria
            try (PreparedStatement statement = connection.prepareStatement(queryToRun)) {
                setParameters(statement, parameters);
                statement.executeUpdate();
            }

            connection.commit();
            return chartsAffected;

        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally { connection.setAutoCommit(true); }
    }

    // Chiamata da (A|D)_APPLY
    // Questa deve eseguire due operazioni atomicamente, quindi annulla l'auto commit per evitare problemi di incoerenza
    private ArrayList<String> executeAtomicApply(String DMLQuery, List<String> parameters) throws SQLException {
        try {
            connection.setAutoCommit(false);

            ArrayList<String> chartsAffected = findChartsAffected(DMLQuery, parameters);

            // Prima operazione
            try (PreparedStatement statement = connection.prepareStatement(DMLQuery)) {
                statement.setInt(1, Integer.parseInt(parameters.getFirst()));
                statement.executeUpdate();
            }

            // Seconda operazione
            String queryToExecute = (DMLQuery.equals(ExecQuery.A_APPLY))
                    ? ExecQuery.A_CANCEL
                    : ExecQuery.D_CANCEL;
            executeDelete(queryToExecute, parameters);

            // Se tutto è andato bene, conferma la transazione
            connection.commit();

            return chartsAffected;

        } catch (SQLException e) {
            // Se qualcosa va storto, annulliamo le modifiche
            connection.rollback();
            throw e;
        } finally {
            // Ripristina autocommit
            connection.setAutoCommit(true);
        }
    }

    // Metodo specifico per COMPLETE_DROP
    // Elimina la gerarchia del database, e tutti i dati dei grafici
    private ArrayList<String> completeDrop() throws SQLException {
        ArrayList<String> chartsAffected = findChartsAffected("COMPLETE_DROP", null);

        PreparedStatement statement = connection.prepareStatement(ExecQuery.COMPLETE_GROUP_DROP);
        statement.executeUpdate();

        statement = connection.prepareStatement(ExecQuery.COMPLETE_CHART_DROP);
        statement.executeUpdate();

        return chartsAffected;
    }

    // In base all'operazione svolta, manda un segnale al ComponentModel di competenza per aggiornare i suoi dati
    private void signalIncoherence(ParsedCommand cmd, ArrayList<String> chartsAffected) {
        // Qualsiasi operazione su gruppi e grafici influenza la TreeView
        // Questo if include anche COMPLETE_DROP
        if ( cmd.type.startsWith("G") || cmd.type.startsWith("C") )
            HierarchyBus.getInstance().signalIncoherence();

        CoherenceBus coherenceBus = CoherenceBus.getInstance();

        switch (cmd.type) {
            // Le APPLY e CANCEL segnalano tutti i grafici che sono stati modificati
            case "APPLY", "CANCEL", "APPLY_ALL", "CANCEL_ALL" -> coherenceBus.signalIncoherence(chartsAffected, cmd.type.startsWith("A"));

            // Queste hanno influenza su un singolo grafico, quindi chiedono solo il primo parametro
            // che corrisponde al codice del grafico
            case "ADD", "DELETE" -> coherenceBus.signalIncoherence(chartsAffected, cmd.apply);

            // C_DROP cancella un solo grafico
            // G_DROP deve far cancellare tutti i grafici aperti in una Tab, e i rispettivi ChartTabModel
            // Il COMPLETE_DROP semplicemente cancella tutto
            case "C_DROP", "G_DROP", "COMPLETE_DROP" -> coherenceBus.signalDelete(chartsAffected);

            default -> {
                // Questa è riferita a (A|D)_(APPLY|CANCEL)
                if (cmd.type.contains("CANCEL") || cmd.type.contains("APPLY") )
                    coherenceBus.signalIncoherence(chartsAffected, cmd.type.contains("APPLY"));
            }
        }
    }

    // I comandi che fanno UPDATE hanno bisogno dell'ordine inverso dei comandi
    private List<String> reverseList(List<String> parameters) {
        List<String> reversed = new ArrayList<>(parameters);
        Collections.reverse(reversed);
        return reversed;
    }

    // I comandi ricevuti sono corretti sintatticamente e semanticamente, e non causano nessuna eccezione
    public void executeCommand(ParsedCommand cmd) throws SQLException {
        ArrayList<String> chartsAffected = switch (cmd.type) {
            // Se parameters.isEmpty() viene eseguita ALL, altrimenti solo la versione relativa a un grafico
            case "APPLY_ALL", "APPLY" -> executeMultiple(cmd.parameters, true);
            case "CANCEL_ALL", "CANCEL" -> executeMultiple(cmd.parameters, false);

            case "COMPLETE_DROP" -> completeDrop();
            case "G_CREATE" -> executeGeneral(ExecQuery.G_CREATE, cmd.parameters);
            case "G_DROP"   -> executeGeneral(ExecQuery.G_DROP, cmd.parameters);
            case "G_MOVE"   -> executeGeneral(ExecQuery.G_MOVE, reverseList(cmd.parameters));
            case "G_RENAME" -> executeGeneral(ExecQuery.G_RENAME, reverseList(cmd.parameters));

            case "C_CREATE" -> executeGeneral(ExecQuery.C_CREATE, cmd.parameters);
            case "C_DROP"   -> executeGeneral(ExecQuery.C_DROP, cmd.parameters);
            case "C_MOVE"   -> executeGeneral(ExecQuery.C_MOVE, reverseList(cmd.parameters));
            case "C_RENAME" -> executeGeneral(ExecQuery.C_RENAME, reverseList(cmd.parameters));

            case "ADD" -> cmd.apply
                    ? executeAdd(ExecQuery.APPLIED_ADD, cmd.parameters)
                    : executeAdd(ExecQuery.UNAPPLIED_ADD, cmd.parameters);

            case "DELETE" -> cmd.apply
                    ? executeDelete(ExecQuery.APPLIED_DELETE, cmd.parameters)
                    : executeDelete(ExecQuery.UNAPPLIED_DELETE, cmd.parameters);

            case "A_APPLY" -> executeAtomicApply(ExecQuery.A_APPLY, cmd.parameters);
            case "D_APPLY" -> executeAtomicApply(ExecQuery.D_APPLY, cmd.parameters);

            case "A_CANCEL" -> executeDelete(ExecQuery.A_CANCEL, cmd.parameters);
            case "D_CANCEL" -> executeDelete(ExecQuery.D_CANCEL, cmd.parameters);

            // SyntaxChecker previene questa chiamata, ma è qui giusto per completezza
            default -> throw new IllegalArgumentException("Tipo comando sconosciuto: " + cmd.type);
        };

        // Segnala l'incoerenza ai grafici modificati
        signalIncoherence(cmd, chartsAffected);
    }
}
