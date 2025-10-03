package project.unitracker.model.database.retriever;

import project.unitracker.utility.uimodel.DataPoint;
import project.unitracker.utility.uimodel.HierarchyNode;
import project.unitracker.utility.psql.ParsedCommand;
import project.unitracker.utility.uimodel.TableViewRow;
import project.unitracker.model.database.DatabaseClass;
import project.unitracker.model.database.retriever.backup.BackupRetriever;
import project.unitracker.model.database.retriever.data.DataRetriever;
import project.unitracker.model.database.retriever.hierarchy.HierarchyRetriever;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Handler delle classi che si occupano di interrogare il database per ricevere dati
// Svolge la stessa funzione di DatabaseHandler ma più specifico, e inoltra i risultati a DatabaseHandler che
// a sua volta li inoltrerà a chi ha fatto richiesta
public class RetrieverHandler extends DatabaseClass  {
    private static RetrieverHandler instance;

    private DataRetriever dataRetriever;
    private HierarchyRetriever hierarchyRetriever;
    private BackupRetriever backupRetriever;

    private RetrieverHandler() {}

    public static RetrieverHandler getInstance() {
        if ( instance == null ) instance = new RetrieverHandler();
        return instance;
    }

    public void setConnection(Connection connection) throws SQLException {
        super.setConnection(connection);
        initializeInstances();
    }

    @Override
    protected void initializeInstances() throws SQLException {
        dataRetriever = DataRetriever.getInstance();
        dataRetriever.setConnection(connection);

        hierarchyRetriever = HierarchyRetriever.getInstance();
        hierarchyRetriever.setConnection(connection);

        backupRetriever = BackupRetriever.getInstance();
        backupRetriever.setConnection(connection);
    }

    @Override
    protected void setStatements() {}

    @Override
    public void closeStatements() throws SQLException {
        dataRetriever.closeStatements();
        hierarchyRetriever.closeStatements();
        backupRetriever.closeStatements();
    }

    public List<DataPoint> getLineChartData(String chartCode, String period, boolean appliedOnly) throws SQLException {
        return dataRetriever.getLineChartData(chartCode, period, appliedOnly);
    }

    public List<TableViewRow> getChartData(String code) throws SQLException {
        return dataRetriever.getChartData(code);
    }

    public HierarchyNode getHierarchy(String name, String code) throws SQLException {
        return hierarchyRetriever.getHierarchy(name, code);
    }

    public HierarchyNode getHierarchy() throws SQLException {
        return hierarchyRetriever.getHierarchy();
    }

    public ArrayList<ParsedCommand> getBackupCommands(String codice, boolean isChart) throws SQLException {
       return (isChart) ?
               backupRetriever.getChartBackupData(codice, false) :
               backupRetriever.getGroupBackupData(codice);
    }
}
