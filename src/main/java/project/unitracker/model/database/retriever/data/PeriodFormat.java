package project.unitracker.model.database.retriever.data;

// Contiene la strftime che dice al database come mostrare la data dei vari dati
// e il dateModifier relativo al periodo da mostrare (ultimi 14 giorni, 12 settimane, 12 mesi, 5 anni)
public record PeriodFormat(String strftimeFormat, String dateModifier) {
}
