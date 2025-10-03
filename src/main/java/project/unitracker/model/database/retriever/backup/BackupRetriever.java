package project.unitracker.model.database.retriever.backup;

import project.unitracker.model.database.DatabaseClass;
import project.unitracker.model.database.DatabaseHandler;
import project.unitracker.utility.uimodel.HierarchyNode;
import project.unitracker.utility.psql.ParsedCommand;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

// La classe che si occupa di creare ParsedCommand che verranno convertiti in comandi PSQL
// Questi permettono di ricostruire i grafici o i gruppi dei quali si vuole eseguire il backup
public class BackupRetriever extends DatabaseClass {
    private static BackupRetriever instance;

    private PreparedStatement GROUP_INFO, CHART_INFO, APPLIED, ADD_PROV;

    private BackupRetriever() {}

    public static BackupRetriever getInstance() {
        if (instance == null) instance = new BackupRetriever();
        return instance;
    }

    @Override
    protected void setStatements() throws SQLException {
        GROUP_INFO = connection.prepareStatement(BackupQuery.GROUP_INFO);
        CHART_INFO = connection.prepareStatement(BackupQuery.CHART_INFO);

        APPLIED = connection.prepareStatement(BackupQuery.BACKUP_APPLIED);
        ADD_PROV = connection.prepareStatement(BackupQuery.BACKUP_ADD_PROV);
    }

    @Override
    public void closeStatements() throws SQLException {
        GROUP_INFO.close();
        CHART_INFO.close();

        APPLIED.close();
        ADD_PROV.close();
    }

    @Override
    protected void initializeInstances() {}

    // Genera il comando per creare il grafico
    // Se preserveGroup == true, il comando per crearlo manterrà il gruppo di appartenenza attuale, altrimenti
    // viene impostato ROOT
    private ParsedCommand getChartInfo(String code, boolean preserveGroup) throws SQLException {
        ParsedCommand cmd = null;

        CHART_INFO.setString(1, code);
        ResultSet resultSet = CHART_INFO.executeQuery();

        // Estraiamo le informazioni relative al grafico
        if ( resultSet.next() ) {
            String nome = resultSet.getString("nome");
            String gruppo = (preserveGroup) ? resultSet.getString("gruppo") : "ROOT";
            String unit = resultSet.getString("unita_di_misura");

            cmd = new ParsedCommand("C_CREATE", Arrays.asList(code, gruppo, nome, unit), false);
        }

        resultSet.close();
        return cmd;
    }

    // Prendiamo tutti i dati applicati o meno di un grafico, convertendoli in ParsedCommand
    private ArrayList<ParsedCommand> getAddData(String code, boolean applied) throws SQLException {
        ArrayList<ParsedCommand> pSQLCommands = new ArrayList<>();

        PreparedStatement queryToRun = (applied) ? APPLIED : ADD_PROV;

        queryToRun.setString(1, code);
        ResultSet resultSet = queryToRun.executeQuery();

        // Tutti i dati vengono salvati
        while (resultSet.next()) {
            String quantity = resultSet.getString("quantita");
            String date = resultSet.getString("data_transazione");
            String description = resultSet.getString("descrizione");

            ParsedCommand cmd = new ParsedCommand("ADD", Arrays.asList(code, quantity, date, description), applied);
            pSQLCommands.add(cmd);
        }

        resultSet.close();

        return pSQLCommands;
    }

    // Converte i dati del database in comandi pSQL
    // La variabile preserveGroup serve a mantenere il gruppo del grafico invariato nel backup
    // Quando è false, viene impostato ROOT come gruppo
    public ArrayList<ParsedCommand> getChartBackupData(String code, boolean preserveGroup) throws SQLException {
        ArrayList<ParsedCommand> pSQLCommands = new ArrayList<>();

        pSQLCommands.add( getChartInfo(code, preserveGroup) );
        pSQLCommands.addAll( getAddData(code, true) );
        pSQLCommands.addAll( getAddData(code, false) );

        return pSQLCommands;
    }

    // Crea ParsedCommand per tutti i subNodes del gruppo 'node'
    // La sua ricorsività con groupBackup() garantisce che la gerarchia venga mantenuta
    private void collectSubNodes(HierarchyNode node, ArrayList<ParsedCommand> pSQLCommands) throws SQLException {
        for (HierarchyNode subNode : node.getSubNodes())
            if (subNode.isChart())
                pSQLCommands.addAll( getChartBackupData(subNode.getCode(), true));
            else
                pSQLCommands.addAll ( groupBackup(subNode, node.getCode()) );
    }

    // Genera i comandi per creare il gruppo groupNode, mantenendo la gerarchia con il suo supergroup
    // Viene chiamata solo una volta che la gerarchia iniziale è già stata estratta in getGroupBackupData() in modo
    // da non interrogare il database più del necessario
    private ArrayList<ParsedCommand> groupBackup(HierarchyNode groupNode, String supergroup) throws SQLException {
        ArrayList<ParsedCommand> pSQLCommands = new ArrayList<>();

        pSQLCommands.add( new ParsedCommand("G_CREATE", Arrays.asList(groupNode.getCode(), supergroup, groupNode.getName()), false));

        collectSubNodes(groupNode, pSQLCommands);

        return pSQLCommands;
    }

    // Il punto di partenza per creare backup di un gruppo
    // Chiama GROUP_INFO da
    public ArrayList<ParsedCommand> getGroupBackupData(String code) throws SQLException {
        ArrayList<ParsedCommand> pSQLCommands = new ArrayList<>();
        String name = "nome_default";

        // Questo genera il comando per creare il gruppo
        // Il gruppo ROOT esiste sempre e non deve essere mai creato
        if ( ! code.equals("ROOT") ) {
            GROUP_INFO.setString(1, code);
            ResultSet resultSet = GROUP_INFO.executeQuery();

            if (resultSet.next()) {
                name = resultSet.getString("nome");
                pSQLCommands.add(new ParsedCommand("G_CREATE", Arrays.asList(code, "ROOT", name), false));
            }

            resultSet.close();
        }

        // Viene estratta la gerarchia di gruppi e tabelle, che verrà utilizzata per creare comandi
        // che la preservino
        HierarchyNode node = ( code.equals("ROOT") ) ?
                DatabaseHandler.getInstance().getHierarchy() :
                DatabaseHandler.getInstance().getHierarchy(name, code);

        collectSubNodes(node, pSQLCommands);

        return pSQLCommands;
    }

}
