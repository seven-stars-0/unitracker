package project.unitracker.utility.uimodel;

import java.util.Objects;

// Classe usata nelle TableView per mostrare dati relativi a grafici o transazioni periodiche in formato tabellare
public class TableViewRow {
    private String chartCode;
    private Integer id;
    private String type;
    private Double quantity;
    private String date;
    private String description;

    private void initializeOthers(String type, Double quantity, String date, String description) {
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.description = description;
    }

    // Questo costruttore viene usato per le righe nella visualizzazione dei dati di un grafico
    public TableViewRow(Integer id, String type, Double quantity, String date, String description) {
        this.id = id;
        initializeOthers(type, quantity, date, description);
    }

    // Questo invece è usato per mostrare le transazioni periodiche
    public TableViewRow(String chartCode, String type, Double quantity, String date, String description) {
        this.chartCode = chartCode;
        initializeOthers(type, quantity, date, description);
    }

    public String getChartCode() { return chartCode; }
    public Integer getId() { return id; }
    public String getType() { return type; }
    public Double getQuantity() { return quantity; }
    public String getDate() { return date; }
    public String getDescription() { return description; }

    // Questo viene usato nei calcoli delle transazioni periodiche
    // è l'unico valore che può essere modificato
    public void setDate(String date) { this.date = date; }

    @Override
    // Viene usato nel cancellare le transazioni periodiche
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null || getClass() != obj.getClass() ) return false;

        TableViewRow other = (TableViewRow) obj;

        return Objects.equals(id, other.id)
                && Objects.equals(chartCode, other.chartCode)
                && Objects.equals(type, other.type)
                && Objects.equals(quantity, other.quantity)
                && Objects.equals(date, other.date)
                && Objects.equals(description, other.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chartCode, id, type, quantity, date, description);
    }
}
