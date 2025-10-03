package project.unitracker.utility.uimodel;

// Classe usata da GroupTreeModel per mostrare la gerarchia di gruppi e grafici
// Questo formato viene anche usato da alcuni Controller per avere accesso ai dati del nodo attualmente selezionato
public class TreeEntry {
    private final String name;
    private final String code;
    private final boolean isChart;
    private final String unit;

    public TreeEntry(String name, String code, String unit, boolean isChart) {
        this.name = name;
        this.code = code;
        this.isChart = isChart;
        this.unit = unit;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public boolean isChart() { return isChart; }
    public String getUnit() { return unit; }

    @Override
    public String toString() {
        return name; // Così la TreeView mostra solo il nome
    }
}
