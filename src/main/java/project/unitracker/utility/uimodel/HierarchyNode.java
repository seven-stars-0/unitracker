package project.unitracker.utility.uimodel;

import java.util.List;

// Classe intermedia tra HierarchyRetriever e TreeViewAdapter
public class HierarchyNode {
    public String name;
    private final String code;
    private final List<HierarchyNode> subNodes;
    private final boolean isChart;
    private final String unit;

    // Se un nodo è un grafico avrà anche un'unità di misura
    public HierarchyNode(String name, String code, List<HierarchyNode> subNodes, boolean isChart, String unit) {
        this.name = name;
        this.code = code;
        this.subNodes = subNodes;
        this.isChart = isChart;
        this.unit = unit;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public boolean isChart() { return isChart; }
    public List<HierarchyNode> getSubNodes() { return subNodes; }

    // Converte se stesso nel formato TreeEntry, che non contiene subNodes
    public TreeEntry toTreeEntry() {
        return new TreeEntry(name, code, unit, isChart);
    }
}
