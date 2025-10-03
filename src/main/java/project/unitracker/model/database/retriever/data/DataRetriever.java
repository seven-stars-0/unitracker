package project.unitracker.model.database.retriever.data;


import project.unitracker.utility.uimodel.DataPoint;
import project.unitracker.utility.uimodel.TableViewRow;
import project.unitracker.model.database.DatabaseClass;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// La classe che si occupa di estrarre dal database i dati relativi a un certo grafico
public class DataRetriever extends DatabaseClass {
    private static DataRetriever instance;

    private PreparedStatement APPLIED_ONLY, ALL_DATA, CHART_ADD_DATA, CHART_ADD_PROV_DATA, CHART_DEL_PROV_DATA;

    private DataRetriever() {}

    public static DataRetriever getInstance() {
        if (instance == null) instance = new DataRetriever();
        return instance;
    }

    @Override
    protected void setStatements() throws SQLException {
        APPLIED_ONLY = connection.prepareStatement(DataQuery.APPLIED_ONLY);
        ALL_DATA = connection.prepareStatement(DataQuery.ALL_DATA);

        CHART_ADD_DATA = connection.prepareStatement(DataQuery.APPLIED_DATA);
        CHART_ADD_PROV_DATA = connection.prepareStatement(DataQuery.ADD_PROV_DATA);
        CHART_DEL_PROV_DATA = connection.prepareStatement(DataQuery.DEL_PROV_DATA);
    }

    @Override
    public void closeStatements() throws SQLException {
        APPLIED_ONLY.close();
        ALL_DATA.close();

        CHART_ADD_DATA.close();
        CHART_ADD_PROV_DATA.close();
        CHART_DEL_PROV_DATA.close();
    }

    @Override
    protected void initializeInstances() {}

    // Prende i dati di 'code' relativi a 'period' e li converte in un formato leggibile per ChartXYAdapter
    // I dati ottenuti sono raggruppati e sommati tra loro dal database in base a 'period'
    public List<DataPoint> getLineChartData(String code, String period, boolean appliedOnly) throws SQLException {
        ArrayList<DataPoint> tableData = new ArrayList<>();

        PeriodFormat periodFormat = PeriodFormatConverter.get(period);

        // Estrae solo i dati applicati oppure tutti i dati, includendo i provvisori
        PreparedStatement queryToRun = (appliedOnly) ? APPLIED_ONLY : ALL_DATA;

        // Impostiamo i parametri della query per estrarre i dati secondo il periodo richiesto
        queryToRun.setString(1, periodFormat.strftimeFormat());
        queryToRun.setString(2, code);
        queryToRun.setString(3, periodFormat.dateModifier());

        ResultSet result = queryToRun.executeQuery();

        while ( result.next() ) {
            String date = result.getString("periodo");
            double value = result.getDouble("totale");

            tableData.add( new DataPoint(date, value) );
        }

        result.close();

        return tableData;
    }

    // Prende i dati di 'code' e li converte in un formato utilizzato dalle TableView dei grafici
    public ArrayList<TableViewRow> getChartData(String code) throws SQLException {

        ArrayList<TableViewRow> tableData = new ArrayList<>();

        CHART_ADD_DATA.setString(1, code);
        ResultSet resultSet = CHART_ADD_DATA.executeQuery();

        // Prendiamo le transazioni applicate
        while ( resultSet.next() ) {
            int id = resultSet.getInt("id");
            double quantita = resultSet.getDouble("quantita");
            String data = resultSet.getString("data_transazione");
            String descrizione = resultSet.getString("descrizione");

            tableData.add( new TableViewRow(id, "APPLIED", quantita, data, descrizione));
        }

        CHART_ADD_PROV_DATA.setString(1, code);
        resultSet = CHART_ADD_PROV_DATA.executeQuery();

        // Transazioni provvisorie
        while ( resultSet.next() ) {
            int id = resultSet.getInt("id");
            double quantita = resultSet.getDouble("quantita");
            String data = resultSet.getString("data_transazione");
            String descrizione = resultSet.getString("descrizione");

            tableData.add( new TableViewRow(id, "PROV", quantita, data, descrizione));
        }

        CHART_DEL_PROV_DATA.setString(1, code);
        resultSet = CHART_DEL_PROV_DATA.executeQuery();

        // Transazioni cancellate provvisorie
        while ( resultSet.next() ) {
            int id = resultSet.getInt("id");

            tableData.add ( new TableViewRow(id, "DELETE", null, null, null));
        }

        resultSet.close();
        return tableData;
    }

}
