package project.unitracker.model.database.retriever.data;

public class DataQuery {

    private final static String resultColumn =
            "SELECT strftime(?, data_transazione) AS periodo, SUM(quantita) AS totale FROM";
    private final static String unappliedData =
            " (SELECT * FROM TransazioneApprovata WHERE id NOT IN ( SELECT id FROM TransazioneCancellataProvvisoria ) UNION ALL SELECT * FROM TransazioneProvvisoria)";
    private final static String finalPart = " AS Transazione WHERE grafico = ? AND data_transazione >= date('now', ?) GROUP BY periodo ORDER BY periodo ASC";

    // --- Estraggono i dati raggruppandoli e sommandoli per periodo, relativi a un certo grafico ---
    public final static String APPLIED_ONLY = resultColumn + " TransazioneApprovata" +  finalPart;
    public final static String ALL_DATA = resultColumn + unappliedData + finalPart;

    // --- Estraggono tutte le operazioni effettuate su un certo grafico ---
    public final static String APPLIED_DATA = "SELECT id, quantita, data_transazione, descrizione FROM TransazioneApprovata WHERE grafico = ?;";
    public final static String ADD_PROV_DATA = "SELECT id, quantita, data_transazione, descrizione FROM TransazioneProvvisoria WHERE grafico = ?;";
    public final static String DEL_PROV_DATA = "SELECT tcp.id FROM TransazioneCancellataProvvisoria tcp JOIN TransazioneApprovata ta ON tcp.id = ta.id WHERE ta.grafico = ?;";
}
