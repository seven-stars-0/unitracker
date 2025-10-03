package project.unitracker.model.database.retriever.hierarchy;

// Le query usate da HierarchyRetriever
public class HierarchyQuery {
    // Mostra i gruppi contenuti all'interno di un certo supergruppo
    public final static String GROUP_HIERARCHY =
            "SELECT codice, nome, supergruppo FROM Gruppo WHERE supergruppo = ? AND codice != ? ORDER BY nome ASC;";
    // Mostra tutti grafici
    // Viene usata da CommandExecutor nel caso di COMPLETE_DROP
    public final static String ALL_CHARTS =
            "SELECT codice, nome, gruppo, unita_di_misura FROM Grafico";
    // Mostra tutti i grafici appartenenti a un certo gruppo
    public final static String CHARTS_OF_GROUP =
            ALL_CHARTS + " WHERE gruppo = ? ORDER BY nome ASC;";
}
