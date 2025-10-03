package project.unitracker.model.database.retriever.backup;

public class BackupQuery {

    public final static String GROUP_INFO = "SELECT codice, nome, supergruppo FROM Gruppo WHERE codice = ?;";

    // --- Permettono di estrarre tutti i dati relativi a un grafico utili per il backup ---
    public final static String CHART_INFO = "SELECT codice, nome, gruppo, unita_di_misura FROM Grafico WHERE codice = ?;";
    public final static String BACKUP_APPLIED = "SELECT grafico, quantita, data_transazione, descrizione FROM TransazioneApprovata WHERE grafico = ?";
    public final static String BACKUP_ADD_PROV = "SELECT grafico, quantita, data_transazione, descrizione FROM TransazioneProvvisoria WHERE grafico = ?";

}
