package project.unitracker.model.database.retriever.hierarchy;

import project.unitracker.utility.uimodel.HierarchyNode;
import project.unitracker.model.database.DatabaseClass;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// La classe che interroga il database per ottenere la gerarchia di gruppi e grafici
public class HierarchyRetriever extends DatabaseClass {
    private static HierarchyRetriever instance;

    private PreparedStatement CHARTS_OF_GROUP;

    private HierarchyRetriever() {}

    public static HierarchyRetriever getInstance() {
        if (instance == null) instance = new HierarchyRetriever();
        return instance;
    }

    @Override
    protected void setStatements() throws SQLException {
        CHARTS_OF_GROUP = connection.prepareStatement(HierarchyQuery.CHARTS_OF_GROUP);
    }

    @Override
    public void closeStatements() throws SQLException {
        CHARTS_OF_GROUP.close();
    }

    @Override
    protected void initializeInstances() {}

    // Chiede la gerarchia dei nodi subordinati al gruppo 'code'
    // I nodi vengono mostrati in ordine alfabetico, mostrando sempre prima i sottogruppi e poi i grafici
    // appartenenti a 'code'
    private List<HierarchyNode> getSubHierarchy(String code) throws SQLException {
        List<HierarchyNode> subGroups = new ArrayList<>();
        List<HierarchyNode> subTables = new ArrayList<>();

        // Questa viene preparata qui dentro perché non potrebbe funzionare con la ricorsione
        PreparedStatement GROUP_HIERARCHY = connection.prepareStatement(HierarchyQuery.GROUP_HIERARCHY);

        GROUP_HIERARCHY.setString(1, code);
        GROUP_HIERARCHY.setString(2, code);

        ResultSet result = GROUP_HIERARCHY.executeQuery();
        // Salviamo tutti i sottogruppi
        while ( result.next() ) {
            String codice = result.getString("codice");
            String nome = result.getString("nome");

            subGroups.add( new HierarchyNode(nome, codice, getSubHierarchy(codice),false, null) );
        }

        result.close();

        CHARTS_OF_GROUP.setString(1, code);
        result = CHARTS_OF_GROUP.executeQuery();

        // Salviamo tutti i grafici del gruppo
        while ( result.next() ) {
            String codice = result.getString("codice");
            String nome = result.getString("nome");
            String uom = result .getString("unita_di_misura");

            subTables.add( new HierarchyNode(nome, codice, null, true, uom) );
        }

        List<HierarchyNode> subNodes = new ArrayList<>(subGroups);
        subNodes.addAll(subTables);
        return subNodes;
    }

    // Partendo da ROOT, trova tutti i gruppi e grafici nel database, in modo ricorsivo
    public HierarchyNode getHierarchy() throws SQLException {
        return new HierarchyNode("ROOT", "ROOT", getSubHierarchy("ROOT"), false, null);
    }

    // Partendo da 'code', trova tutti i gruppi e grafici nel database, in modo
    // Questa viene chiamata (tramite DatabaseHandler) da BackupRetriever per mantenere la gerarchia nei file di backup
    public HierarchyNode getHierarchy(String name, String code) throws SQLException {
        return new HierarchyNode(name, code, getSubHierarchy(code), false, null);
    }
}
